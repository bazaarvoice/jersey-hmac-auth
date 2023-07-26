package com.bazaarvoice.auth.hmac.dropwizard.server;

import com.bazaarvoice.auth.hmac.common.Credentials;
import com.bazaarvoice.auth.hmac.server.AbstractCachingAuthenticator;

import java.util.concurrent.TimeUnit;

public class PizzaAuthenticator extends AbstractCachingAuthenticator<String> {
    private static final long ALLOWED_TIMESTAMP_SLOP_MINUTES = 5;
    private static final long CACHE_TIMEOUT_MINUTES = 1;
    private static final long MAX_CACHE_ELEMENTS = 1000;

    public PizzaAuthenticator() {
        super(ALLOWED_TIMESTAMP_SLOP_MINUTES, CACHE_TIMEOUT_MINUTES, TimeUnit.MINUTES, MAX_CACHE_ELEMENTS);
    }

    @Override
    protected String loadPrincipal(Credentials credentials) {
        // For simplicity, only support one hard-coded API key
        if ("fred-api-key".equals(credentials.getApiKey())) {
            return "fred";
        }
        return null;
    }

    @Override
    protected String getSecretKeyFromPrincipal(String principal) {
        if ("fred".equals(principal)) {
            return "fred-secret-key";
        }
        return null;
    }
}
