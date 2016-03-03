package transSuccess.model;

import java.sql.Timestamp;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Depth implements JsonObject {
	private float price;
	private float amount;
	private TradeType tradeType;
	private Currency currency;
	
	public Depth(){}
	public Depth(@JsonProperty("price") float price,
				 @JsonProperty("amount")  float amount){
		this.price = price;
		this.amount = amount;
	}

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

	public TradeType getTradeType() {
		return tradeType;
	}
	public void setTradeType(String tradeType) {
	if("bids".equals(tradeType))
		this.tradeType = TradeType.buy;
	else if("asks".equals(tradeType))
		this.tradeType = TradeType.sell;
	}
	public Currency getCurrency() {
		return currency;
	}
	public void setCurrency(Currency currency) {
		this.currency = currency;
	}

	@Override
	public boolean equals(Object obj){
	       if (!(obj instanceof Depth))
	            return false;
	        if (obj == this)
	            return true;

	        Depth t = (Depth) obj;
	        return new EqualsBuilder().
	        	append(price,t.price).
	        	append(amount,t.amount).
	            append(tradeType, t.tradeType).
	            isEquals();
	}
	
    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31). // two randomly chosen prime numbers
            // if deriving: appendSuper(super.hashCode()).
        	append(price).
        	append(amount).
            append(tradeType).
            toHashCode();
    }
}
