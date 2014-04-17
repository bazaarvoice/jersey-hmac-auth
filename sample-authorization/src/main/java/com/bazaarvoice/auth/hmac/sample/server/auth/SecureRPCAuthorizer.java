package com.bazaarvoice.auth.hmac.sample.server.auth;

import com.bazaarvoice.auth.hmac.server.Authorizer;

public class SecureRPCAuthorizer implements Authorizer<SecureRPC, User> {
    @Override
    public boolean authorize(final SecureRPC annotation, final User principal) {
        return principal.hasRights(annotation.requiredRights());
    }
}
