package com.forgerock.customerportal.changes;

import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.io.stream.BytesStreamOutput;
import org.elasticsearch.common.joda.time.DateTime;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.index.engine.Engine;
import org.elasticsearch.index.indexing.IndexingOperationListener;
import org.elasticsearch.index.shard.service.IndexShard;
import org.elasticsearch.indices.IndicesLifecycle;
import org.elasticsearch.indices.IndicesService;
import org.glassfish.tyrus.server.Server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Date: 07/08/2015
 * Time: 14:57
 */
public class ChangeRegister {

    private static final String SETTING_PRIMARY_SHARD_ONLY = "changes.primaryShardOnly";
    private static final String SETTING_PORT = "changes.port";

    private final ESLogger log = Loggers.getLogger(ChangeRegister.class);

    private static final Map<String, WebSocket> LISTENERS = new HashMap<String, WebSocket>();

    @Inject
    public ChangeRegister(final Settings settings, IndicesService indicesService) {
        final boolean allShards = !settings.getAsBoolean(SETTING_PRIMARY_SHARD_ONLY, Boolean.FALSE);
        final int port = settings.getAsInt(SETTING_PORT, 9400);

        Server server = new Server("localhost", port, "/ws", null, WebSocket.class);

        try {
            log.info("Starting WebSocket server");
            server.start();
            log.info("WebSocket server started");
        } catch (Exception e) {
            log.error("Failed to start WebSocket server",e);
            throw new RuntimeException(e);
        }

        indicesService.indicesLifecycle().addListener(new IndicesLifecycle.Listener() {
            @Override
            public void afterIndexShardStarted(IndexShard indexShard) {
                if (allShards || indexShard.routingEntry().primary()) {

                    indexShard.indexingService().addListener(new IndexingOperationListener() {
                        @Override
                        public void postCreate(Engine.Create create) {
                            ChangeEvent change=new ChangeEvent(
                                    create.id(),
                                    create.type(),
                                    new DateTime(),
                                    ChangeEvent.Operation.CREATE,
                                    create.version(),
                                    create.source()
                            );

                            addChange(change);
                        }

                        @Override
                        public void postDelete(Engine.Delete delete) {
                            ChangeEvent change=new ChangeEvent(
                                    delete.id(),
                                    delete.type(),
                                    new DateTime(),
                                    ChangeEvent.Operation.DELETE,
                                    delete.version(),
                                    null
                            );

                            addChange(change);
                        }

                        @Override
                        public void postIndex(Engine.Index index) {

                            ChangeEvent change=new ChangeEvent(
                                    index.id(),
                                    index.type(),
                                    new DateTime(),
                                    ChangeEvent.Operation.INDEX,
                                    index.version(),
                                    index.source()
                            );

                            addChange(change);
                        }

                        private void addChange(ChangeEvent change) {

                            String message;
                            try {
                                XContentBuilder builder = new XContentBuilder(JsonXContent.jsonXContent, new BytesStreamOutput());
                                builder.startObject()
                                        .field("_type", change.getType())
                                        .field("_id", change.getId())
                                        .field("timestamp", change.getTimestamp())
                                        .field("version", change.getVersion())
                                        .field("operation", change.getOperation().toString())
                                        .rawField("_source", change.getSource())
                                        .endObject();

                                message = builder.string();
                            } catch (IOException e) {
                                log.error("Failed to write JSON", e);
                                return;
                            }

                            for (WebSocket listener : LISTENERS.values()) {
                                try {
                                    listener.sendMessage(message);
                                } catch (Exception e) {
                                    log.error("Failed to send message", e);
                                }

                            }

                        }
                    });
                }
            }

        });
    }

    public static void registerListener(WebSocket webSocket) {
        LISTENERS.put(webSocket.getId(), webSocket);
    }

    public static void unregisterListener(WebSocket webSocket) {
        LISTENERS.remove(webSocket.getId());
    }
}
