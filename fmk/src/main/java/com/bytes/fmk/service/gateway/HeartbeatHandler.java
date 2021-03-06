package com.bytes.fmk.service.gateway;
//package com.bytes.fmk.ws;
//
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.atomic.AtomicBoolean;
//import java.util.concurrent.atomic.AtomicInteger;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
///**
// * Handles sending heartbeats and receiving heartbeat acks. The handler uses
// * this information to detect a zombie connection with Discord and initiates the
// * reconnect process when detected. This happens if Discord doesn't respond to a
// * heartbeat with a heartbeat ack {@link #maxMissedPings} times in a row.
// */
//class HeartbeatHandler {
//
//	private static Logger logger = LoggerFactory.getLogger(HeartbeatHandler.class);
//
//	/**
//	 * The websocket connection this handler sends heartbeats on.
//	 */
//	private final DiscordWS ws;
//
//	/**
//	 * The maximum number of times Discord can not respond with a heartbeat ACK
//	 * before the reconnect process begins.
//	 */
//	private final int maxMissedPings;
//	/**
//	 * The number of missed heartbeat acks since the last successful ack.
//	 */
//	private final AtomicInteger missedPings = new AtomicInteger(0);
//	/**
//	 * Whether the handler has sent a heartbeat and is waiting for an ack from
//	 * Discord.
//	 */
//	private final AtomicBoolean waitingForAck = new AtomicBoolean(false);
//
//	/**
//	 * Thread on which the {@link #heartbeatTask} runs.
//	 */
//	private ScheduledExecutorService keepAlive = Executors
//			.newSingleThreadScheduledExecutor(DiscordUtils.createDaemonThreadFactory("Keep-Alive Handler"));
//	/**
//	 * The task scheduled on {@link #keepAlive} which handles the actual sending and
//	 * receiving of heartbeats.
//	 */
//	private final Runnable heartbeatTask;
//
//	/**
//	 * The time at which the last heartbeat was sent.
//	 */
//	private long sentHeartbeatAt;
//	/**
//	 * The amount of time it last took Discord to respond to a heartbeat with an
//	 * ack.
//	 */
//	private long ackResponseTime;
//
//	HeartbeatHandler(DiscordWS ws, int maxMissedPings) {
//		this.ws = ws;
//		this.maxMissedPings = maxMissedPings;
//
//		heartbeatTask = () -> {
//			if (waitingForAck.get()) { // Missed ping
//				missedPings.set(missedPings.get() + 1);
//				logger.debug("Last heartbeat not acknowledged by Discord. Total: {}", missedPings.get());
//
//				if (missedPings.get() == maxMissedPings) {
//					logger.info("Missed max number of heartbeat acks. Opening new session.");
//					ws.state = DiscordWS.State.RECONNECTING;
//					ws.client.reconnectManager.scheduleReconnect(ws);
//				}
//			} else {
//				missedPings.set(0);
//			}
//
//			logger.trace("Sending heartbeat on shard {}", ws.shard.getInfo()[0]);
//			ws.send(GatewayOps.HEARTBEAT, ws.seq);
//			sentHeartbeatAt = System.currentTimeMillis();
//			waitingForAck.set(true);
//		};
//	}
//
//	/**
//	 * Schedules the {@link #heartbeatTask} on the {@link #keepAlive} executor with
//	 * the given interval. If the executor was previously shutdown (this happens in
//	 * the case of reconnects), then a new one is created.
//	 *
//	 * @param interval
//	 *            The time between heartbeats in milliseconds.
//	 */
//	void begin(long interval) {
//		if (keepAlive.isShutdown())
//			keepAlive = Executors
//					.newSingleThreadScheduledExecutor(DiscordUtils.createDaemonThreadFactory("Keep-Alive Handler"));
//
//		keepAlive.scheduleAtFixedRate(heartbeatTask, 0, interval, TimeUnit.MILLISECONDS);
//	}
//
//	/**
//	 * Called when the gateway receives a {@link GatewayOps#HEARTBEAT_ACK}.
//	 */
//	void ack() {
//		if (!waitingForAck.get()) {
//			logger.debug("Received heartbeat ack without sending a heartbeat. Is the websocket out of sync?");
//		}
//		ackResponseTime = System.currentTimeMillis() - sentHeartbeatAt;
//		waitingForAck.set(false);
//	}
//
//	/**
//	 * Stops the sending of heartbeats and resets stored heartbeat information.
//	 */
//	void shutdown() {
//		missedPings.set(0);
//		waitingForAck.set(false);
//		keepAlive.shutdown();
//	}
//
//	/**
//	 * Gets the amount of time it last took Discord to respond to a heartbeat with
//	 * an ack.
//	 *
//	 * @return the amount of time it last took Discord to respond to a heartbeat
//	 *         with an ack.
//	 */
//	long getAckResponseTime() {
//		return ackResponseTime;
//	}
//}
