package com.forgerock.elasticsearch.changes;

import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.logging.Loggers;
import org.glassfish.tyrus.server.Server;

import javax.websocket.DeploymentException;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Date: 04/05/2017
 * Time: 17:24
 */
public class WebSocketServer {

    private final Logger log = Loggers.getLogger(WebSocketServer.class, "Changes Feed");
    private final Server server;

    public WebSocketServer(int port) {
        log.info("Starting Changes Plugin");

        server = new Server("0.0.0.0", port, "/ws", null, WebSocketEndpoint.class);
    }

    public void start() {
        try {
            log.info("Starting WebSocket server");
            AccessController.doPrivileged(new PrivilegedAction<Void>() {
                @Override
                public Void run() {
                    try {
                        // Tyrus tries to load the server code using reflection. In Elasticsearch 2.x Java
                        // security manager is used which breaks the reflection code as it can't find the class.
                        // This is a workaround for that
                        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
                        server.start();
                        return null;
                    } catch (DeploymentException e) {
                        throw new RuntimeException("Failed to start server", e);
                    }
                }
            });
            log.info("WebSocket server started");
        } catch (Exception e) {
            log.error("Failed to start WebSocket server",e);
            throw new RuntimeException(e);
        }

    }
}
