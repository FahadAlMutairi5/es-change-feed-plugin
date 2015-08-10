package com.forgerock.customerportal.changes;

import org.elasticsearch.common.collect.ImmutableList;
import org.elasticsearch.common.inject.Module;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.plugins.AbstractPlugin;

import java.util.Collection;

/**
 * Date: 10/08/2015
 * Time: 14:34
 */
public class ChangesPlugin extends AbstractPlugin {
    private final ESLogger log = Loggers.getLogger(ChangesPlugin.class);
    private final Collection<Class<? extends Module>> modules;

    public ChangesPlugin() {
        log.info("Starting Changes Plugin");

        modules= ImmutableList.<Class<? extends Module>>of(ChangesModule.class);
    }

    @Override
    public Collection<Class<? extends Module>> modules() {
        return modules;
    }

    public String description() {
        return "Changes Plugin";
    }

    public String name() {
        return "changes";
    }
}
