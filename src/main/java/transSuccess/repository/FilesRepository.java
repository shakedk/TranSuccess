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
	
	public JsonNode getTelAvivAreas(){
	     // JSONParser parser = new JSONParser();
    	ClassLoader classLoader = getClass().getClassLoader();
    	File file = new File(classLoader.getResource("static/tel_aviv_areas.json").getFile());
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
	            //URL jsonImput =
	            //        resources.class.getResource("icons/printer63.png");
/*	            Object obj = new FileReader(tel_avi "/Users/<username>/Documents/file1.txt");
	 
	            JSONObject jsonObject = (JSONObject) obj;*/

	
}
