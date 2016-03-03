package transSuccess.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TradeJson implements JsonObject {
/*	@JsonProperty("price")
	private float price;
	@JsonProperty("amount")
	private float amount;
	@JsonProperty("tid")
	private int tid;
	@JsonProperty("type")
	private TradeType tradeType;
	@JsonProperty("timestamp")
	private String timestamp;*/
	@JsonProperty("btc_usd")
	private List<Trade> currency;
	/*
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


	}
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}*/
	public void setCurrency(List currency) {
		this.currency = currency;
		}
	public List getCurrency() {
		return currency;
	}
}
