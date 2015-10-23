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
