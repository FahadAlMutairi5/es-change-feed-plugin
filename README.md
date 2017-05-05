# Elasticsearch Changes Feed Plugin

A plugin for [elasticsearch](https://www.elastic.co/products/elasticsearch) which allows a client to create a 
websocket connection to an elasticsearch node and receive a feed of changes from the database.

Loosely based on https://github.com/derryx/elasticsearch-changes-plugin

## Requirements

* Elasticsearch 1.4, 2.2, 2.3, 2.4, 5.3
* Java 8+

## Prebuilt Binary Installation     

To install run the following command, replacing {version} with the version you want to install, e.g. v2.2.0. Note that 
the plugin requires additional permissions which will automatically be granted using the below command (remove -b option
if you want to manually approve these permissions)

    bin/plugin install -b https://github.com/jurgc11/es-change-feed-plugin/releases/download/{version}/es-changes-feed-plugin.zip

Restart elasticsearch.

## Build From Source

    git clone https://github.com/jurgc11/es-change-feed-plugin.git
    cd es-change-feed-plugin
    mvn clean install

## Limitations

There is a good explanation here of some of the difficulties inherent in a changes feed from elasticsearch:
https://groups.google.com/d/msg/elasticsearch/S3fSfr4Cz3g/fyse5X4ofuYJ

This plugin has the following limitations:

* Only sends live changes - if the client isn't connected at the time the plugin gets the change, it won't be sent
* Doesn't send the initial state of any documents. You need to use one of the existing APIs for this
* No handling of misbehaving endpoints beyond what the underlying websocket implementation does [see Project Tyrus](https://tyrus.java.net/)
* Client needs to handle failing nodes


## Configuration

### changes.port
Port the websocket will use. Default is 9400

### changes.listenSource 
Which indices, types and documents the plugin will listen to in the format `<index_pattern>[/<type_pattern>[/<document_pattern>]]`.

Each of the above patterns is can be either:

* `*` All indices, types or documents
* `id[,id]*` List of specific indices, types or documents

So for example:

`*` will match everything. This is the default value

`*/tweet/*` will match all tweets in any index

`gb,us/user,tweet/*` will match all documents of type user or tweet in the gb or us indices

## Messages

Below is an example message the client might receive:

    {
        "_id": "testdoc",
        "_index": "testidx",
        "_operation": "INDEX",
        "_source": {
            "hello": "world"
        },
        "_timestamp": "2015-10-23T14:22:44.738Z",
        "_type": "testtyp",
        "_version": 1
    }
