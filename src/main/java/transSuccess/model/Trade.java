package transSuccess.model;

import java.sql.Timestamp;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Trade implements JsonObject {
	private float price;
	private float amount;
	private int tid;
	private TradeType tradeType;
	private Timestamp timestamp;
	private Currency currency;
	
	public Trade(@JsonProperty("price") float price,
				 @JsonProperty("amount")  float amount,
				 @JsonProperty("tid") int tid, 
				 @JsonProperty("type") String tradeType,
				 @JsonProperty("timestamp") Timestamp timestamp){
		this.price = price;
		this.amount = amount;
		this.tid = tid;
		this.timestamp = timestamp;
		if("bid".equals(tradeType))
			this.tradeType = TradeType.buy;
		else if("ask".equals(tradeType))
			this.tradeType = TradeType.sell;
	}
	/*
	private float price;
	private float amount;
	private int tid;
	private TradeType tradeType;
	private Timestamp timestamp;
	
	*/
	public float getPrice() {
		return price;
	}
	public void setPrice(float price) {
		this.price = price;
	}
	public float getAmount() {
		return amount;
	}
	public void setAmount(float amount) {
		this.amount = amount;
	}
	public int getTid() {
		return tid;
	}
	public void setTid(int tid) {
		this.tid = tid;
	}
	public TradeType getTradeType() {
		return tradeType;
	}
	public void setTradeType(TradeType tradeType) {
		this.tradeType = tradeType;
	}
	public Currency getCurrency() {
		return currency;
	}
	public void setCurrency(Currency currency) {
		this.currency = currency;
	}
	public Timestamp getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}
	@Override
	public boolean equals(Object obj){
	       if (!(obj instanceof Trade))
	            return false;
	        if (obj == this)
	            return true;

	        Trade t = (Trade) obj;
	        return new EqualsBuilder().
	            append(tid, t.tid).
	            append(tradeType, t.tradeType).
	            isEquals();
	}
	
    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31). // two randomly chosen prime numbers
            // if deriving: appendSuper(super.hashCode()).
            append(tid).
            append(tradeType).
            toHashCode();
    }
}
