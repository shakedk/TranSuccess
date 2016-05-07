package transSuccess.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AreaRank {
	private int areaId;
	private double rank;
	
	public AreaRank(){}
	public AreaRank(@JsonProperty("areaId") int area_id,
				 @JsonProperty("rank") int rank){
		this.areaId = area_id;
		this.rank = rank;
	}
	public int getArea_id() {
		return areaId;
	}
	public void setArea_id(int area_id) {
		this.areaId = area_id;
	}
	public double getRank() {
		return rank;
	}
	public void setRank(double rank) {
		this.rank = rank;
	}

}
