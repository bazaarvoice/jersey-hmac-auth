package com.bazaarvoice.auth.hmac.server;

import com.bazaarvoice.auth.hmac.server.exception.InternalServerException;
import com.bazaarvoice.auth.hmac.server.exception.NotAuthorizedException;
import com.sun.jersey.api.core.HttpRequestContext;

import java.lang.annotation.Annotation;

/**
 * A fake <code>RequestHandler</code> that simply returns the value specified at construction.
 * Most useful for testing purposes.
 *
 * @param <A> the type of annotation to look for (consider using {@link HmacAuth})
 * @param <P> the type of principal the handler returns
 */
public class PassThroughRequestHandler<A extends Annotation, P> implements RequestHandler<A, P> {
    private final P value;

    public PassThroughRequestHandler(P value) {
        this.value = value;
    }

    @Override
    public P handle(A annotation, HttpRequestContext request) throws NotAuthorizedException, InternalServerException {
        return value;
    }
}
