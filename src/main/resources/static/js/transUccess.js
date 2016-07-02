$(document).ready(function() {
// Used to maintain opacity if the PC is on brush mode and the user changed time
// interval
var isOnBrushMode = false;
var brushedLines;
function TwoDigits(val) {
	if (val < 10) {
		return "0" + val;
	}

	return val;
}

$("#slider").dateRangeSlider({
	bounds : {
		min : new Date(2013, 0, 1),
		max : new Date(2013, 0, 1, 23, 59, 59)
	},
	defaultValues : {
		min : new Date(2013, 0, 1, 6),
		max : new Date(2013, 0, 1, 9)
	},
	formatter : function(value) {
		var hours = value.getHours(), minutes = value.getMinutes();
		return TwoDigits(hours) + ":" + TwoDigits(minutes);
	},
	step : {
		minutes : 60
	}
}).bind(
		"valuesChanged",
		function(e, data) {
			// Redraw upon filtering
			draw_areas_and_lines(data.values.min.getHours(), data.values.max
					.getHours());
		});

// place holder for the parallel-coordinates draw
var host = "localhost"; // "5.102.230.126"
var parcoords = d3.parcoords()("#parallelCoords");
var parCoordData;
// Importing the mapbox tiles layer. For our purposes, the example map is
// sufficient
var mapboxTiles = L
		.tileLayer(
				'https://api.mapbox.com/v4/mapbox.streets/{z}/{x}/{y}.png?access_token={token}',
				{
					attribution : 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="http://mapbox.com">Mapbox</a>',
					mapId : 'mapbox-streets',
					token : 'pk.eyJ1Ijoic2hha2VkayIsImEiOiJjaWxjYzVxbzIwMDZud2dsejg3Zmw3dncyIn0.1mxg8ZqXNXzMZ2OkH9os5A'
				});
// Importing the map from leaflet and adding the tiles layer.
// The initial view is centered to Tel Aviv, according to the example info
// (lines 171 and 222).
var map = L.map('map').addLayer(mapboxTiles).setView(
		[ 32.087917, 34.795551], 13.5);
var popup = new L.Popup({
	autoPan : false
});

// Initialize the SVG layer
L.svg().addTo(map);
// Categorical colors for 2 lines routes and the optional changing station's
// colors
var mappingColors = {
	"areaBorderColor" : "rgb(152,78,163)",
};
  // onevariant colors used for PC & Map
var colors = ["rgb(129, 16, 237)","rgb(45, 85, 253)","rgb(6, 155, 221)","rgb(0, 192, 191)","rgb(8, 226, 148)","rgb(112, 245, 26)","rgb(196, 187, 0)","rgb(232, 139, 12)","rgb(254, 75, 53)","rgb(238, 17, 128)"];
	
var irrelevantcolor = "rgba(255,0,0,0)";

// Holds the hues that will be assigned for the waiting time mapping (the
// station changeStations color).
// The actual setting is done after the relevant number of optional changing
// station is calculated.
var stationsHues;
// We pick up the SVG from the map object
var svg = d3.select(map.getPanes().overlayPane).append("svg"), g = svg
		.append("g").attr("class", "leaflet-zoom-hide");
// Styling variables used areas border
var sAreaStyle = {
	"fillColor" : mappingColors.areaBorderColor
};
var areasLayer;

draw_areas_on_map = function(startHour,endHour) {
	// Preventing filtering for the same hour
	if (startHour === endHour){
		alert("Please select an interval of at least two hours");
		return;
	}
	d3.json('http://' + host + ':8080/areas/'+startHour+'/'+endHour, function(data) {
		if (typeof areasLayer != 'undefined'){						
			updateStyles(data);
			if (isOnBrushMode){
				onBrushEvent(brushedLines);
			} 
			// Hour Filtering without brushing
			else {
			rePaintAreas();
			}
		} else{
		areasLayer = L.geoJson(data, {
			style : getAreaStyle,
			onEachFeature : onEachFeature
		}).addTo(map);
		}
	})
}

var updateStyles = function(data){
// Creating a map of areaID->Style as the data array isn't soreted
var areaStyleMap = new Map();
var areaID;
var areaNewStyle;
for (i=0; i < data.features.length; i++){
	areaID = data.features[i].properties.Name
	areaNewStyle = data.features[i].properties.styleHash;
	areaStyleMap.set(areaID,areaNewStyle);
}
// Updating styles
		areasLayer.eachLayer(function(layer) {												
			layer.feature.properties.styleHash = areaStyleMap.get(layer._leaflet_id);
			// layer.feature.
		});					  

}
	
var color = function(d) {

	if (d >= 0 && d <= colors.length) {
		return colors[d];
	}
	return irrelevantcolor;

};

function getAreaStyle(feature) {
	return {
		weight : 0.5,
		opacity : 0.5,
		color : 'black',
		fillOpacity : 0.7,
		fillColor : color(feature.properties.styleHash),
	};
}

// Reapint Areas back to default paint
function rePaintAreas() {
	areasLayer.eachLayer(function(layer) {
		layer.setStyle(getAreaStyle(layer.feature));
	});
}

function onEachFeature(feature, layer) {
	var areaID = feature.properties.Name;				
	layer._leaflet_id = areaID;
	layer.on({
		contextmenu : contextmenu,
		// mouseout : mouseout,
		// text : areaID
	});
}

map.on('click', function(e) {
	parcoords.unhighlight();
});
var closeTooltip;

function contextmenu(e) {
	// Highlight the cooresponding line in the PC chart
	parLineHightlight(e);
	var layer = e.target;

	popup.setLatLng(e.latlng);
	popup.setContent('<div class="marker-title">Area ID: '
			+ layer.feature.properties.Name + " TAI: "+ layer.feature.properties.styleHash+'</div>');

	if (!popup._map)
		popup.openOn(map);
	window.clearTimeout(closeTooltip);

	// highlight feature
	layer.setStyle({
		weight : 2,
		opacity : 0.3,
		fillOpacity : 0.9
	});
	if (!L.Browser.ie && !L.Browser.opera) {
		layer.bringToFront();
	}
}

function mouseout(e) {
	areasLayer.resetStyle(e.target);
}

function zoomToFeature(e) {
	map.fitBounds(e.target.getBounds());
}

// Adding a legend with sequential horizontal bar for easy access
var legend = L.control({
	position : 'bottomright'
});
legend.onAdd = function(map) {
	var div = L.DomUtil.create('div', 'legend');
	div.innerHTML += '<p style="margin:auto; background: linear-gradient(to right, '
			+ colors + ')"</p>' + '<br>';
	div.innerHTML += '<p style="font: 25px bold"> Lowest Accessibility &#8596 Highest Accessibility</p>';
	return div;
};
legend.addTo(map);

// Adding the areas and the Pc right upon page load
$(document).ready(function() {
	draw_areas_and_lines();
});

// Main function for drawing the elements or re-drawing them. map si called
// first to avoid out of date data
draw_areas_and_lines = function (startHour="06",endHour="09"){
	draw_areas_on_map(startHour,endHour);
	draw_par_coords();
}

// load csv file and create the chart
draw_par_coords = function() {
		d3.json('http://' + host + ':8080/areaPcProperties/',
			function(data) {
				parCoordData = data;
	
				var customScale = function(columName,rangeMin,rangeMax,isClamp){
					range = parcoords.height()
					- parcoords.margin().top
					- parcoords.margin().bottom;
			min = d3.min(data, function(d) {
				return parseInt(d[columName]);
			});
			max = d3.max(data, function(d) {
				return parseFloat(d[columName]);
			});
			return d3.scale.linear().clamp(true).domain([ min, max ])
					.range([ range, 1 ]);
				}

				// creating a SQRT scale
				var sqrtScale = function(columName) {
					range = parcoords.height()
							- parcoords.margin().top
							- parcoords.margin().bottom;
					min = d3.min(data, function(d) {
						return parseInt(d[columName]);
					});
					max = d3.max(data, function(d) {
						return parseFloat(d[columName]);
					});
					return d3.scale.sqrt().clamp(true).domain([ min, max ])
							.range([ range, 1 ]);
				}
				
				// creating a custom SQRT scale for ShapeArea
				var shapeAreaSqrtScale = function(columName) {
					range = parcoords.height()
							- parcoords.margin().top
							- parcoords.margin().bottom;
					min = d3.min(data, function(d) {
						return parseInt(d[columName]);
					});
					max = d3.max(data, function(d) {
						return 1.2;
					});
					return d3.scale.linear().clamp(true).domain([ min, max ])
							.range([ range, 1 ]);
				}

				var dimensions = {
					"numberOfStopsInArea" : {
						title : 'Stops in Zone',
						// Scaling the yAxis as sqrt to be less condensed
						yscale : sqrtScale('numberOfStopsInArea'),
					},
					"shapeArea" : {
						title : 'Area (km^2)',
						// Scaling the yAxis as sqrt to be less condensed
						// yscale : sqrtScale('shapeArea'),
						yscale : shapeAreaSqrtScale('shapeArea'),
					},
					"population" : {
						title : 'Population',
						// Scaling the yAxis as sqrt to be less condensed
						yscale : sqrtScale('population'),
					},
					"averageFrequencies" : {
						title : 'ZAF',	
						
					},
					"tai" : {
						title : 'TAI',
						tickValues: [0,1,2,3,4,5,6,7,8,9,10],
						
					},	
					"medianIncome" : {
						title : 'Median Income',
						ticks: 6,
						// yscale : sqrtScale('medianIncome'),
					},

				};

				var pcColor = function(d) {
					return color(Math.floor(d.tai));
					
				}
				// Filtering out business areas (their Median income is < 0)
				// Change all zones aiwth area > 3 to 1
				data = data.filter(function(d) {								
					return parseFloat(d.tai) >= 0
				})
				parcoords.data(data)
				.color(pcColor).dimensions(dimensions)
						.detectDimensions()
						.hideAxis(["areaID"])
						.render().alpha(0.5)
						.render().shadows().reorderable()
						.composite("darken").margin({
							top : 24,
							left : 150,
							bottom : 12,
							right : 0
						}).mode("queue").render().brushMode(
								"1D-axes").updateAxes()// enable brushing});

				// styling the text
				parcoords.svg.selectAll("text").style("font",
						"25px sans-serif").style("font-weight", "bold");
		
				parcoords.svg.attr("transform", "translate(90,24)");
				parcoords.svg.attr("margin-bottom", "20px");
				
				parcoords.svg.selectAll(".dimension text.label")
						.style("color", "red");

							onBrushEvent = function(d) {
								brushedLines = d;
								isOnBrushMode = true;
								if (areasLayer) {
									// Making the entire layer more transperent
									areasLayer									
									.setStyle({
										fillOpacity : 0.1,
										
									});
									d.forEach(function(polygon) {
										selectPolygon = areasLayer
												.getLayer(polygon.areaID);
										setAreaHighlighted(selectPolygon);
									});
								}
							}			
				// update map hightlight brush event
				parcoords.on("brush",onBrushEvent );

				var sltBrushMode = d3.select('#sltBrushMode')
				sltBrushMode.selectAll('option').data(
						parcoords.brushModes()).enter().append(
						'option').text(function(d) {
					return d;
				});

				sltBrushMode.on('change', function() {
					parcoords.brushMode(this.value);
					switch (this.value) {
					case 'None':
						d3.select("#pStrums").style("visibility",
								"hidden");
						d3.select("#lblPredicate").style(
								"visibility", "hidden");
						d3.select("#sltPredicate").style(
								"visibility", "hidden");
						d3.select("#btnReset").style("visibility",
								"hidden");
						break;
					case '2D-strums':
						d3.select("#pStrums").style("visibility",
								"visible");
						break;
					default:
						d3.select("#pStrums").style("visibility",
								"hidden");
						d3.select("#lblPredicate").style(
								"visibility", "visible");
						d3.select("#sltPredicate").style(
								"visibility", "visible");
						d3.select("#btnReset").style("visibility",
								"visible");
						break;
					}
				});
				d3.select('#btnReset').on('click', function() {
					parcoords.brushReset();
					rePaintAreas();
					isOnBrushMode = false;
				})
				d3.select('#sltPredicate').on('change', function() {
					parcoords.brushPredicate(this.value);
				});
			});
};

// Highlight the cooresponding line in the PC chart
parLineHightlight = function(e) {
	lineToHighlight = parCoordData.filter(function(d) {
		return ""+d.areaID === e.target.feature.properties.Name;
	});
	// Don't highlight business areas, where median income is >
	if (lineToHighlight[0].medianIncome > 0) {
		parcoords.highlight(lineToHighlight);
	}
};

setAreaHighlighted = function(polygon) {
	polygon.setStyle({
		fillOpacity : 1,
		fillColor: color(polygon.feature.properties.styleHash),
	});
};


});