package com.forgerock.elasticsearch.changes;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.io.stream.BytesStreamOutput;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.index.engine.Engine;
import org.elasticsearch.index.shard.IndexingOperationListener;
import org.elasticsearch.index.shard.ShardId;
import org.joda.time.DateTime;

/**
 * Date: 04/05/2017
 * Time: 16:54
 */
public class WebSocketIndexListener implements IndexingOperationListener {

    private final Logger log = Loggers.getLogger(WebSocketIndexListener.class);

    private final Set<Source> sources;
    private final List<String> filter;
    private final WebSocketRegister register;

    WebSocketIndexListener(Set<Source> sources, List<String> filter, WebSocketRegister register) {
        this.sources = sources;
        this.filter = filter;
        this.register = register;
    }

    @Override
    public void postIndex(ShardId shardId, Engine.Index index, Engine.IndexResult result) {

        ChangeEvent change=new ChangeEvent(
                shardId.getIndex().getName(),
                index.type(),
                index.id(),
                new DateTime(),
                result.isCreated() ? ChangeEvent.Operation.CREATE : ChangeEvent.Operation.INDEX,
                result.getVersion(),
                index.source()
        );

        addChange(change);
    }

    @Override
    public void postDelete(ShardId shardId, Engine.Delete delete, Engine.DeleteResult result) {

        ChangeEvent change=new ChangeEvent(
                shardId.getIndex().getName(),
                delete.type(),
                delete.id(),
                new DateTime(),
                ChangeEvent.Operation.DELETE,
                result.getVersion(),
                null
        );

        addChange(change);
    }

    private static boolean filter(String index, String type, String id, Source source) {
    	
        if (source.getIndices() != null && !source.getIndices().contains(index) ) {
 
        	boolean result = false;        	
        	for (String s : source.getIndices() ) {	      		        		
        		if (index.startsWith(s)) {
                    result = true;
                    break;
        		}      		
        	}
        	
        	if (result == false )
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
            builder.startObject()
                    .field("_index", change.getIndex())
                    .field("_type", change.getType())
                    .field("_id", change.getId())
                    .field("_timestamp", change.getTimestamp())
                    .field("_version", change.getVersion())
                    .field("_operation", change.getOperation().toString());
            if (change.getSource() != null) {
                builder.rawField("_source", change.getSource().streamInput(), XContentType.JSON);
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
