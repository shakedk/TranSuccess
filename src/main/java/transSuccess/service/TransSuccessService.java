package transSuccess.service;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;

import transSuccess.model.AreaProperty;
import transSuccess.repository.FilesRepository;

@Service
public class TransSuccessService {

	public static Connection conn;
	static final String[] hours = { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13",
			"14", "15", "16", "17", "18", "19", "20", "21", "22", "23" };

	private boolean isAreasDataUpdated = false;
	private static Map<String, AreaProperty> areasData;

	@Autowired
	FilesRepository filesRepository;

	// public String getsubIndicesDataForJSON() {
	// return filesRepository.getsubIndicesDataForJSON();
	// }
	//
	public JsonNode getTelAvivAreasForMap(int startHour, int endHour)
			throws JsonParseException, JsonMappingException, IOException {
		return updateAreasInMapProperties(filesRepository.getTelAvivAreas(), startHour, endHour);
	}

	public JsonNode getAreasRanks() throws JsonParseException, JsonMappingException, IOException {
		return filesRepository.getAreaRanks();
	}

	public JsonNode getAreasPropertiesForPcChart()
			throws JsonParseException, JsonMappingException, IOException {
		return updateAreasInPCProperties(filesRepository.getAreasPropertiesForPcChart());
	}
	
	private class areaSerializerForPC extends JsonSerializer<AreaProperty> {
	    @Override
	    public void serialize(AreaProperty value, JsonGenerator jgen, SerializerProvider provider) 
	      throws IOException, JsonProcessingException {
	        jgen.writeStartObject();
	        jgen.writeNumberField("areaID", value.getAreaID());
	        jgen.writeNumberField("numberOfStopsInArea", +value.getNumberOfStopsInArea());
	        jgen.writeNumberField("shapeArea", value.getShapeArea());
	        jgen.writeNumberField("population", value.getPopulation());
	        jgen.writeNumberField("averageFrequencies",value.getAreasAverageFrequencies());
	        jgen.writeNumberField("medianIncome",value.getMedianIncome());
	        jgen.writeNumberField("tai",value.getTai());
	        jgen.writeEndObject();
	    }
	}

	@SuppressWarnings("unchecked")
	private JsonNode updateAreasInPCProperties(JsonNode jsonFile)
			throws JsonParseException, JsonMappingException, IOException {
		JsonNode node = null;
		//Waiting for the other function to update the areasData
			Resource resource = new ClassPathResource("static/areaProperties.json");
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
		
//		while (!isAreasDataUpdated) {}
//			ObjectMapper mapper = new ObjectMapper();
//			SimpleModule module = new SimpleModule();
//			module.addSerializer(AreaProperty.class, new areaSerializerForPC());
//			mapper.registerModule(module);
//			if (areasData!= null){
//				List<AreaProperty> areasDList = new ArrayList<AreaProperty>();
//				for (String areadID : areasData.keySet()) {
//					areasDList.add(areasData.get(areadID));					
//				}
//			node = mapper.valueToTree(areasDList);
//			
//			}
			// Invalidadting isAreasDataUpdated so next it will be udpated
			// accordingly
//			isAreasDataUpdated = false;

		
//		return node;
	}

