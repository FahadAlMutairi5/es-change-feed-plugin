package com.forgerock.elasticsearch.changes;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;

import java.util.List;


public class PreChangeEvent {

    private final String elasticsearch_url;
    private final Integer elasticsearch_port;
    private final String elasticsearch_username;
    private final String elasticsearch_password;
    private final String elasticsearch_schema;

    PreChangeEvent(String elasticsearch_url, Integer elasticsearch_port, String elasticsearch_username, String elasticsearch_password, String elasticsearch_schema) {
        this.elasticsearch_url = elasticsearch_url;

        this.elasticsearch_port = elasticsearch_port;
        this.elasticsearch_username = elasticsearch_username;
        this.elasticsearch_password = elasticsearch_password;
        this.elasticsearch_schema = elasticsearch_schema;
    }

    public RestHighLevelClient client(){
        final CredentialsProvider credentialsProvider =
                new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(elasticsearch_username, elasticsearch_password));

        RestClientBuilder builder = RestClient.builder(
                new HttpHost(elasticsearch_url, elasticsearch_port, elasticsearch_schema))
                .setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                    @Override
                    public HttpAsyncClientBuilder customizeHttpClient(
                            HttpAsyncClientBuilder httpClientBuilder) {
                        httpClientBuilder.disableAuthCaching();
                        return httpClientBuilder
                                .setDefaultCredentialsProvider(credentialsProvider);
                    }
                });

    RestHighLevelClient client = new RestHighLevelClient(
            builder
    );
//                new HttpHost("localhost", 9201, "http")));
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