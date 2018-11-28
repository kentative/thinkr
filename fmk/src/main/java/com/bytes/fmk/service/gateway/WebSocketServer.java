/**
 * 
 */
package com.bytes.fmk.service.gateway;

import java.io.IOException;
import java.util.Vector;

import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @see https://javaee-spec.java.net/nonav/javadocs/javax/websocket/package-summary.html
 */
@ServerEndpoint("/off")
public class WebSocketServer {
	
	private static Logger logger = LoggerFactory.getLogger(WebSocketServer.class);
	
	private static Vector<Session> sessions = new Vector<Session>();
	
	/**
	 * Get the list of sessions
	 * @return
	 */
	public static Vector<Session> getSessions() {
		return sessions;
	}
	
	
	/**
	 * 
 	 * @param message
	 * @param session
	 * @throws IOException
	 * @throws InterruptedException
	 * @see https://javaee-spec.java.net/nonav/javadocs/javax/websocket/OnMessage.html
	 */
	@OnMessage
	public void onMessage(String message, Session session) throws IOException, InterruptedException {
		
		// Print the client message for testing purposes
		logger.info(session.getId() + ": "  + message);
		
		if (message.contains("green")) {
			sendToAll(session, "confirmed");
		} else {
			sendToAll(session, message);
		}
	}

	/**
	 * This method level annotation can be used to decorate a Java method that wishes to be called 
	 * when a new web socket session is open. 
	 * The method may only take the following parameters:
	 * optional Session parameter
	 * optional EndpointConfig parameter
	 * Zero to n String parameters annotated with the PathParam annotation.
	 * @param session
	 * @param config
	 */
	@OnOpen
	public void onOpen(Session session, EndpointConfig config) {		

		if (sessions.contains(session)) {			
			System.out.println("Removed existing session: " + session.getId());
			sessions.remove(session);			
		}		
		
		sessions.add(session);
		
		try {
			session.getBasicRemote().sendText("Welcome to server v10.22");
		} catch (IOException e) {
			logger.error("Unable to send message", e);
		}
		
		System.out.println(session.getId() + " connected.");		
	}
	
	/**
	 * This method level annotation can be used to decorate a Java method that wishes to be called 
	 * when a web socket session is closing.
	 * The method may only take the following parameters:-
	 * optional Session parameter
	 * optional CloseReason parameter
	 * Zero to n String parameters annotated with the PathParam annotation.
	 * @see #Endpoint.onClose()
	 * @param session
	 * @param config
	 */
	@OnClose
	public void onClose(Session session, CloseReason closeReason) {
		
		if (sessions.remove(session))  {
			System.out.println("Connection closed. " + 
					closeReason.getReasonPhrase() + ". successfully removed session  " +
					session.getId());
		} else {
			System.out.println("Connection closed: " + closeReason.getReasonPhrase());
		}
	}
	
	/**
	 * 
	 * @param source
	 * @param message
	 * @throws IOException
	 */
	private void sendToAll(Session source, String message) throws IOException {
		
		try {
			for (Session s : sessions) {
				if (s.isOpen()) {
					s.getBasicRemote().sendText(s.getId() + ": " +  message);
				}				
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
