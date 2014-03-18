package com.bazaarvoice.auth.hmac.server;

import com.bazaarvoice.auth.hmac.server.exception.InternalServerException;
import com.bazaarvoice.auth.hmac.server.exception.NotAuthorizedException;
import com.sun.jersey.api.core.HttpRequestContext;

/**
 * A fake <code>RequestHandler</code> that simply returns the value specified at construction.
 * Most useful for testing purposes.
 *
 * @param <Principal> the type of principal the handler returns
 */
public class PassThroughRequestHandler<Principal> implements RequestHandler<Principal> {
    private final Principal value;

    public PassThroughRequestHandler(Principal value) {
        this.value = value;
    }

    @Override
    public Principal handle(HttpRequestContext request) throws NotAuthorizedException, InternalServerException {
        return value;
    }
}
