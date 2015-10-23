package com.forgerock.elasticsearch.changes;

import org.elasticsearch.common.inject.AbstractModule;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;

public class ChangesModule extends AbstractModule {
    private final ESLogger log = Loggers.getLogger(ChangesModule.class);
    
    @Override
    protected void configure() {
        log.info("Binding Changes Plugin");
        bind(ChangeRegister.class).asEagerSingleton();
    }
}
