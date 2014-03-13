package com.bazaarvoice.auth.hmac.sample.server;

import com.bazaarvoice.auth.hmac.server.HmacAuthProvider;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;

public class PizzaService extends Service<Configuration> {

    public static void main(String[] args) throws Exception {
        new PizzaService().run(args);
    }

    @Override
    public void initialize(Bootstrap<Configuration> bootstrap) {
        bootstrap.setName("pizza-application");
    }

    @Override
    public void run(Configuration configuration, Environment environment) throws Exception {
        environment.addResource(new PizzaResource());
        environment.addHealthCheck(new PizzaHealthCheck());
        environment.addProvider(new HmacAuthProvider<>(new PizzaAuthenticator()));
    }
}
