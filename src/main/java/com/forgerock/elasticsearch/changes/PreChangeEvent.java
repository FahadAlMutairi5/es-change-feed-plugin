package com.forgerock.elasticsearch.changes;

import org.apache.http.HttpHost;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;



public class PreChangeEvent {

    PreChangeEvent() {

    }

    public RestHighLevelClient client(){   
    RestHighLevelClient client = new RestHighLevelClient(
        RestClient.builder(
                new HttpHost("localhost", 9200, "http"),
                new HttpHost("localhost", 9201, "http")));
    return client;
    }

    public GetRequest getIndex(String id, String index) {
        GetRequest getRequest = new GetRequest(
            index,
            "_doc", 
            id);
        return getRequest;
    }
}