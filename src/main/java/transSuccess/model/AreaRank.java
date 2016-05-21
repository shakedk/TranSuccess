package transSuccess.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AreaRank {
	private int AreaID;
	private double rank;
	
	public AreaRank(){}
	public AreaRank(@JsonProperty("AreaID") int AreaID,
				 @JsonProperty("rank") int rank){
		this.AreaID = AreaID;
		this.rank = rank;
	}
	public int getAreaID() {
		return AreaID;
	}
	public void setAreaID(int AreaID) {
		this.AreaID = AreaID;
	}
	public double getRank() {
		return rank;
	}
	public void setRank(double rank) {
		this.rank = rank;
	}

}
