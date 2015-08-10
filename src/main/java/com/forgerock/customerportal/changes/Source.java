package com.forgerock.customerportal.changes;

import org.elasticsearch.common.collect.ImmutableSet;

import java.util.Set;

/**
 * Date: 10/08/2015
 * Time: 17:06
 */
public class Source {
    private final Set<String> indices;
    private final Set<String> types;
    private final Set<String> ids;

    public Source(String source) {
        String[] parts = source.split("/");

        indices = parts[0].equals("*") ? null : ImmutableSet.copyOf(parts[0].split(","));

        if (parts.length > 1) {
            types = parts[1].equals("*") ? null : ImmutableSet.copyOf(parts[1].split(","));
        } else {
            types = null;
        }

        if (parts.length > 2) {
            ids = parts[2].equals("*") ? null : ImmutableSet.copyOf(parts[2].split(","));
        } else {
            ids = null;
        }
    }

    public Set<String> getIds() {
        return ids;
    }

    public Set<String> getIndices() {
        return indices;
    }

    public Set<String> getTypes() {
        return types;
    }

}
