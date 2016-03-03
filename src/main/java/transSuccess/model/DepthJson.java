package transSuccess.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DepthJson implements JsonObject {
	@JsonProperty("btc_usd")
	private List<DepthAskBid> currency;

	public void setCurrency(List<DepthAskBid> currency) {
		this.currency = currency;
		}
	public List<DepthAskBid> getCurrency() {
		return currency;
	}
}
