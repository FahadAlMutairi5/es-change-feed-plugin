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

import org.elasticsearch.common.bytes.BytesReference;
import org.joda.time.DateTime;

public class ChangeEvent {
    private final String index;
    private final String id;
    private final String type;
    private final DateTime timestamp;
    private final Operation operation;
    private final long version;
    private final BytesReference source;

    public enum Operation {
        INDEX,CREATE,DELETE
    }

    public ChangeEvent(String index, String type, String id, DateTime timestamp, Operation operation, long version, BytesReference source) {

        this.index = index;
        this.id = id;
        this.type = type;
        this.timestamp = timestamp;
        this.operation = operation;
        this.version = version;
        this.source = source;
    }

    public String getIndex() {
        return index;
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
