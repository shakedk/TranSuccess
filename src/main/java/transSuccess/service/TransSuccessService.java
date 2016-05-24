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
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import transSuccess.model.AreaRank;
import transSuccess.model.subIndices;
import transSuccess.repository.FilesRepository;

@Service
public class TransSuccessService {

	public static Connection conn;
	static final String[] hours = { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13",
			"14", "15", "16", "17", "18", "19", "20", "21", "22", "23" };

	@Autowired
	FilesRepository filesRepository;

	public JsonNode getTelAvivAreas(int startHour, int endHour)
			throws JsonParseException, JsonMappingException, IOException {
		return updateTelAvivAerasWithData(filesRepository.getTelAvivAreas(), startHour, endHour);

	}

	public JsonNode getAreasRanks() throws JsonParseException, JsonMappingException, IOException {
		return filesRepository.getAreaRanks();
	}

	public JsonNode getSubIndices() throws JsonParseException, JsonMappingException, IOException {
		return filesRepository.getSubIndices();
	}

	private JsonNode updateTelAvivAerasWithData(JsonNode file, int startHour, int endHour)
			throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper m = new ObjectMapper();
		JsonNode jsonAreasRanks = getAreasRanks();
		List<AreaRank> areaRanks = m.readValue(jsonAreasRanks.toString(), new TypeReference<List<AreaRank>>() {
		});
		FeatureCollection featureCollection = m.readValue(file.toString(), FeatureCollection.class);
		List<Feature> features = featureCollection.getFeatures();
		Map<String, subIndices> areasData = calculateSubIndices(startHour, endHour);
		for (Feature feature : features) {
			String desc = feature.getProperty("Description");
			int idIndex = desc.indexOf("ms_ezor") + 10;
			String id = desc.substring(idIndex, idIndex + 3);
			//Calculate the needed values			
			for (String areaID : areasData.keySet()) {
//			for (AreaRank areaRank : areaRanks) {
				subIndices area = areasData.get(areaID);
//				int rankId = areaRank.getAreaID();
//				if (Integer.parseInt(id) == rankId) {
				if (id.equals(areaID)){
//					double stai = areaRank.getRank();
					//TODO: Insert explanation about formula
					double stai = area.getNormalizedMedianIncome() * 10 * 0.5 + area.getSafAreaPopulationScaled1to10() * 0.5;
					// STAI smoothed out using log & scaling to 1-10
					if (stai <= 0){
						stai = -1;						
					} else {
						stai = Math.log10(stai);
						stai = stai * 10;
					}
					feature.setProperty("styleHash", Math.floor(stai));
					feature.setProperty("Name", id);
				}
			}

		}
		JsonNode node = m.convertValue(featureCollection, JsonNode.class);
		return node;
	}

	public JsonNode getTelAvivStations() {
		return filesRepository.getTelAvivStations();
	}

	public String getsubIndicesDataForJSON() {
		return filesRepository.getsubIndicesDataForJSON();
	}

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

	@SuppressWarnings("finally")
	/**
	 * Updates the hourly frequency (num of buses that pass every hour in each
	 * station)
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
	 * Median Income to be later combined into STAI on client side
	 * 
	 * @param startHour
	 * @param endHour
	 * @return 
	 */
	public static Map<String, subIndices> calculateSubIndices(int startHour, int endHour) {

		ArrayList<String> areasIDs = getAreasIDs();
		Map<String, subIndices> areasData = new HashMap<String, subIndices>();
		// Creating the areas
		for (String areaID : areasIDs) {
			areasData.put(areaID, new subIndices(areaID));
		}
		calculateAverageFrequncyForEachArea(areasData, startHour, endHour);
		getAreasData(areasData);
		calculateAccessabilityIndex(areasData);
		calculatingNormalizedMediaIncome(areasData);
		calculatingSacledSaf(areasData);
		return areasData;

	}

	private static void calculatingNormalizedMediaIncome(Map<String, subIndices> areasData) {
		double maxMedianIncome = 0;
		double medianIncome;
		subIndices area;
		// Getting the maximum median income
		for (String areaID : areasData.keySet()) {
			area = areasData.get(areaID);
			medianIncome = area.getmedianIncome();
			if (medianIncome > maxMedianIncome) {
				maxMedianIncome = medianIncome;
			}
		}
		// calculating normalized median income
		for (String areaID : areasData.keySet()) {
			area = areasData.get(areaID);
			area.setNormalizedMedianIncome(area.getmedianIncome() / maxMedianIncome);
		}
	}

	private static void calculatingSacledSaf(Map<String, subIndices> areasData) {
		double maxSaf = 0;
		double minSaf = Integer.MAX_VALUE;
		double saf;
		subIndices area;
		// Getting the minimum & maximum median income
		for (String areaID : areasData.keySet()) {
			area = areasData.get(areaID);
			saf = area.getSafAreaPopulation();
			if (saf > maxSaf) {
				maxSaf = saf;
			}
			if (saf < minSaf){
				minSaf = saf;
			}
		}
		// calculating the scaled value
		for (String areaID : areasData.keySet()) {
			area = areasData.get(areaID);
			saf = area.getSafAreaPopulation();
			area.setSafAreaPopulationScaled1to10((((10-1)*(saf-minSaf))/(maxSaf-minSaf))+1);			
		}
	}
	
	private static void calculateAccessabilityIndex(Map<String, subIndices> areasData) {
		for (String areaID : areasData.keySet()) {
			subIndices area = areasData.get(areaID);
			// SAF/Area/Population (pop>1000)
			int populationCount = area.getPopulationCount();
			if (populationCount >= 1000) {
				area.setSafAreaPopulation(
						area.getStatisticalAreaFrequencies() / area.getAreaShapeArea() / (populationCount*0.001));
			} else {
				area.setSafAreaPopulation(0);
			}
		}
	}

	private static void getAreasData(Map<String, subIndices> areasData) {
		connectToDb();
		PreparedStatement ps;
		ResultSet rs;
		String query;
		try {
			query = "select * from areas";
			ps = conn.prepareStatement(query);
			rs = ps.executeQuery();
			while (rs.next()) {
				subIndices area = areasData.get(rs.getString("area_id"));
				area.setmedianIncome(rs.getDouble("median_income"));
				area.setAreaShapeArea(rs.getDouble("area_size"));
				area.setPopulationCount(rs.getInt("population"));
				area.setMainStreets(rs.getString("main_streets"));
			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		closeConnection();
	}

	private static void calculateAverageFrequncyForEachArea(Map<String, subIndices> areas, int startHour, int endHour) {
		Map<String, Double> stopAvgFrequencies = calculateStopAvgFrequencies(startHour, endHour);
		calculateAreaAvgFreq(areas, stopAvgFrequencies);
	}

	/**
	 * Summing the average rides frequencies for each area
	 * 
	 * @param areas
	 * @param stopAvgFrequencies
	 */
	private static void calculateAreaAvgFreq(Map<String, subIndices> areas, Map<String, Double> stopAvgFrequencies) {
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
					avgFreqOfArea += stopAvgFrequencies.get(stopID);
				}
				rs.close();
				ps.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			subIndices area = areas.get(areaID);
			// Save the average frequency for the area
			area.setStatisticalAreaFrequencies(avgFreqOfArea / numOfStopsInArea);
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
	 * Calculating the average frequency for each stop in Tel Aviv
	 * SQL method
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
		query += ")/" + numberOfHours + " AS Hours from STOPHOURLYFREQUENCIES";
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
