package com.forgerock.elasticsearch.changes;

import org.joda.time.DateTime;
import org.junit.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Date: 05/05/2017
 * Time: 10:09
 */
public class WebSocketIndexListenerTest {

    private final ChangeEvent change = new ChangeEvent("idx", "type", "doc", new DateTime(2017, 4, 1, 12, 0), ChangeEvent.Operation.CREATE, 1, null);

    @Test
    public void testCompleteFilter() {

        assertTrue(WebSocketIndexListener.filter(change, set(new Source("idx/type/doc"))));
        assertFalse(WebSocketIndexListener.filter(change, set(new Source("x/y/z"))));
    }

    @Test
    public void testWildcardDoc() {

        assertTrue(WebSocketIndexListener.filter(change, set(new Source("idx/type/*"))));
        assertFalse(WebSocketIndexListener.filter(change, set(new Source("x/y/*"))));

        assertTrue(WebSocketIndexListener.filter(change, set(new Source("idx/type"))));
        assertFalse(WebSocketIndexListener.filter(change, set(new Source("x/y"))));
    }

    @Test
    public void testWildcardDocType() {
        assertTrue(WebSocketIndexListener.filter(change, set(new Source("idx/*/*"))));
        assertFalse(WebSocketIndexListener.filter(change, set(new Source("x/*/*"))));

        assertTrue(WebSocketIndexListener.filter(change, set(new Source("idx"))));
        assertFalse(WebSocketIndexListener.filter(change, set(new Source("x"))));
    }

    @Test
    public void testWildcardDocTypeIndex() {
        assertTrue(WebSocketIndexListener.filter(change, set(new Source("*/*/*"))));

        assertTrue(WebSocketIndexListener.filter(change, set(new Source("*"))));
    }

    @Test
    public void testWildcardIndex() {
        assertTrue(WebSocketIndexListener.filter(change, set(new Source("*/type/doc"))));

        assertFalse(WebSocketIndexListener.filter(change, set(new Source("*/x/y"))));
    }

    @Test
    public void testWildcardType() {
        assertTrue(WebSocketIndexListener.filter(change, set(new Source("idx/*/doc"))));

        assertFalse(WebSocketIndexListener.filter(change, set(new Source("x/*/y"))));
    }


    private Set<Source> set(Source... sources) {
        return Arrays.stream(sources)
                .collect(Collectors.toSet());

    }
}
