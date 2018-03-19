# Elasticsearch Changes Feed Plugin

A plugin for [elasticsearch](https://www.elastic.co/products/elasticsearch) which allows a client to create a 
websocket connection to an elasticsearch node and receive a feed of changes from the database.

Loosely based on https://github.com/derryx/elasticsearch-changes-plugin

## Requirements

* Elasticsearch 1.x, 2.x, 5.x, 6.x (experimental)
* Java 8+

## Prebuilt Binary Installation     

To install run the following command, replacing {version} with the version you want to install, e.g. v2.2.0. Note that 
the plugin requires additional permissions which will automatically be granted using the below command (remove -b option
if you want to manually approve these permissions)

    bin/elasticsearch-plugin install -b https://github.com/jurgc11/es-change-feed-plugin/releases/download/{version}/es-changes-feed-plugin.zip

Restart elasticsearch.

Note that Elasticsearch plugins have to be built for the exact version of Elasticsearch which they are to be installed 
in. If a prebuilt binary isn't available for your version of Elasticsearch, then you'll need to build from source.

## Build From Source

    git clone https://github.com/jurgc11/es-change-feed-plugin.git
    cd es-change-feed-plugin

Update the `version` and `elasticsearch.version` property in the `pom.xml`. Then to build: 

    mvn clean install
    
### Troubleshooting

When changing the version you may run into problems when the Elasticsearch API changes. Try taking the nearest branch 
to the version you're trying to build for.   

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

### changes.disable
Disable the plugin through config. Setting this to true will stop the websocket server 
from starting up and will not intercept and change requests.

## Messages

The websocket will be on the path `/ws/_changes`. When you connect, you should start receiving messages which look 
something like this: 

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
