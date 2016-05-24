package transSuccess.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class subIndices {
	private int areaId;
	private int numberOfStopsInArea;
	private  double areaShapeArea;
	private int populationCount;
	private double statisticalAreaFrequencies;
	private double medianIncome;
	private double normalizedMedianIncome;
	private String mainStreets;
	private double safAreaPopulation;
	//STAI = ocioeconomic Transit Availability Index
	private double stai;
	
	public subIndices(){}
	public subIndices(@JsonProperty("areaId") int area_id,
				 @JsonProperty("numberOfStopsInArea") int numberOfStopsInArea,
				 @JsonProperty("areaShapeArea") double areaShapeArea,
				 @JsonProperty("populationCount") int populationCount,
				 @JsonProperty("statisticalAreaFrequencies") double statisticalAreaFrequencies,
				 @JsonProperty("medianIncome") double medianIncome,
				 @JsonProperty("safAreaPopulation") double safAreaPopulation,
				 @JsonProperty("mainStreets") String mainStreets,
				 @JsonProperty("stai") double normalizedMedianIncome,
				@JsonProperty("stai") double stai){
		this.areaId = area_id;		
		this.numberOfStopsInArea = numberOfStopsInArea;
		this.areaShapeArea = areaShapeArea;
		this.populationCount = populationCount;
		this.statisticalAreaFrequencies = statisticalAreaFrequencies;
		this.medianIncome = medianIncome;
		this.mainStreets = mainStreets;
		this.stai = stai;
		this.safAreaPopulation = safAreaPopulation;
		this.normalizedMedianIncome = normalizedMedianIncome;
	}
	public subIndices(String areaID) {
		this.areaId = Integer.parseInt(areaID);
	}
	public int getArea_id() {
		return areaId;
	}
	public void setArea_id(int area_id) {
		this.areaId = area_id;
	}

	public int getNumberOfStopsInArea() {
		return numberOfStopsInArea;
	}
	public void setNumberOfStopsInArea(int numberOfStopsInArea) {
		this.numberOfStopsInArea = numberOfStopsInArea;
	}
	public int getPopulationCount() {
		return populationCount;
	}
	public void setPopulationCount(int populationCount) {
		this.populationCount = populationCount;
	}
	public double getStatisticalAreaFrequencies() {
		return statisticalAreaFrequencies;
	}
	public void setStatisticalAreaFrequencies(double statisticalAreaFrequencies) {
		this.statisticalAreaFrequencies = statisticalAreaFrequencies;
	}
	public String getMainStreets() {
		return mainStreets;
	}
	public void setMainStreets(String mainStreets) {
		this.mainStreets = mainStreets;
	}
	public double getmedianIncome() {
		return medianIncome;
	}
	public void setmedianIncome(double medianIncome) {
		this.medianIncome = medianIncome;
	}
	public double getAreaShapeArea() {
		return areaShapeArea;
	}
	public void setAreaShapeArea(double areaShapeArea) {
		this.areaShapeArea = areaShapeArea;
	}
	public double getStai() {
		return stai;
	}
	public void setStai(double stai) {
		this.stai = stai;
	}
	public double getSafAreaPopulation() {
		return safAreaPopulation;
	}
	public void setSafAreaPopulation(double safAreaPopulation) {
		this.safAreaPopulation = safAreaPopulation;
	}
	public double getNormalizedMedianIncome() {
		return normalizedMedianIncome;
	}
	public void setNormalizedMedianIncome(double normalizedMedianIncome) {
		this.normalizedMedianIncome = normalizedMedianIncome;
	}

}
