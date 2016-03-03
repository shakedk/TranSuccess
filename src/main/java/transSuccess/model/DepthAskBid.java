package transSuccess.model;

import java.sql.Timestamp;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DepthAskBid implements JsonObject {
	@JsonProperty("asks") 
	private List<String> askPrices;
	@JsonProperty("bids") 
	private List<String> bidPrices;
/*	public DepthAskBid(){}
	public DepthAskBid(List<String> askPrices, List<String> bidPrices){
		this.setAskPrices(askPrices);
		this.setBidPrices(bidPrices);
	}*/
	public List<String> getAskPrices() {
		return askPrices;
	}
	public List<String> getBidPrices() {
		return bidPrices;
	}
	
	public void setAskPrices(List<String> askPrices) {
		this.askPrices = askPrices;
	}
	public void setBidPrices(List<String> bidPrices) {
		this.bidPrices = bidPrices;
	}
}
