package com.forgerock.elasticsearch.changes;

/**
 * Date: 06/08/2015
 * Time: 16:32
 */

import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/_changes")
public class WebSocket {

    private final ESLogger log = Loggers.getLogger(WebSocket.class);
    private Session session;

    @OnOpen
    public void onOpen(Session session) {
        log.info("Connected ... " + session.getId());
        this.session = session;
        ChangeRegister.registerListener(this);
    }

    public void sendMessage(String message) {
        session.getAsyncRemote().sendText(message);
    }

    public String getId() {
        return session == null ? null : session.getId();
    }

    @OnMessage
    public void onMessage(String message) {
        log.info("Received message: "+message);
    }

    @OnClose
    public void onClose() {
        ChangeRegister.unregisterListener(this);
        this.session = null;
    }

    @OnError
    public void onError(Throwable t) {
        log.error("Error on websocket "+(session == null ? null : session.getId()), t);
    }


}
