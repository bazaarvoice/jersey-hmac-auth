package com.bazaarvoice.auth.hmac.dropwizard.client;

import com.bazaarvoice.auth.hmac.dropwizard.Pizza;
import com.google.common.base.Joiner;

import java.net.URI;

public class PizzaClientTest {
    // This is the default endpoint for the Pizza service when you run it
    private static final URI ENDPOINT = URI.create("http://localhost:8080");

    // These keys are defined in PizzaAuthenticator
    private static final String API_KEY = "fred-api-key";
    private static final String SECRET_KEY = "fred-secret-key";

    public static void main(String[] args) throws Exception {
        // Get a pizza
        PizzaClient pizzaClient = new PizzaClient(ENDPOINT, API_KEY, SECRET_KEY);
        Pizza pizza = pizzaClient.getPizza();

        // Print out the results
        System.out.printf("Pizza is size '%s' with toppings: %s%n",
                pizza.getSize(),
                Joiner.on(", ").join(pizza.getToppings()));
    }
}
