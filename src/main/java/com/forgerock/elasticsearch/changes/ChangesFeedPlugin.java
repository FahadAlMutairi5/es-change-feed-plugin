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

import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Setting;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.IndexModule;
import org.elasticsearch.plugins.Plugin;

import java.util.*;
import java.util.stream.Collectors;

public class ChangesFeedPlugin extends Plugin {

    private static final String SETTING_PORT = "changes.port";
    private static final String SETTING_LISTEN_SOURCE = "changes.listenSource";
    private static final String SETTING_DISABLE = "changes.disable";

    private final Logger log = Loggers.getLogger(ChangesFeedPlugin.class);
    private final Set<Source> sources;
    private final boolean enabled;
    private final static WebSocketRegister REGISTER = new WebSocketRegister();

    public ChangesFeedPlugin(Settings settings) {
        log.info("Starting Changes Plugin");

        enabled = !settings.getAsBoolean(SETTING_DISABLE, false);

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
            indexModule.addIndexOperationListener(new WebSocketIndexListener(sources, REGISTER));
        }
        super.onIndexModule(indexModule);
    }

    public List<Setting<?>> getSettings() {
        List<Setting<?>> settings = new ArrayList<>();
        settings.add(Setting.simpleString(SETTING_PORT, Setting.Property.NodeScope));
        settings.add(Setting.simpleString(SETTING_LISTEN_SOURCE, Setting.Property.NodeScope));
        settings.add(Setting.simpleString(SETTING_DISABLE, Setting.Property.NodeScope));
        return settings;
    }

    static WebSocketRegister getRegister() {
        return REGISTER;
    }
}
