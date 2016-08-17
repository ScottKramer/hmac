package com.chicagocoderconference.hmactest;

import junit.framework.TestCase;

import org.glassfish.grizzly.http.server.HttpServer;

import com.chicagocoderconference.hmactest.JerseyHMACSimulator;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.header.MediaTypes;


public class JerseyHMACSimulatorTest extends TestCase {

    private HttpServer httpServer;
    private WebResource webResource;

    public JerseyHMACSimulatorTest(String testName) {
        super(testName);
    }
    

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        httpServer = JerseyHMACSimulator.startServer();  //start the Grizzly2 web container
        Client c = Client.create();  // create the client
        webResource = c.resource(JerseyHMACSimulator.BASE_URI);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        httpServer.stop();
    }

    //Test to see that web service is returning message.
    public void testHMACSend() {
        String responseMsg = webResource.path("/jerseyHMAC/send/test").get(String.class);
        assertTrue(responseMsg.contains("HTTP/1.1 200 OK:{"));
    }
    
    //Test WADL document is available at the relative path (http://localhost:9998/)...application.wadl
    public void testApplicationWadl() {
        String serviceWadl = webResource.path("application.wadl").
                accept(MediaTypes.WADL).get(String.class);

        assertTrue(serviceWadl.length() > 0);
    }
}
