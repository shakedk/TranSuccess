package transSuccess.service;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
		return modifyRanks(filesRepository.getTelAvivAreas());
	}
	public JsonNode getAreasRanks() throws JsonParseException, JsonMappingException, IOException{
		return filesRepository.getAreaRanks();
	}
	private JsonNode modifyRanks(JsonNode file) throws JsonParseException, JsonMappingException, IOException{
		ObjectMapper m = new ObjectMapper();
		JsonNode jsonAreasRanks = getAreasRanks();
		//List<AreaRank> areaRanks = m.readValue(file.toString(), AreaRank.class);
		List<AreaRank> areaRanks = m.readValue(jsonAreasRanks.toString(), new TypeReference<List<AreaRank>>(){});
		
		FeatureCollection featureCollection = m.readValue(file.toString(), FeatureCollection.class);
		List<Feature> features = featureCollection.getFeatures();
		for(Feature feature : features){
			String desc = feature.getProperty("Description");
			String id = desc.substring(desc.indexOf("ms_ezor")+11,14);
			for(AreaRank areaRank : areaRanks){
				int rankId = areaRank.getArea_id();
				if(Integer.parseInt(id)==rankId){
					double rank = areaRank.getRank();
					feature.setProperty("styleHash", rank);
				}
			}
			
		}
/*		for(int i=0; i < features.size() ; i++){
			//Feature feature = features.get(i);
			//Feature feature = features.getProperty("Description");
			AreaRank areaRank = areaRanks.get(i);
			double rank = areaRank.getRank();
			feature.setProperty("styleHash", rank);
		}*/
		JsonNode node = m.convertValue(featureCollection, JsonNode.class);

		return node;
	}
	
	public JsonNode getTelAvivStations(){
		return filesRepository.getTelAvivStations();
	}
	
}
