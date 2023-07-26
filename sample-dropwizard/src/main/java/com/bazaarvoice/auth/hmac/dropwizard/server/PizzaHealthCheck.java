package com.bazaarvoice.auth.hmac.dropwizard.server;

import com.codahale.metrics.health.HealthCheck;

public class PizzaHealthCheck extends HealthCheck {

    public PizzaHealthCheck() {
        super();
    }

    @Override
    protected Result check() throws Exception {
        // The service is always healthy, right?
        return Result.healthy();
    }
}
