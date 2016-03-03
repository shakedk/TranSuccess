package transSuccess.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;

@Service
public class TransSuccessService {
	private static final String DOMAIN = "btc-e.com";
	private static long _nonce= System.nanoTime()/100000000;
	private String _secret = "b9cf6e09d9251280c88160af0c3c9fd07726dd6ca53281b3b792b0b44e4ce735";
	private String _key = "2QAF2TQ9-9A9ZZPNG-GEM0HEMC-V5XHR2UT-ZHA3L0KU";

	/**
	 * Execute a authenticated query on btc-e.
	 *
	 * @param method
	 *            The method to execute.
	 * @param arguments
	 *            The arguments to pass to the server.
	 *
	 * @return The returned data as JSON or null, if the request failed.
	 *
	 * @see http://pastebin.com/K25Nk2Sv
	 */
	
	public final HttpResponse authenticatedHTTPRequest(String method, Map<String, String> arguments) {
		HashMap<String, String> headerLines = new HashMap<String, String>(); // Create

		Mac mac;
		SecretKeySpec key = null;

		if (arguments == null) { // If the user provided no arguments, just
			arguments = new HashMap<String, String>();
		}

		arguments.put("method", method); // Add the method to the post data.
		arguments.put("nonce", "" + ++_nonce); // Add the dummy nonce.

		String postData = "";

		for (Iterator argumentIterator = arguments.entrySet().iterator(); argumentIterator.hasNext();) {
			Map.Entry argument = (Map.Entry) argumentIterator.next();

			if (postData.length() > 0) {
				postData += "&";
			}
			postData += argument.getKey() + "=" + argument.getValue();
		}

		// Create a new secret key
		try {
			key = new SecretKeySpec(_secret.getBytes("UTF-8"), "HmacSHA512");
		} catch (UnsupportedEncodingException uee) {
			System.err.println("Unsupported encoding exception: " + uee.toString());
			return null;
		}

		// Create a new mac
		try {
			mac = Mac.getInstance("HmacSHA512");
		} catch (NoSuchAlgorithmException nsae) {
			System.err.println("No such algorithm exception: " + nsae.toString());
			return null;
		}

		// Init mac with key.
		try {
			mac.init(key);
		} catch (InvalidKeyException ike) {
			System.err.println("Invalid key exception: " + ike.toString());
			return null;
		}

		// Add the key to the header lines.
		headerLines.put("Key", _key);

		// Encode the post data by the secret and encode the result as base64.
		try {
			headerLines.put("Sign", Hex.encodeHexString(mac.doFinal(postData.getBytes("UTF-8"))));
		} catch (UnsupportedEncodingException uee) {
			System.err.println("Unsupported encoding exception: " + uee.toString());
			return null;
		}

		// Now do the actual request
		//String requestResult = HttpUtils.httpPost("https://" + DOMAIN + "/tapi", headerLines, postData);
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost("https://" + DOMAIN + "/tapi");
/*		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
		nameValuePairs.add(new BasicNameValuePair("userId",
				"123456789"));
		try {
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
*/
		HttpResponse response = null;
		try {
			response = client.execute(post);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (response != null) { // The request worked
			return response;
/*
			try {
				// Convert the HTTP request return value to JSON to parse
				// further.
				JSONObject jsonResult = JSONObject.fromObject(response);

				// Check, if the request was successful
				int success = jsonResult.getInt("success");

				if (success == 0) { // The request failed.
					String errorMessage = jsonResult.getString("error");

					System.err.println("btc-e.com trade API request failed: " + errorMessage);

					return null;
				} else { // Request succeeded!
					return jsonResult.getJSONObject("return");
				}

			} catch (JSONException je) {
				System.err.println("Cannot parse json request result: " + je.toString());

				return null; // An error occured...
			}*/
		}

		return null; // The request failed.
	}
	
	
}
