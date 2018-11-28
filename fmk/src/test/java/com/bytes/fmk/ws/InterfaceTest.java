package com.bytes.fmk.ws;

import org.junit.Before;

public class InterfaceTest extends ConnectionTest {
	
	protected int sendInterval;
	
	@Before public void initialize() {

        String sslRoot = "C:/Workspace/ksl/Keys/ssl/";
        String keystore = sslRoot + "weblogic.jks";
        String truststore = sslRoot + "truststore.jks";
        String password = "Password!1";

        System.out.println("Will now connect to server.");
        System.setProperty("javax.net.ssl.keyStore", keystore);
        System.setProperty("javax.net.ssl.keyStorePassword", password);
        System.setProperty("javax.net.ssl.trustStore", truststore);
        System.setProperty("javax.net.ssl.trustStorePassword", "Password!1");

        String trustStore = System.getProperty("javax.net.ssl.trustStore");
        if (trustStore == null) {
            System.out.println("javax.net.ssl.trustStore is not defined");
        } else {
            System.out.println("javax.net.ssl.trustStore = " + trustStore);
        }


        location = "wss://localhost:7002/WebServices/ws";

		handshakeWait = 3000;
		sendInterval = 100;
		msgCount = 10;
		message = new String("green");
		
		
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
