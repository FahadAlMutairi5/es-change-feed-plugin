package com.forgerock.elasticsearch.changes;

/*
    Copyright 2015 ForgeRock AS

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.IndexModule;
import org.elasticsearch.plugins.Plugin;
import org.glassfish.tyrus.server.Server;

import javax.websocket.DeploymentException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ChangesFeedPlugin extends Plugin {

    private static final String SETTING_PRIMARY_SHARD_ONLY = "changes.primaryShardOnly";
    private static final String SETTING_PORT = "changes.port";
    private static final String SETTING_LISTEN_SOURCE = "changes.listenSource";

    static final Map<String, WebSocket> LISTENERS = new HashMap<String, WebSocket>();//TODO urgh

    private final Logger log = Loggers.getLogger(ChangesFeedPlugin.class);
    final Set<Source> sources;

    public ChangesFeedPlugin(Settings settings) {
        log.info("Starting Changes Plugin");

        final boolean allShards = !settings.getAsBoolean(SETTING_PRIMARY_SHARD_ONLY, Boolean.FALSE);
        final int port = settings.getAsInt(SETTING_PORT, 9400);
        final String[] sourcesStr = settings.getAsArray(SETTING_LISTEN_SOURCE, new String[]{"*"});
        this.sources = new HashSet<>();
        for(String sourceStr : sourcesStr) {
            sources.add(new Source(sourceStr));
        }

        final Server server = new Server("localhost", port, "/ws", null, WebSocket.class) ;

        try {
            log.info("Starting WebSocket server");
            AccessController.doPrivileged(new PrivilegedAction() {
                @Override
                public Object run() {
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

    @Override
    public void onIndexModule(IndexModule indexModule) {
        indexModule.addIndexOperationListener(new IndexListener(sources));
        super.onIndexModule(indexModule);
    }

    public static void registerListener(WebSocket webSocket) {
        LISTENERS.put(webSocket.getId(), webSocket);
    }

    public static void unregisterListener(WebSocket webSocket) {
        LISTENERS.remove(webSocket.getId());
    }
}
