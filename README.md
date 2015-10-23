# Elasticsearch Changes Feed Plugin

A plugin for [elasticsearch](https://www.elastic.co/products/elasticsearch) which allows a client to create a 
websocket connection to an elasticsearch node and receive a feed of changes from the database.

## Requirements

Elasticsearch 1.4
Java 7

## Installation

    bin/plugin --url file:///path/to/plugin --install es-changes-feed-plugin

## Configuration

### changes.port
Port the websocket will use. Default is 9400

### changes.primaryShardOnly 
Whether only changes to the primary shards or changes to all shards are sent. Default is false

### changes.listenSource 
Which indices, types and documents the plugin will listen to in the format `<index_pattern>[/<type_pattern>[/<document_pattern>]]`.

Each of the above patterns is can be either:

* `*` All indices, types or documents
* `id[,id]*` List of specific indices, types or documents

So for example:

`*` will match everything

`*/tweet/*` will match all tweets in any index

`gb,us/user,tweet/*` will match all documents of type user or tweet in the gb or us indices

