package com.bazaarvoice.auth.hmac.dropwizard.client;

import com.bazaarvoice.auth.hmac.client.HmacClientFilter;
import com.bazaarvoice.auth.hmac.dropwizard.Pizza;
import com.sun.jersey.api.client.Client;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

/**
 * This is a Java SDK for the Pizza API. It uses jersey-hmac-auth to build requests such that they can be
 * authenticated by the API.
 */
public class PizzaClient {
    private final UriBuilder uriBuilder;
    private final Client jerseyClient;

    public PizzaClient(URI serviceUrl, String apiKey, String secretKey) {
        this.uriBuilder = UriBuilder.fromUri(serviceUrl);
        this.jerseyClient = createClient(apiKey, secretKey);
    }

    public Pizza getPizza() {
        URI uri = uriBuilder.clone()
                .segment("pizza")
                .build();

        return jerseyClient.resource(uri)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(Pizza.class);
    }

    private static Client createClient(String apiKey, String secretKey) {
        Client client = Client.create();
        client.addFilter(new HmacClientFilter(apiKey, secretKey, client.getMessageBodyWorkers()));
        return client;
    }
}
