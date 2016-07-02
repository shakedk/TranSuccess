package transSuccess.repository;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


@Repository
public class FilesRepository {

	public FilesRepository() {
	}

	private static final String AREAS_PATH = "static/tel_aviv_areas.json";
	private static final String STATION_PATH = "static/stations.geojson";
	private static final String AREA_RANKS_PATH = "static/AreaRanks.json";
	private static final String AREA_SUB_INDICES = "static/areaProperties.json";

	public JsonNode getTelAvivAreas() {
		return getFileFromPath(AREAS_PATH);
	}

	public JsonNode getTelAvivStations() {
		return getFileFromPath(STATION_PATH);
	}

	public JsonNode getAreaRanks() {
		return getFileFromPath(AREA_RANKS_PATH);
	}

	public JsonNode getAreasPropertiesForPcChart() {
		return getFileFromPath(AREA_SUB_INDICES);
		
	}

	private JsonNode getFileFromPath(String path) {
		Resource resource = new ClassPathResource(path);
		ObjectMapper m = new ObjectMapper();
		JsonNode rootNode = null;
		try {
			rootNode = m.readTree(resource.getFile());
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rootNode;
	}


	@SuppressWarnings("resource")
	private String readFile(String path) {

		String result = "";
			
		ClassLoader classLoader = getClass().getClassLoader();
		try {
		    result = IOUtils.toString (classLoader.getResourceAsStream(path));
		} catch (IOException e) {
			e.printStackTrace();
		}
			
		return result;
	  }

}
