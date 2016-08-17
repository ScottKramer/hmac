package com.chicagocoderconference.hmactest;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.commons.codec.binary.Base64;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.grizzly.http.server.HttpServer;

import com.sun.jersey.api.container.grizzly2.GrizzlyWebContainerFactory;

@Path("/jerseyhmac")
public class JerseyHMACSimulator {
	private final static String DATE_FORMAT = "EEE, d MMM yyyy HH:mm:ss z";
	private final static String HMAC_SHA256_ALGORITHM = "HmacSHA256";
	private final static String SECRET_KEY = "TheSecretSharedKey";
	public final static URI BASE_URI = UriBuilder.fromUri("http://localhost/").port(9998).build();

	protected static HttpServer startServer() throws IOException {
		final Map<String, String> initParams = new HashMap<String, String>();

		System.out.println("Starting JavaEE App Server - grizzly2...");
		initParams.put("com.sun.jersey.config.property.packages","com.chicagocoderconference.hmactest");
		return GrizzlyWebContainerFactory.create(BASE_URI, initParams);
	}

	@XmlRootElement
	private static class HmacServiceRequest implements Serializable {
		private static final long serialVersionUID = 7848988463595166908L;
		public String id; // Should not use public
		public String serviceURL; // Should not use public
		public String timestamp; // Should not use public
		public String hmacSignature; // Should not use public
		public String secretMessage; // Should not use public
	}

	public static void main(String[] args) throws HttpException, IOException,
			NoSuchAlgorithmException {

		HttpServer httpServer = startServer();
		System.out.println(String.format("Jersey Started - WADL at %sapplication.wadl\nHit [ENTER] to stop", BASE_URI));
		System.in.read();
		httpServer.stop();
	}

	@GET
	@Produces("text/plain")
	@Consumes("text/plain")
	@Path("send/{secretMessage}")
	public String makeHTTPCallUsingHMAC(
			@PathParam("secretMessage") String secretMessage)
			throws HttpException, IOException, NoSuchAlgorithmException {

		final String hmacRESTMicroserviceUrl = "http://localhost:9998/jerseyhmac/receive";
		String matchedSendVsReceiveHmac = "false";
		try { // Build the JSON object
			DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			dfm.setTimeZone(TimeZone.getTimeZone("GMT"));
			String timestamp = dfm.format(new Date());
			String hmacSignature = calculateSha256Hmac(SECRET_KEY,
					secretMessage.getBytes());
			String id = "SenderID";

			JSONObject jsonObject = new JSONObject();
			jsonObject.put("id", id);
			jsonObject.put("hmacSignature", hmacSignature);
			jsonObject.put("secretMessage", secretMessage);
			jsonObject.put("timestamp", timestamp);
			jsonObject.put("secretMessage", secretMessage);
			jsonObject.put("serviceURL", hmacRESTMicroserviceUrl);

			// Create the POST object and add the parameters
			HttpPost httpPost = new HttpPost(hmacRESTMicroserviceUrl);
			httpPost.addHeader("id", id);
			httpPost.addHeader("hmacSignature", hmacSignature);
			httpPost.addHeader("timestamp", timestamp);

			StringEntity entity = new StringEntity(jsonObject.toString(),"UTF-8");
			entity.setContentType("application/json; charset=UTF-8");
			httpPost.setEntity(entity);
			HttpClient httpClient = new DefaultHttpClient();
			HttpResponse httpResponse = httpClient.execute(httpPost);
			String json = EntityUtils.toString(httpResponse.getEntity());
			matchedSendVsReceiveHmac = httpResponse.getStatusLine() + ":"
					+ json.toString();

		} catch (Exception ex) {
			matchedSendVsReceiveHmac = "ERROR: Exception From Post";
			System.out.println(matchedSendVsReceiveHmac);
		}

		return matchedSendVsReceiveHmac;
	}

	@POST
	@Path("receive")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public HmacServiceRequest receiveHTTPCallUsingHMACPost(
			HmacServiceRequest hmacServiceRequest, @Context HttpHeaders headers) {

		String headerHmacSignature = headers.getRequestHeader("hmacSignature").get(0);

		HmacServiceRequest hmacServiceResponse = new HmacServiceRequest();
		if (headerHmacSignature.equals(calculateSha256Hmac(SECRET_KEY, hmacServiceRequest.secretMessage.getBytes()))) {
			hmacServiceResponse = hmacServiceRequest;
		}
		
		return hmacServiceResponse;
	}

	private String calculateSha256Hmac(String secret, byte[] data) {
		try {
			SecretKeySpec signingKey = new SecretKeySpec(secret.getBytes(),	HMAC_SHA256_ALGORITHM);
			Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
			mac.init(signingKey);
			byte[] rawHmac = mac.doFinal(data);
			String result = new String(Base64.encodeBase64(rawHmac));
			return result;
		} catch (GeneralSecurityException e) {
			throw new IllegalArgumentException();
		}
	}

	private byte[] calculateMD5Hmac(String secret, byte[] data) {
		try {
			SecretKeySpec signingKey = new SecretKeySpec(secret.getBytes(),	"HmacMD5");
			Mac mac = Mac.getInstance("HmacMD5");
			mac.init(signingKey);
			return mac.doFinal(data);
		} catch (GeneralSecurityException gse) {
			throw new IllegalArgumentException();
		}

	}

	private byte[] encrypt(byte[] secretKey, byte[] textToEncode)
			throws Exception {
		Cipher c = Cipher.getInstance("AES");
		SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey, "AES");
		c.init(Cipher.ENCRYPT_MODE, secretKeySpec);
		return c.doFinal(textToEncode);
	}

	private byte[] decrypt(byte[] secretKey, byte[] textToDecode)
			throws Exception {
		Cipher c = Cipher.getInstance("AES");
		SecretKeySpec k = new SecretKeySpec(secretKey, "AES");
		c.init(Cipher.DECRYPT_MODE, k);
		return c.doFinal(textToDecode);
	}


}