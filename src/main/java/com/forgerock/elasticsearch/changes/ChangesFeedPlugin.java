package com.forgerock.elasticsearch.changes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Setting;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.IndexModule;
import org.elasticsearch.plugins.Plugin;

public class ChangesFeedPlugin extends Plugin {

    private static final String SETTING_PORT = "changes.port";
    private static final String SETTING_LISTEN_SOURCE = "changes.listenSource";
    private static final String SETTING_DISABLE = "changes.disable";
    private static final String SETTING_FILTER = "changes.field.includes";
    private static final String SETTING_ELASTICSEARCH_URL = "changes.elasticsearch.host";
    private static final String SETTING_ELASTICSEARCH_PORT = "changes.elasticsearch.port";
    private static final String SETTING_ELASTICSEARCH_USERNAME = "changes.elasticsearch.username";
    private static final String SETTING_ELASTICSEARCH_PASSWORD = "changes.elasticsearch.password";
    private static final String SETTING_ELASTICSEARCH_SCHEMA = "changes.elasticsearch.schema";

    private final Logger log = Loggers.getLogger(ChangesFeedPlugin.class, "Changes Feed");
    private final Set<Source> sources;
    private final boolean enabled;
    private final List<String> filter;
    private final static WebSocketRegister REGISTER = new WebSocketRegister();
    private final String elasticsearch_url;
    private final Integer elasticsearch_port;
    private final String elasticsearch_username;
    private final String elasticsearch_password;
    private final String elasticsearch_schema;

    public ChangesFeedPlugin(Settings settings) {
        log.info("Starting Changes Plugin");

        enabled = !settings.getAsBoolean(SETTING_DISABLE, false);
        filter = settings.getAsList(SETTING_FILTER, Collections.singletonList("*"));
        elasticsearch_url = settings.get(SETTING_ELASTICSEARCH_URL, "0.0.0.0");
        elasticsearch_port = settings.getAsInt(SETTING_ELASTICSEARCH_PORT, 9200);
        elasticsearch_username = settings.get(SETTING_ELASTICSEARCH_USERNAME, "elastic");
        elasticsearch_password = settings.get(SETTING_ELASTICSEARCH_PASSWORD, "elastic");
        elasticsearch_schema = settings.get(SETTING_ELASTICSEARCH_SCHEMA, "http");

        if (enabled) {
            int port = settings.getAsInt(SETTING_PORT, 9400);
            List<String> sourcesStr = settings.getAsList(SETTING_LISTEN_SOURCE, Collections.singletonList("*"));
            this.sources = sourcesStr.stream()
                    .map(Source::new)
                    .collect(Collectors.toSet());

            WebSocketServer server = new WebSocketServer(port);

            server.start();
        } else {
            sources = null;
        }
    }

    @Override
    public void onIndexModule(IndexModule indexModule) {
        if (enabled) {

            indexModule.addIndexOperationListener(new WebSocketIndexListener(sources, filter, REGISTER, elasticsearch_url, elasticsearch_port, elasticsearch_username, elasticsearch_password, elasticsearch_schema));
        }
        super.onIndexModule(indexModule);
    }

    public List<Setting<?>> getSettings() {
        List<Setting<?>> settings = new ArrayList<>();
        settings.add(Setting.simpleString(SETTING_PORT, Setting.Property.NodeScope));
        settings.add(Setting.simpleString(SETTING_LISTEN_SOURCE, Setting.Property.NodeScope));
        settings.add(Setting.simpleString(SETTING_DISABLE, Setting.Property.NodeScope));
        settings.add(Setting.simpleString(SETTING_FILTER, Setting.Property.NodeScope));
        settings.add(Setting.simpleString(SETTING_ELASTICSEARCH_URL, Setting.Property.NodeScope));
        settings.add(Setting.simpleString(SETTING_ELASTICSEARCH_PORT, Setting.Property.NodeScope));
        settings.add(Setting.simpleString(SETTING_ELASTICSEARCH_USERNAME, Setting.Property.NodeScope));
        settings.add(Setting.simpleString(SETTING_ELASTICSEARCH_PASSWORD, Setting.Property.NodeScope));
        settings.add(Setting.simpleString(SETTING_ELASTICSEARCH_SCHEMA, Setting.Property.NodeScope));
        return settings;
    }

    static WebSocketRegister getRegister() {
        return REGISTER;
    }
}
