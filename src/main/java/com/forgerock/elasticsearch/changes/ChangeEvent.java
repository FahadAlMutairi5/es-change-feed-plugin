package com.forgerock.elasticsearch.changes;

import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.joda.time.DateTime;

/**
 * Date: 07/08/2015
 * Time: 15:41
 */
public class ChangeEvent {
    private final String id;
    private final String type;
    private final DateTime timestamp;
    private final Operation operation;
    private final long version;
    private final BytesReference source;

    public enum Operation {
        INDEX,CREATE,DELETE
    }

    public ChangeEvent(String id, String type, DateTime timestamp, Operation operation, long version, BytesReference source) {
        this.id = id;
        this.type = type;
        this.timestamp = timestamp;
        this.operation = operation;
        this.version = version;
        this.source = source;
    }

    public String getId() {
        return id;
    }

    public Operation getOperation() {
        return operation;
    }

    public DateTime getTimestamp() {
        return timestamp;
    }

    public String getType() {
        return type;
    }

    public long getVersion() {
        return version;
    }

    public BytesReference getSource() {
        return source;
    }
}
