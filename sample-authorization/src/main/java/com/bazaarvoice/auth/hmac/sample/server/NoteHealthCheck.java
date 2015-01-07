package com.bazaarvoice.auth.hmac.sample.server;

import com.yammer.metrics.core.HealthCheck;

public class NoteHealthCheck extends HealthCheck {

    public NoteHealthCheck() {
        super("note-doctor");
    }

    @Override
    protected Result check() throws Exception {
        // The service is always healthy, right?
        return Result.healthy();
    }
}
