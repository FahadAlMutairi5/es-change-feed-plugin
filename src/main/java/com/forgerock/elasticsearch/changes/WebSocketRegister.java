package com.forgerock.elasticsearch.changes;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Date: 04/05/2017
 * Time: 17:46
 */
public class WebSocketRegister {
    private final Map<String, WebSocketEndpoint> listeners = new HashMap<String, WebSocketEndpoint>();

    public void registerListener(WebSocketEndpoint webSocket) {
        listeners.put(webSocket.getId(), webSocket);
    }

    public void unregisterListener(WebSocketEndpoint webSocket) {
        listeners.remove(webSocket.getId());
    }

    public Collection<WebSocketEndpoint> getListeners() {
        return Collections.unmodifiableCollection(listeners.values());
    }
}
