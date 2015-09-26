package com.bazaarvoice.auth.hmac.sample.server;

import com.bazaarvoice.auth.hmac.common.Credentials;
import com.bazaarvoice.auth.hmac.server.Authenticator;

/**
 * Dummy {@link Authenticator} implementation that just checks for the apiKey "fred-api-key".
 */
public class PizzaAuthenticator implements Authenticator<String> {

    public String authenticate(final Credentials credentials) {
        if ("fred-api-key".equals(credentials.getApiKey())) {
            return "fred";
        }
        return null;
    }

}