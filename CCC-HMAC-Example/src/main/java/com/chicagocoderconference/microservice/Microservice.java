package com.chicagocoderconference.microservice;

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

@Path("/microservice")
public class Microservice {
	public final static URI BASE_URI = UriBuilder.fromUri("http://localhost/").port(9998).build();
	
	protected static HttpServer startServer() throws IOException {
		final Map<String, String> initParams = new HashMap<String, String>();
		System.out.println("Starting JavaEE App Server - grizzly2...");
		initParams.put("com.sun.jersey.config.property.packages","com.chicagocoderconference.microservice");
		return GrizzlyWebContainerFactory.create(BASE_URI, initParams);
	}

	public static void main(String[] args) throws HttpException, IOException, NoSuchAlgorithmException {
		HttpServer httpServer = startServer();
		System.out.println(String.format("Jersey Started - WADL at %sapplication.wadl\nHit [ENTER] to stop", BASE_URI));
		System.in.read();
		httpServer.stop();
	}

	@GET
	@Path("test1")
	public String restServiceTest()
			throws HttpException, IOException, NoSuchAlgorithmException {
		return "Yeah, Got TestOne Working";
	}
	
	//LESSON: 1 - Add one more listener for a test message
	// ...
	//@Path("test2/{message}")
	//public String restServiceMessageTest(@PathParam("message") String message)

	
}
