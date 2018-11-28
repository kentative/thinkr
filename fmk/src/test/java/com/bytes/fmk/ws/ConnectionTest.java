package com.bytes.fmk.ws;

import java.net.URI;
import java.net.URISyntaxException;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 * This is the base class for all connection-type tests.
 * Test pattern is as follow:
 * 	1. define connection parameters
 *  2. define message parameters
 *  3. define the sendTask (Runnable) to send test-specific entity to the server
 * @author Kent
 *
 */
public abstract class ConnectionTest {

	private Logger logger = LoggerFactory.getLogger(ConnectionTest.class);
	
	// Connection Parameters
	protected WebSocketClient client;    // The Web Socket client, no need to override this
	protected String location;           // The Web Socket server address, fully qualified 
	protected int handshakeWait;         // The handshake wait time before sending the first message (millis)
	
	// Message Parameters
	protected String message;     		 // The default message, override as needed
	protected int msgCount;       		 // The number of messages to send
	protected boolean prettyPrint;       // If JSON is used, enable pretty print 
	
	// Send Task Parameters
	protected Runnable sendTask;         // Define the test-specific send task
	protected int clientCounter = 0;	 // Use to track message count [optional]
	
	// The pretty-print enabled gson
	protected Gson pfGson = new GsonBuilder().setPrettyPrinting().create();

	public ConnectionTest() {
		prettyPrint = false;
		
	}
	
	
	/**
	 * Sends n messages to the server and check to see if the server receives all of them.
	 * 	 * 
	 * @throws URISyntaxException
	 * @throws InterruptedException
	 */
	@Test 
	public void ConnectClient() throws URISyntaxException, InterruptedException {
		
		URI address = new URI(location);		
		client = new WebSocketClient(address, new Draft_17()) {		
			
			protected volatile int serverCounter = 0;
			
			@Override
			public void onOpen(ServerHandshake handshake) {				
				Assert.assertNotNull(handshake);
				serverCounter = 0;
			}
			
			@Override
			public void onMessage(String msg) {
				
				System.out.println(msg);
				
			}		

			@Override
			public void onError(Exception ex) {
				
				logger.info(ex.getMessage());				
			}
			
			@Override
			public void onClose(int code, String reason, boolean remote) {
				
				logger.info("Reason: " + reason +" Remote: " + remote);	
				logger.info("msgCount: " + msgCount +" serverCounter: " + serverCounter);	
			}
		};		
		
		client.connect();

		// Allow time to connect
		Thread.sleep(handshakeWait);
		if (client.getConnection().isOpen()) {
			logger.info("Connection is open. Executing sendTask.");
			if (sendTask != null) {
				sendTask.run();
			}		
		} else {
			logger.info("Connection is not open.");
		}
		
	}
	
}
