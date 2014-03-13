package com.bazaarvoice.auth.hmac.sample.server;

import com.yammer.metrics.core.HealthCheck;

public class PizzaHealthCheck extends HealthCheck {

    public PizzaHealthCheck() {
        super("pizza-application");
    }

    @Override
    protected Result check() throws Exception {
        // The service is always healthy, right?
        return Result.healthy();
    }
}
