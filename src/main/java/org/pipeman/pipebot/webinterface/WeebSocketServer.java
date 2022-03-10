package org.pipeman.pipebot.webinterface;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONObject;
import org.pipeman.pipebot.Main;
import org.pipeman.pipebot.music.PlayerInstance;
import org.pipeman.pipebot.webinterface.webserver.WebServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class WeebSocketServer extends WebSocketServer {
    private static final int TCP_PORT = 12000;
    private final Logger logger = LoggerFactory.getLogger(WeebSocketServer.class);
    private final HashMap<WebSocket, String> ips = new HashMap<>();
    private final HashMap<WebSocket, String> sessions = new HashMap<>();

    public WeebSocketServer() {
        super(new InetSocketAddress(TCP_PORT));
    }

    @Override
    public void onStart() {
        logger.info("Started weeb socket");
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        logger.debug("New connection from "
                + handshake.getFieldValue("X-Real-IP")
                + conn.getResourceDescriptor());

        ips.put(conn, handshake.getFieldValue("X-Real-IP"));
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!sessions.containsKey(conn)) {
                    conn.close();
                    logger.debug(ips.get(conn) + " did not send their session id.");
                }
            }
        }, 2000);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        logger.debug("Closed connection to " + ips.get(conn));
        sessions.remove(conn);
        ips.remove(conn);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        try {
            JSONObject jsonObject = new JSONObject(message);
            if (jsonObject.optString("id").equals("0") && jsonObject.has("sid") && !sessions.containsKey(conn)) {
                sessions.put(conn, jsonObject.getString("sid"));
                logger.debug("Bound " + ips.get(conn) + " to session id \"" + sessions.get(conn) + "\"");

                PlayerInstance pi = WebServer.getPiForSession(sessions.get(conn));

                if (pi != null) {
                    S2CPackets.sendPlayerStatus(conn, pi);
                    S2CPackets.sendQueue(conn, pi.queue);
                    S2CPackets.sendHistory(conn);
                }
            }
        } catch (Exception ignored) {}
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
    }

    public void close() throws InterruptedException {
        this.stop();
    }

    public void updateQueue(PlayerInstance pi) {
        for (Map.Entry<WebSocket, String> e : sessions.entrySet()) {
            if (e.getValue().equals(pi.sessionID)) S2CPackets.sendQueue(e.getKey(), pi.queue);
        }
    }

    public void updatePI(PlayerInstance pi) {
        for (Map.Entry<WebSocket, String> e : sessions.entrySet()) {
            if (e.getValue().equals(pi.sessionID)) S2CPackets.sendPlayerStatus(e.getKey(), pi);
        }
    }

    public void closeSession(PlayerInstance pi) {
        for (Map.Entry<WebSocket, String> e : sessions.entrySet()) {
            if (e.getValue().equals(pi.sessionID)) {
                S2CPackets.closeSession(e.getKey());
                Timer t = new Timer();
                t.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (e.getKey().isOpen()) e.getKey().close();
                    }
                }, 2000);
            }
        }
    }

    public static void main(String[] args) {
        new WeebSocketServer().start();
    }
}