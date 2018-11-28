package com.bytes.fmk.ws;

import org.junit.Before;

public class StringTest extends ConnectionTest {
	
	protected int sendInterval;
	
	@Before 
	public void initialize() {

		// Use this for Weblogic endpoint		
		// location = "ws://localhost:7001/WebSocketApplication/ws";
    
		// Context root in Tomcat is the folder or war file name!
		// In this case, it's server.war
		location = "ws://localhost:8080/bot-server/pogo";
		handshakeWait = 3000;
		sendInterval = 100;
		msgCount = 10;
		message = new String("This is a string test");
		
		/** Sends n messages to the server. */
		sendTask =  new Runnable() {
			
			@Override
			public void run() {
				while (clientCounter < msgCount) {
					client.send(message + " client: " + (clientCounter));
					clientCounter++;
					try {
						Thread.sleep(sendInterval);
					} catch (InterruptedException e) {						
						e.printStackTrace();
					}
				}
				
			}
		};		
	}
	
}
