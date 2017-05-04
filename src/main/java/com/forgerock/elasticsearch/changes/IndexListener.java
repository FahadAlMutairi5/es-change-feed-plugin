package com.forgerock.elasticsearch.changes;

import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.io.stream.BytesStreamOutput;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.index.engine.Engine;
import org.elasticsearch.index.shard.IndexingOperationListener;
import org.elasticsearch.index.shard.ShardId;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.Set;

/**
 * Date: 04/05/2017
 * Time: 16:54
 */
public class IndexListener implements IndexingOperationListener {

    private final Logger log = Loggers.getLogger(IndexListener.class);

    private final Set<Source> sources;

    IndexListener(Set<Source> sources) {
        this.sources = sources;
    }

    @Override
    public void postIndex(ShardId shardId, Engine.Index index, Engine.IndexResult result) {

        ChangeEvent change=new ChangeEvent(
                index.id(),
                index.type(),
                new DateTime(),
                result.isCreated() ? ChangeEvent.Operation.CREATE : ChangeEvent.Operation.INDEX,
                index.version(),
                index.source()
        );

        addChange(change);
    }

    @Override
    public void postDelete(ShardId shardId, Engine.Delete delete, Engine.DeleteResult result) {

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

    private boolean filter(String index, String type, String id, Source source) {
        if (source.getIndices() != null && !source.getIndices().contains(index)) {
            return false;
        }

        if (source.getTypes() != null && !source.getTypes().contains(type)) {
            return false;
        }

        if (source.getIds() != null && !source.getIds().contains(id)) {
            return false;
        }

        return true;
    }

    private boolean filter(String index, ChangeEvent change) {
        for (Source source : sources) {
            if (filter(index, change.getType(), change.getId(), source)) {
                return true;
            }
        }

        return false;
    }

    private void addChange(ChangeEvent change) {

        String indexName = "";
        if (!filter(indexName, change)) {
            return;
        }

        String message;
        try {
            XContentBuilder builder = new XContentBuilder(JsonXContent.jsonXContent, new BytesStreamOutput());
            builder.startObject()
                    .field("_index", indexName)
                    .field("_type", change.getType())
                    .field("_id", change.getId())
                    .field("_timestamp", change.getTimestamp())
                    .field("_version", change.getVersion())
                    .field("_operation", change.getOperation().toString());
            if (change.getSource() != null) {
                builder.rawField("_source", change.getSource(), XContentType.JSON);
            }
            builder.endObject();

            message = builder.string();
        } catch (IOException e) {
            log.error("Failed to write JSON", e);
            return;
        }

        for (WebSocket listener : ChangesFeedPlugin.LISTENERS.values()) {
            try {
                listener.sendMessage(message);
            } catch (Exception e) {
                log.error("Failed to send message", e);
            }

        }

    }
}