	private JsonNode updateAreasInMapProperties(JsonNode file, int startHour, int endHour)
			throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper m = new ObjectMapper();
		FeatureCollection featureCollection = m.readValue(file.toString(), FeatureCollection.class);
		List<Feature> features = featureCollection.getFeatures();
		areasData = calculateAreasProperties(startHour, endHour);
		isAreasDataUpdated = true;
		// breakPoints = {safFirstBP,safSecondBP, incomeFirstBP,
		// incomeSecondBP};
		// double[] valuesBreakPoints = getValuesBreakPoints(areasData);
		for (Feature feature : features) {
			String desc = feature.getProperty("Description");
			int idIndex = desc.indexOf("ms_ezor") + 10;
			String id = desc.substring(idIndex, idIndex + 3);

			// Calculate the needed values
			for (String areaID : areasData.keySet()) {
				AreaProperty area = areasData.get(areaID);
				if (id.equals(areaID)) {
					// TODO: Insert explanation about formula
					double tai = area.getSafAreaPopulationScaled1to10();
					feature.setProperty("styleHash", Math.floor(tai));

					feature.setProperty("Name", id);
				}
			}

		}
		JsonNode node = m.convertValue(featureCollection, JsonNode.class);
		return node;
	}

	// public JsonNode getTelAvivStations() {
	// return filesRepository.getTelAvivStations();
	// }

	public static ArrayList<String> getTelAvivStopIDs() {
		connectToDb();
		PreparedStatement ps;
		ResultSet rs;
		ArrayList<String> stopIDs = new ArrayList<String>();
		String query = "select stop_id from stops where stop_desc like '%עיר: תל אביב יפו%'";
		try {
			ps = conn.prepareStatement(query);
			rs = ps.executeQuery();
			while (rs.next()) {
				stopIDs.add(rs.getString("stop_id"));
			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		closeConnection();
		return stopIDs;
	}

	/**
	 * Updates the hourly frequency (num of buses that pass every hour in each
	 * station) into DB - stophourlyfrequencies ;; This is only for DB populate
	 * use!!!
	 * 
	 * @param stopIDs
	 * @return
	 */
	public static ArrayList<String> updateStopsFreqs(ArrayList<String> stopIDs) {
		connectToDb();
		PreparedStatement ps = null;
		String query;
		// This will only work if the stopIDs were populated into the table
		// first!!! Use CSV load
		// ITerating over 24 hours, for each hour iterating over all the stops
		for (int i = 0; i < hours.length; i++) {
			query = "update stophourlyfrequencies set hr" + hours[i]
					+ " = (select count(*) from (select route_id,arrival_time,stop_id from stop_times JOIN trips JOIN calendar where calendar.service_id=trips.service_id AND calendar.sunday='TRUE' and stop_id="
					+ "(?) and arrival_time like '" + hours[i]
					+ ":%' and stop_times.trip_id=trips.trip_id group by route_id,arrival_time,stop_id)) where stop_id="
					+ "(?)";
			try {
				ps = conn.prepareStatement(query);
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			for (String stopID : stopIDs) {

				try {
					ps.setString(1, stopID);
					ps.setString(2, stopID);
					ps.addBatch();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			try {
				int[] executeBatch = ps.executeBatch();
				ps.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		closeConnection();
		return stopIDs;
	}

	/**
	 * Calculating Accessibility Index: SAF / Area / Population and Normlaized
	 * Median Income
	 * 
	 * @param startHour
	 * @param endHour
	 * @return
	 */
	public static Map<String, AreaProperty> calculateAreasProperties(int startHour, int endHour) {

		ArrayList<String> areasIDs = getAreasIDs();
		Map<String, AreaProperty> areasData = new HashMap<String, AreaProperty>();
		// Creating the areas
		for (String areaID : areasIDs) {
			areasData.put(areaID, new AreaProperty(areaID));
		}
		calculateAverageFrequncyForEachArea(areasData, startHour, endHour);
		getAreasData(areasData);
		calculateAccessabilityIndex(areasData);
		calculatingNormalizedMediaIncome(areasData);
		calculatingSacledSaf(areasData);
		return areasData;

	}

	private static void calculatingNormalizedMediaIncome(Map<String, AreaProperty> areasData) {
		double maxMedianIncome = 0;
		double medianIncome;
		AreaProperty area;
		// Getting the maximum median income
		for (String areaID : areasData.keySet()) {
			area = areasData.get(areaID);
			medianIncome = area.getMedianIncome();
			if (medianIncome > maxMedianIncome) {
				maxMedianIncome = medianIncome;
			}
		}
		// calculating normalized median income
		for (String areaID : areasData.keySet()) {
			area = areasData.get(areaID);
			area.setNormalizedMedianIncome(area.getMedianIncome() / maxMedianIncome);
		}
	}

	private static void calculatingSacledSaf(Map<String, AreaProperty> areasData) {
		double maxSaf = 0;
		double minSaf = Integer.MAX_VALUE;
		double saf;
		AreaProperty area;
		// Getting the minimum & maximum median income
		for (String areaID : areasData.keySet()) {
			area = areasData.get(areaID);
			saf = area.getTai();
			if (saf > maxSaf) {
				maxSaf = saf;
			}
			// Emitting the 0 values
			if (saf < minSaf && saf > 0) {
				minSaf = saf;
			}
		}
		double safAreaPopulationScaled1to10;
		// calculating the scaled value
		for (String areaID : areasData.keySet()) {

			area = areasData.get(areaID);
			saf = area.getTai();
			if (saf > 0) {
				// double safAreaPopulationScaled1to10 = (((10 - 1) * (saf -
				// minSaf)) / (maxSaf - minSaf)) + 1;
				safAreaPopulationScaled1to10 = ((log(saf) - log(minSaf)) / (log(maxSaf) - log(minSaf))) * 9;
				area.setSafAreaPopulationScaled1to10(safAreaPopulationScaled1to10);
			} else {
				area.setSafAreaPopulationScaled1to10(0);
			}
		}
	}

	private static double log(double num) {
		return Math.log10(num);
	}

	private static void calculateAccessabilityIndex(Map<String, AreaProperty> areasData) {
		for (String areaID : areasData.keySet()) {
			AreaProperty area = areasData.get(areaID);
			// SAF/Area/Population (pop>1000)
			int populationCount = area.getPopulation();
			if (populationCount >= 1000) {
				double safAreaPopulation = area.getAreasAverageFrequencies() / area.getShapeArea()
						/ (populationCount * 0.001);
				area.setTai(safAreaPopulation);
			} else {
				area.setTai(0);
			}
		}
	}

	private static void getAreasData(Map<String, AreaProperty> areasData) {
		connectToDb();
		PreparedStatement ps;
		ResultSet rs;
		String query;
		try {
			query = "select * from areas";
			ps = conn.prepareStatement(query);
			rs = ps.executeQuery();
			while (rs.next()) {
				AreaProperty area = areasData.get(rs.getString("area_id"));
				area.setmedianIncome(rs.getDouble("median_income"));
				area.setShapeArea(rs.getDouble("area_size"));
				area.setPopulation(rs.getInt("population"));
			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		closeConnection();
	}

	private static void calculateAverageFrequncyForEachArea(Map<String, AreaProperty> areas, int startHour,
			int endHour) {
		Map<String, Double> stopAvgFrequencies = calculateStopAvgFrequencies(startHour, endHour);
		calculateAreaAvgFreq(areas, stopAvgFrequencies);
	}

	/**
	 * Summing the average rides frequencies for each area
	 * 
	 * @param areas
	 * @param stopAvgFrequencies
	 */
	private static void calculateAreaAvgFreq(Map<String, AreaProperty> areas, Map<String, Double> stopAvgFrequencies) {
		connectToDb();
		PreparedStatement ps;
		ResultSet rs;
		String query;
		double avgFreqOfArea;
		int numOfStopsInArea;
		String stopID;
		for (String areaID : areas.keySet()) {
			query = "select stop_id from stops_areas where area_id=" + areaID;
			avgFreqOfArea = 0;
			numOfStopsInArea = 0;
			try {
				ps = conn.prepareStatement(query);
				rs = ps.executeQuery();
				// Summing up all the stop averages
				while (rs.next()) {
					numOfStopsInArea++;
					stopID = rs.getString("stop_id");
					if (stopAvgFrequencies != null) {
						avgFreqOfArea += stopAvgFrequencies.get(stopID);
					}
				}
				rs.close();
				ps.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			AreaProperty area = areas.get(areaID);
			// Save the average frequency for the area
			if (numOfStopsInArea > 0) {
				area.setAreasAverageFrequencies(avgFreqOfArea / numOfStopsInArea);
			} else {
				area.setAreasAverageFrequencies(0);
			}
			// Save the area's number of stops
			area.setNumberOfStopsInArea(numOfStopsInArea);
		}
		closeConnection();
	}

	private static ArrayList<String> getAreasIDs() {
		connectToDb();
		ArrayList<String> areasIDs = new ArrayList<String>();
		PreparedStatement ps;
		ResultSet rs;
		String query = "select area_id from areas";
		try {
			ps = conn.prepareStatement(query);
			rs = ps.executeQuery();
			while (rs.next()) {
				areasIDs.add(rs.getString("area_id"));
			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		closeConnection();
		return areasIDs;
	}

	/**
	 * Calculating the average frequency for each stop in Tel Aviv SQL method
	 * 
	 * @param startHour
	 * @param endHour
	 * @return
	 */
	@SuppressWarnings("finally")
	private static Map<String, Double> calculateStopAvgFrequencies(int startHour, int endHour) {
		connectToDb();
		PreparedStatement ps;
		ResultSet rs;
		Map<String, Double> stopsAvgFrequencies = new HashMap<String, Double>();
		// Building the query with start and end hours
		int numberOfHours = endHour - startHour;
		String query = "select stop_id,(";
		for (int i = 0; i < numberOfHours; i++) {
			query += "+hr" + (hours[startHour + i]);
		}
		// Converting the hours to double, so the DB returns double values and
		// not ints
		query += ")/" + (double) numberOfHours + " AS Hours from STOPHOURLYFREQUENCIES";
		try {
			ps = conn.prepareStatement(query);
			rs = ps.executeQuery();
			while (rs.next()) {
				stopsAvgFrequencies.put(rs.getString("stop_id"), rs.getDouble("Hours"));
			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			closeConnection();
			return stopsAvgFrequencies;
		}
	}

	public static void connectToDb() {
		// TODO Auto-generated method stub
		try {
			Class.forName("org.h2.Driver");
			conn = DriverManager.getConnection("jdbc:h2:~/gtfs", "sa", "");
			// add application code here

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void closeConnection() {
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
