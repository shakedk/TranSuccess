package transSuccess.repository;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.sf.json.JSONObject;

@Repository
public class FilesRepository {
	
	public FilesRepository(){}
	
	private static final String AREAS_PATH = "static/tel_aviv_areas.json";
	private static final String STATION_PATH = "static/stations.geojson";
	
	public JsonNode getTelAvivAreas(){
		return getFileFromPath(AREAS_PATH);
	}
	public JsonNode getTelAvivStations(){
		return getFileFromPath(STATION_PATH);
	}	
	private JsonNode getFileFromPath(String path){
	     // JSONParser parser = new JSONParser();
	   	ClassLoader classLoader = getClass().getClassLoader();
	   	File file = new File(classLoader.getResource(path).getFile());
	   	ObjectMapper m = new ObjectMapper();
	   	JsonNode rootNode = null;
			try {
				rootNode = m.readTree(file);
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	   	return rootNode;
	}
	
}
