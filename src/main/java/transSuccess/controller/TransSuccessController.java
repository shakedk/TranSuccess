package transSuccess.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.GeoJsonObject;
import org.geojson.LineString;
import org.geojson.LngLatAlt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import transSuccess.service.TransSuccessService;

@ComponentScan("transSuccess")
@Controller
@EnableAutoConfiguration
public class TransSuccessController {

	@Autowired
	TransSuccessService transSuccessService;

	@RequestMapping(value = "/areaPcProperties", method = RequestMethod.GET,
			produces="application/json")
	public @ResponseBody String getAreasPropertiesForPcChart() throws IOException {
		JsonNode jsonNode = transSuccessService.getAreasPropertiesForPcChart();
		System.out.println(jsonNode.toString());
		return jsonNode.toString();
		
	}
	
	@RequestMapping(value = "/areas/{startHour}/{endHour}", method = RequestMethod.GET,
			produces="application/json")
	public @ResponseBody String getTelAvivAreas(@PathVariable("startHour") int startHour,
			@PathVariable("endHour") int endHour) throws IOException {		
		if (endHour-startHour==0){
			return "";
		}
		JsonNode jsonNode = transSuccessService.getTelAvivAreasForMap(startHour,endHour);
		
		return jsonNode.toString();
	}
	
//	@RequestMapping(value = "/stations", method = RequestMethod.GET,
//			produces="application/json")
//	public @ResponseBody String getTelAvivStations() throws JsonProcessingException {
//		
//		JsonNode jsonNode = transSuccessService.getTelAvivStations();
//		return jsonNode.toString();
//	}
//	
	public static void main(String[] args) throws Exception {
		SpringApplication.run(TransSuccessController.class, args);		
//		TransSuccessService.updateStopsFreqs(TransSuccessService.getTelAvivStopIDs());
		
	}
}