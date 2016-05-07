package transSuccess.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class subIndices {
	private int areaId;
	private int numberOfStopsInArea;
	private  double areaShapeArea;
	private int populationCount;
	private double statisticalAreaFrequencies;
	private double areaMedianIncome;
	private double socialEconomicIndex;
	//STAI = ocioeconomic Transit Availability Index
	private double stai;
	
	public subIndices(){}
	public subIndices(@JsonProperty("areaId") int area_id,
				 @JsonProperty("numberOfStopsInArea") int numberOfStopsInArea,
				 @JsonProperty("areaShapeArea") double areaShapeArea,
				 @JsonProperty("populationCount") int populationCount,
				 @JsonProperty("statisticalAreaFrequencies") double statisticalAreaFrequencies,
				 @JsonProperty("areaMedianIncome") double areaMedianIncome,
				 @JsonProperty("socialEconomicIndex") double socialEconomicIndex,
				@JsonProperty("stai") double stai){
		this.areaId = area_id;		
		this.numberOfStopsInArea = numberOfStopsInArea;
		this.areaShapeArea = areaShapeArea;
		this.populationCount = populationCount;
		this.statisticalAreaFrequencies = statisticalAreaFrequencies;
		this.areaMedianIncome = areaMedianIncome;
		this.socialEconomicIndex = socialEconomicIndex;
		this.stai = stai;
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
	public double getSocialEconomicIndex() {
		return socialEconomicIndex;
	}
	public void setSocialEconomicIndex(double socialEconomicIndex) {
		this.socialEconomicIndex = socialEconomicIndex;
	}
	public double getAreaMedianIncome() {
		return areaMedianIncome;
	}
	public void setAreaMedianIncome(double areaMedianIncome) {
		this.areaMedianIncome = areaMedianIncome;
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

}
