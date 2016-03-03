package transSuccess.service;

import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.sf.json.JSONObject;
import transSuccess.model.Currency;
import transSuccess.model.Depth;
import transSuccess.model.DepthAskBid;
import transSuccess.model.DepthJson;
import transSuccess.model.Trade;
import transSuccess.model.TradeJson;

@Service
public class PublicApiService {
	private static final String DOMAIN = "http://btc-e.com";
	private static final String INFO_PATH = "/api/3/info/";
	private static final String TICKER_PATH = "/api/3/ticker/";
	private static final String DEPTH_PATH = "/api/3/depth/";
	private static final String TRADE_PATH = "/api/3/trades/";
	private static final String CURRENCY_BTC_USD = "btc_usd";
	private static final String LIMIT_2000 = "?limit=2000";
	private final String USER_AGENT = "Mozilla/5.0";
	private HashSet<Trade> trades;
	
	public void getPublicTrade(){
		trades = new HashSet<>();
		try {
			String tradeJson = 	sendGet(TRADE_PATH,CURRENCY_BTC_USD);
			mapTradeObject(tradeJson);
			String depthJson = 	sendGet(DEPTH_PATH,CURRENCY_BTC_USD);
			printDepthObject(depthJson);
			printTradeObject(tradeJson);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("error while GET request");
		}
	}
	private void mapTradeObject(String json){
		JsonNode arrNode = null;
		try {
			arrNode = (JsonNode) new ObjectMapper().readTree(json).get(Currency.btc_usd.toString());
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (arrNode.isArray()) {
		    for (final JsonNode objNode : arrNode) {
		    	
		        System.out.println(objNode);
		    }
		}

/*		List<Trade> currancy = json.getCurrency();
		Iterator<Trade> iter = currancy.iterator();
		while(iter.hasNext()){
			trades.add((Trade)iter.next());
		}*/
	}
	private static HttpClient getHttpClient() {

	    try {
	        SSLContext sslContext = SSLContext.getInstance("SSL");

	        sslContext.init(null,
	                new TrustManager[]{new X509TrustManager() {
	                    public X509Certificate[] getAcceptedIssuers() {

	                        return null;
	                    }

	                    public void checkClientTrusted(
	                            X509Certificate[] certs, String authType) {

	                    }

	                    public void checkServerTrusted(
	                            X509Certificate[] certs, String authType) {

	                    }
	               	 }}, new SecureRandom());

	        SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext,SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);



	        HttpClient httpClient = HttpClientBuilder.create().setSSLSocketFactory(socketFactory).build();

	        return httpClient;

	    } catch (Exception e) {
	        e.printStackTrace();
	        return HttpClientBuilder.create().build();
	    }
	}
	// HTTP GET request
	private String sendGet(String method,String currency) throws Exception {

		String url = DOMAIN+method+currency;
		HttpClient client = getHttpClient();
		HttpGet request = new HttpGet(url);

		// add request header

		request.addHeader("User-Agent", USER_AGENT);

		HttpResponse response = client.execute(request);

		System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + 
                       response.getStatusLine().getStatusCode());
		JSONObject myObject = new JSONObject();
		//myObject.j

		String jsonString = EntityUtils.toString(response.getEntity());


		return jsonString;
	}
	private void printTradeObject(String jsonString) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		TradeJson tradeJson = mapper.readValue(jsonString, TradeJson.class);
        System.out.println(tradeJson.toString());

		List<Trade> tradesList = tradeJson.getCurrency();
		for(Trade t : tradesList){
			System.out.println(t.getTid());
			System.out.println(t.getAmount());
			System.out.println(t.getPrice());
			System.out.println(t.getCurrency());
			System.out.println(t.getTimestamp());
			System.out.println(t.getTradeType());
		}
	}
	private void printDepthObject(String jsonString) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		Set<Depth> depths = new HashSet<>();
		Collection<DepthJson> tradeJson = mapper.readValue(jsonString, new TypeReference<Collection<DepthJson>>() { });
		JsonNode rootNode = mapper.readTree(jsonString);
		Iterator<JsonNode> jsonIter = rootNode.elements();
		while(jsonIter.hasNext()){
			JsonNode jsonNode = jsonIter.next();
			Depth depth = new Depth();
			Iterator<String> namesIter = jsonNode.fieldNames();
			while(namesIter.hasNext()){			
				String str = namesIter.next();
				if("asks".equals(str)||"bids".equals(str)){
					depth.setTradeType(str);
				}
				Iterator<JsonNode> nodeContent = jsonNode.elements();
				while(nodeContent.hasNext()){
					JsonNode jsonNode2 = nodeContent.next();
					String noteText = jsonNode2.asText();
					System.out.println(noteText);
				}
			}
		}
	}
		//currency
		
        //System.out.println(rootNode.toString());

/*		List<Depth> depthList = tradeJson.getCurrency();
		for(Depth t : depthList){
			System.out.println(t.getPrice());
			System.out.println(t.getAmount());
		}*/
	
}
