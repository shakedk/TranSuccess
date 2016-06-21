package transSuccess.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AreaProperty {
	private int areaID;
	private int numberOfStopsInArea;
	private  double shapeArea;
	private int population;
	private double areasAverageFrequencies;
	private double medianIncome;
	private double normalizedMedianIncome;
	// Transit Accessibility Index
	private double tai;
	private double sacaled1To10Tai;
	
	
	public AreaProperty(){}
	public AreaProperty(@JsonProperty("areaID") int areaID,
				 @JsonProperty("numberOfStopsInArea") int numberOfStopsInArea,
				 @JsonProperty("shapeArea") double shapeArea,
				 @JsonProperty("population") int population,
				 @JsonProperty("areasAverageFrequencies") double areasAverageFrequencies,
				 @JsonProperty("medianIncome") double medianIncome,
				 @JsonProperty("tai") double tai){
		this.areaID = areaID;		
		this.numberOfStopsInArea = numberOfStopsInArea;
		this.shapeArea = shapeArea;
		this.population = population;
		this.areasAverageFrequencies = areasAverageFrequencies;
		this.medianIncome = medianIncome;
		this.tai = tai; 
	}
	public AreaProperty(String areaID) {
		this.areaID = Integer.parseInt(areaID);
	}
	public int getAreaID() {
		return areaID;
	}
	public void setAreaID(int area_id) {
		this.areaID = area_id;
	}

	public int getNumberOfStopsInArea() {
		return numberOfStopsInArea;
	}
	public void setNumberOfStopsInArea(int numberOfStopsInArea) {
		this.numberOfStopsInArea = numberOfStopsInArea;
	}
	public int getPopulation() {
		return population;
	}
	public void setPopulation(int population) {
		this.population = population;
	}
	public double getAreasAverageFrequencies() {
		return areasAverageFrequencies;
	}
	public void setAreasAverageFrequencies(double areasAverageFrequencies) {
		this.areasAverageFrequencies = areasAverageFrequencies;
	}
	public double getMedianIncome() {
		return medianIncome;
	}
	public void setmedianIncome(double medianIncome) {
		this.medianIncome = medianIncome;
	}
	public double getShapeArea() {
		return shapeArea;
	}
	public void setShapeArea(double areaShapeArea) {
		this.shapeArea = areaShapeArea;
	}

	public double getTai() {
		return tai;
	}
	public void setTai(double safAreaPopulation) {
		this.tai = safAreaPopulation;
	}
	public double getNormalizedMedianIncome() {
		return normalizedMedianIncome;
	}
	public void setNormalizedMedianIncome(double normalizedMedianIncome) {
		this.normalizedMedianIncome = normalizedMedianIncome;
	}
	public double getSacaled1To10Tai() {
		return sacaled1To10Tai;
	}
	public void setSacaled1To10Tai(double sacaled1To10Tai) {
		this.sacaled1To10Tai = sacaled1To10Tai;
	}
	
	public String toString() {
		return ""+getAreaID();
		
	}

}
