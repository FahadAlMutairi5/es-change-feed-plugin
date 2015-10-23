package com.forgerock.elasticsearch.changes;

import org.glassfish.tyrus.client.ClientManager;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CountDownLatch;

/**
 * Date: 07/08/2015
 * Time: 14:07
 */
@ClientEndpoint
public class WebSocketClient {

    private static CountDownLatch latch;

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("Connected ... " + session.getId());
        try {
            session.getBasicRemote().sendText("start");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {

        System.out.println("Received ...." + message);
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        System.out.println(String.format("Session %s close because of %s", session.getId(), closeReason));
    }

    public static void main(String[] args) {
        latch = new CountDownLatch(1);

        ClientManager client = ClientManager.createClient();
        try {
            System.out.println("Connecting");
            client.connectToServer(WebSocketClient.class, new URI("ws://localhost:9400/ws/_changes"));
            System.out.println("Connected");
            latch.await();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
