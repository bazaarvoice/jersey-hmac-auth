package com.bazaarvoice.auth.hmac.dropwizard.server;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;


public class PizzaApplication extends Application<Configuration> {

    public static void main(String[] args) throws Exception {
        new PizzaApplication().run(args);
    }

    @Override
    public String getName() {
        return "pizza-application";
    }

    @Override
    public void initialize(Bootstrap<Configuration> bootstrap) {

    }

    @Override
    public void run(Configuration configuration, Environment environment) throws Exception {

    }
}
