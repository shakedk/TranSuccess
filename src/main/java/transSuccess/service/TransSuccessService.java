package transSuccess.service;

import java.io.IOException;
import java.util.List;

import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import transSuccess.model.AreaRank;
import transSuccess.repository.FilesRepository;


@Service
public class TransSuccessService {

	@Autowired
	FilesRepository filesRepository;
	
	public JsonNode getTelAvivAreas() throws JsonParseException, JsonMappingException, IOException{
		return modifyAreaIndices(filesRepository.getTelAvivAreas());
	}
	public JsonNode getAreasRanks() throws JsonParseException, JsonMappingException, IOException{
		return filesRepository.getAreaRanks();
	}
	public JsonNode getSubIndices()  throws JsonParseException, JsonMappingException, IOException{
		return filesRepository.getSubIndices();
	}
	
	private JsonNode modifyAreaIndices(JsonNode file) throws JsonParseException, JsonMappingException, IOException{
		ObjectMapper m = new ObjectMapper();
		JsonNode jsonAreasRanks = getAreasRanks();
		List<AreaRank> areaRanks = m.readValue(jsonAreasRanks.toString(), new TypeReference<List<AreaRank>>(){});
		
		FeatureCollection featureCollection = m.readValue(file.toString(), FeatureCollection.class);
		List<Feature> features = featureCollection.getFeatures();
		for(Feature feature : features){
			String desc = feature.getProperty("Description");
			int idIndex = desc.indexOf("ms_ezor")+10;
			String id = desc.substring(idIndex,idIndex+3);
			for(AreaRank areaRank : areaRanks){
				int rankId = areaRank.getAreaID();
				if(Integer.parseInt(id)==rankId){
					double rank = areaRank.getRank();
					feature.setProperty("styleHash", rank);
					feature.setProperty("Name", id);
				}
			}
			
		}
		JsonNode node = m.convertValue(featureCollection, JsonNode.class);
		return node;
	}
	
	public JsonNode getTelAvivStations(){
		return filesRepository.getTelAvivStations();
	}
	public String getsubIndicesDataForJSON() {
		return filesRepository.getsubIndicesDataForJSON();
	}
	
	
}
