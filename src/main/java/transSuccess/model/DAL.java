package transSuccess.model;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Scanner;

public class DAL {

	public static Connection conn;
	static ArrayList<Integer> stopsIDs;
	static final String[] hours = { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13",
			"14", "15", "16", "17", "18", "19", "20", "21", "22", "23" };
	static final String[] fullHours = { "12:00 AM", "01:00 AM", "02:00 AM", "03:00 AM", "04:00 AM", "05:00 AM",
			"06:00 AM", "07:00 AM", "08:00 AM", "09:00 AM", "10:00 AM", "11:00 AM", "12:00 PM", "01:00 PM", "02:00 PM",
			"03:00 PM", "04:00 PM", "05:00 PM", "06:00 PM", "07:00 PM", "08:00 PM", "09:00 PM", "10:00 PM",
			"11:00 PM", };
	private static HashMap<Integer, String> stopNames = new HashMap<Integer, String>();

	public static void main(String[] args) {
		connectToDb();
		stopsIDs = readstopID();
		HashMap<Integer, int[]> frequencyPerStation = getstopInfoFromDB(stopsIDs);
		//Assigning each station(hour) the ranking position in comparison to the hours in that station
		HashMap<Integer, int[]> frequencyRatingPerStation = getRankedFrequency(frequencyPerStation);
		//Calculating the hourly deltas between hours per station. e.g. between hour 6AM to 7AM there was a rise of 10 busses
		HashMap<Integer, int[]> frequencyDeltasPerStation = getFrequencydeltas(frequencyPerStation);
		writeDataToCSV(frequencyPerStation);
		System.out.println("Finito!");
	}

	/**
	 * Replacing the hourly frequency with a ranked position in comparison to the frequencies during all day in a given station
	 * @param frequencyPerStation
	 * @return
	 */
	private static HashMap<Integer, int[]> getRankedFrequency(HashMap<Integer, int[]> frequencyPerStation) {
		Iterator<Entry<Integer, int[]>> it = frequencyPerStation.entrySet().iterator();
		 HashMap<Integer, int[]> result = new HashMap<>();
		while (it.hasNext()){
			 @SuppressWarnings("unchecked")
			HashMap<Integer, int[]>  stationFreq = ( HashMap<Integer, int[]> ) it.next();
			
		}
		return null;
	}

	private static void writeDataToCSV(HashMap<Integer, int[]> frequencyPerStation) {
		Date date = new Date();
		DateFormat formatter = new SimpleDateFormat("ddMMyy__HH_mm_ss");
		String dateFormatted = formatter.format(date);
		String fileName = "C:\\Users\\shakedk\\Google Drive\\אוניברסיטה\\שנה ד\\פרויקט גמר\\מידע תחבורה ציבורית - פרויקט גמר\\DataSpreads";
		fileName = fileName.concat("\\" + dateFormatted + "_Data.csv");
		try {
			FileWriter writer = new FileWriter(fileName);
			writer.append("FROM/ID,");
			for (Integer stopID : stopNames.keySet()) {
				writer.append(stopID + ",");// + stopNames.get(stopID));
			}
			writer.append("\n");
			for (int i = 0; i < fullHours.length; i++) {
				writer.append("" + fullHours[i] + ",");
				for (int[] frequencies : frequencyPerStation.values()) {
					writer.append(frequencies[i] + ",");
				}
				writer.append("\n");
			}
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static HashMap<Integer, int[]> getstopInfoFromDB(ArrayList<Integer> stopsIDs) {
		HashMap<Integer, int[]> frequencyPerStation = new HashMap<Integer, int[]>();
		String query;
		PreparedStatement ps;
		ResultSet rs;
		int[] frequency;
		for (int j = 0; j < stopsIDs.size(); j++) {
			int stopID = stopsIDs.get(j);
			query = "select stop_name from stops where stop_id =" + stopID;
			try {
				ps = conn.prepareStatement(query);
				rs = ps.executeQuery();
				while (rs.next()) {
					stopNames.put(stopID, rs.getString("STOP_NAME"));
				}
				rs.close();
				ps.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		for (int j = 0; j < stopsIDs.size(); j++) {
			int stopID = stopsIDs.get(j);
			frequency = new int[24];
			for (int i = 0; i < hours.length; i++) {
//				query = "select count(*) from (select route_id,arrival_time,stop_id from stop_times JOIN trips JOIN calendar where calendar.service_id=trips.service_id AND calendar.sunday='TRUE' and stop_id='"+stopID+"' and arrival_time like '"+hours[i]+":%' and stop_times.trip_id=trips.trip_id group by  route_id,arrival_time,stop_id)";
				query = "select count(*) from (select distinct route_id from stop_times JOIN trips JOIN calendar where calendar.service_id=trips.service_id AND calendar.sunday='TRUE' and stop_id='"+stopID+"'and stop_times.trip_id=trips.trip_id group by route_id,arrival_time,stop_id)";

				try {
					ps = conn.prepareStatement(query);
					rs = ps.executeQuery();
					while (rs.next()) {
						String string = rs.getString("count(*)");
						if (string != null) {
							frequency[i] = (frequency[i] + Integer.parseInt(string));
						}
					}
					rs.close();
					ps.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			frequencyPerStation.put(stopID, frequency);
		}
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return frequencyPerStation;

	}


	private static String getRouteForTrip(String tripId) {
		PreparedStatement ps;
		ResultSet rs;
		String result = "";
		String query = "select route_id as RESULT from trips where trip_id = '" + tripId + "'";
		try {
			ps = conn.prepareStatement(query);
			rs = ps.executeQuery();
			while (rs.next()) {
				result = rs.getString("RESULT");
			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}	

	private static ArrayList<Integer> readstopID() {
		System.out.println("Enter the stop IDs for analysis, when finished enter -1");
		ArrayList<Integer> stopsIDs = new ArrayList<Integer>();
		int end = 0;
		try {
			@SuppressWarnings("resource")
			Scanner reader = new Scanner(System.in);
			while (true) {
				end = reader.nextInt();
				if (end == -1) {
					break;
				}
				stopsIDs.add(end);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return stopsIDs;

	}

	private static void connectToDb() {
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

}
