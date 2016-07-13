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
	draw_areas_and_lines();
}

// Main function for drawing the elements or re-drawing them. map si called
// first to avoid out of date data
draw_areas_and_lines = function (startHour="06",endHour="09"){
	draw_areas_on_map(startHour,endHour);
	draw_par_coords();
}

// JQuery Time Slider for hour filtering
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

/**
 * General variables used by the map and the Parallel-Coordinates Chart
 * (abbreviated as PC in this js)
 */

// change this to the server's IP if you wish for remote access
var host = "localhost"; // "5.102.230.126"

// place holder for the parallel-coordinates draw
var parcoords = d3.parcoords()("#parallelCoords");

// Data for the parallel-coordinates chart
var parCoordData;

// colors used for PC & Map areas/lines
var colors = ["rgb(129, 16, 237)","rgb(45, 85, 253)","rgb(6, 155, 221)","rgb(0, 192, 191)","rgb(8, 226, 148)","rgb(112, 245, 26)","rgb(196, 187, 0)","rgb(232, 139, 12)","rgb(254, 75, 53)","rgb(238, 17, 128)"];
	
// Color use for irrelvant area, e.g. area with no sufficient population or data
var irrelevantcolor = "rgba(255,0,0,0)";

/**
 * Generating, coloring and updating the map
 * 
 */
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

// Importing the map from leaflet and adding the tiles layer. the coordiantes
// point to Tel Aviv center
var map = L.map('map').addLayer(mapboxTiles).setView(
		[ 32.087917, 34.795551], 13.5);
var popup = new L.Popup({
	autoPan : false
});

// Initialize the SVG layer
L.svg().addTo(map);


// We pick up the SVG from the map object
var svg = d3.select(map.getPanes().overlayPane).append("svg"), g = svg
		.append("g").attr("class", "leaflet-zoom-hide");

var areasLayer;

// Drawing the city's areas on the map with TAI encoded as the area color
// according to the time filter
draw_areas_on_map = function(startHour,endHour) {
	// Preventing filtering for the same hour
	if (startHour === endHour){
		alert("Please select an interval of at least two hours");
		return;
	}
	// Getting the area new colors and updating it accordingly
	d3.json('http://' + host + ':8080/areas/'+startHour+'/'+endHour, function(data) {
		if (typeof areasLayer != 'undefined'){
			// Updating the colors data
			updateAreaColors(data);
			// If on PC brush mode, filter the areas on the map according to the
			// brushed lines
			if (isOnBrushMode){
				onBrushEvent(brushedLines);
			} 
			// Paint areas to dfeault color, based on hour filtering (not Pc
			// brush)
			else {
			rePaintAreadToDefault();
			}
		} else{
		// Defining the areas layer with color and behavior for each area
		areasLayer = L.geoJson(data, {
			style : getAreaStyles,
			onEachFeature : onEachFeature
		}).addTo(map);
		}
	})
}

// Removing highlights from the map
map.on('click', function(e) {
	parcoords.unhighlight();
});


var updateAreaColors = function(data){
// Creating a map of areaID->Style as the data array isn't sorted
var areaStyleMap = new Map();
var areaID;
var areaNewStyle;
for (i=0; i < data.features.length; i++){
	areaID = data.features[i].properties.Name
	areaNewStyle = data.features[i].properties.styleHash;
	areaStyleMap.set(areaID,areaNewStyle);
}

// Updating the actual styles
		areasLayer.eachLayer(function(layer) {												
			layer.feature.properties.styleHash = areaStyleMap.get(layer._leaflet_id);
		});					  

};
	
// Mapping TAI to a color from the color scale
var getColorforTai = function(d) {
	if (d >= 0 && d <= colors.length) {
		return colors[d];
	}
	return irrelevantcolor;
};

// Default area styles
function getAreaStyles(feature) {
	return {
		weight : 0.5,
		opacity : 0.5,
		color : 'black',
		fillOpacity : 0.7,
		fillColor : getColorforTai(feature.properties.styleHash),
	};
}


// aeapint Areas back to default paint
function rePaintAreadToDefault() {
	areasLayer.eachLayer(function(layer) {
		layer.setStyle(getAreaStyles(layer.feature));
	});
}

// Define the beahviour of each area - defining right click for text show
function onEachFeature(feature, layer) {
	var areaID = feature.properties.Name;				
	layer._leaflet_id = areaID;
	layer.on({
		contextmenu : contextmenu,
	});
}
// Clear highlights from the map
map.on('click', function(e) {
	parcoords.unhighlight();
});

// var closeTooltip;

function contextmenu(e) {
	// Highlight the cooresponding line in the PC chart
	parLineHightlight(e);
	var layer = e.target;

	popup.setLatLng(e.latlng);
	popup.setContent('<div class="marker-title">Area ID: '
			+ layer.feature.properties.Name + " TAI: "+ layer.feature.properties.styleHash+'</div>');

	if (!popup._map)
		popup.openOn(map);

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

// Adding a legend with color scheme
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

/**
 * Generating and updating the parallel coordinates chart
 */
// Generate the PC chart
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

				// Defining the PC axes
				var dimensions = {
					"numberOfStopsInArea" : {
						title : 'Stops in Zone',
						// Scaling the yAxis as sqrt to be less condensed
						yscale : sqrtScale('numberOfStopsInArea'),
					},
					"shapeArea" : {
						title : 'Area (km^2)',
						// Scaling the yAxis as sqrt to be less condensed
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
					},

				};
				
				
				var pcColor = function(d) {
					return getColorforTai(Math.floor(d.tai));
					
				}
				// Filtering out business areas (their Median income is < 0)
				// Change all zones aiwth area > 3 to 1
				data = data.filter(function(d) {								
					return parseFloat(d.tai) >= 0
				})
				parcoords.data(data).color(pcColor).dimensions(dimensions)
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

//highlighting areas according to brushed lines 
setAreaHighlighted = function(polygon) {
	polygon.setStyle({
		fillOpacity : 1,
		fillColor: getColorforTai(polygon.feature.properties.styleHash),
	});
};


});