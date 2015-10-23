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

import org.elasticsearch.common.collect.ImmutableList;
import org.elasticsearch.common.inject.Module;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.plugins.AbstractPlugin;

import java.util.Collection;

public class ChangesFeedPlugin extends AbstractPlugin {
    private final ESLogger log = Loggers.getLogger(ChangesFeedPlugin.class);
    private final Collection<Class<? extends Module>> modules;

    public ChangesFeedPlugin() {
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
