package com.forgerock.elasticsearch.changes;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.io.stream.BytesStreamOutput;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.index.engine.Engine;
import org.elasticsearch.index.shard.IndexingOperationListener;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.action.get.GetRequest;

import org.joda.time.DateTime;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;

/**
 * Date: 04/05/2017 Time: 16:54
 */
class WebSocketIndexListener implements IndexingOperationListener {

    private final Logger log = Loggers.getLogger(WebSocketIndexListener.class, "Changes Feed");

    private final Set<Source> sources;
    private final List<String> filter;
    private final WebSocketRegister register;
    private BytesReference sourceAsMap;
    private final String elasticsearch_url;
    private final Integer elasticsearch_port;
    private final String elasticsearch_username;
    private final String elasticsearch_password;
    private final String elasticsearch_schema;

    WebSocketIndexListener(Set<Source> sources, List<String> filter, WebSocketRegister register,
                           String elasticsearch_url, Integer elasticsearch_port, String elasticsearch_username,
                           String elasticsearch_password, String elasticsearch_schema) {
        this.sources = sources;
        this.filter = filter;
        this.register = register;
        this.elasticsearch_url = elasticsearch_url;
        this.elasticsearch_port = elasticsearch_port;
        this.elasticsearch_username = elasticsearch_username;
        this.elasticsearch_password = elasticsearch_password;
        this.elasticsearch_schema = elasticsearch_schema;
    }

    @Override
    public Engine.Index preIndex(ShardId shardId, Engine.Index index) {
        try {
            PreChangeEvent preChangeEvent = new PreChangeEvent(elasticsearch_url, elasticsearch_port, elasticsearch_username, elasticsearch_password, elasticsearch_schema);
            RestHighLevelClient client = preChangeEvent.client();
            GetRequest request = preChangeEvent.getIndex(index.id(), shardId.getIndex().getName());
            GetResponse getResponse = client.get(request, RequestOptions.DEFAULT);
            sourceAsMap = getResponse.getSourceAsBytesRef();
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return index;
    }

    @Override
    public void postIndex(ShardId shardId, Engine.Index index, Engine.IndexResult result) {

        ChangeEvent change = new ChangeEvent(shardId.getIndex().getName(), index.type(), index.id(), new DateTime(),
                result.isCreated() ? ChangeEvent.Operation.CREATE : ChangeEvent.Operation.INDEX, result.getVersion(),
                index.source());

        addChange(change);
    }

    @Override
    public void postDelete(ShardId shardId, Engine.Delete delete, Engine.DeleteResult result) {

        ChangeEvent change = new ChangeEvent(shardId.getIndex().getName(), delete.type(), delete.id(), new DateTime(),
                ChangeEvent.Operation.DELETE, result.getVersion(), null);

        addChange(change);
    }

    private static boolean filter(String index, String type, String id, Source source) {

        if (source.getIndices() != null && !source.getIndices().contains(index)) {

            boolean result = false;
            for (String s : source.getIndices()) {
                if (s != null && s.length() > 0 && s.endsWith("*")
                        && index.startsWith(s.substring(0, s.length() - 1))) {
                    result = true;
                    break;
                }
            }

            if (result == false) {
                return false;
            }
        }

        if (source.getTypes() != null && !source.getTypes().contains(type)) {
            return false;
        }

        if (source.getIds() != null && !source.getIds().contains(id)) {
            return false;
        }

        return true;
    }

    static boolean filter(ChangeEvent change, Set<Source> sources) {
        for (Source source : sources) {
            if (filter(change.getIndex(), change.getType(), change.getId(), source)) {
                return true;
            }
        }

        return false;
    }

    private void addChange(ChangeEvent change) {

        if (!filter(change, sources)) {
            return;
        }
        String message;

        Set<String> filters = new HashSet<>(filter);

        try {

            XContentBuilder builder = new XContentBuilder(JsonXContent.jsonXContent, new BytesStreamOutput(), filters);
            builder.startObject().field("_index", change.getIndex()).field("_type", change.getType())
                    .field("_id", change.getId()).field("_timestamp", change.getTimestamp())
                    .field("_version", change.getVersion()).field("_operation", change.getOperation().toString());
            if (change.getSource() != null) {
                builder.rawField("_source", change.getSource().streamInput(), XContentType.JSON);
            }
            if (sourceAsMap != null){
                builder.rawField("_oldSource", sourceAsMap.streamInput(), XContentType.JSON);
            }
            builder.endObject();
            message = Strings.toString(builder);
        } catch (IOException e) {
            log.error("Failed to write JSON", e);
            return;
        }

        for (WebSocketEndpoint listener : register.getListeners()) {
            try {
                listener.sendMessage(message);
            } catch (Exception e) {
                log.error("Failed to send message", e);
            }

        }

    }
}
