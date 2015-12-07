package com.bazaarvoice.auth.hmac.server;

import com.bazaarvoice.auth.hmac.server.exception.InternalServerException;
import com.bazaarvoice.auth.hmac.server.exception.NotAuthorizedException;
import com.sun.jersey.api.core.HttpRequestContext;

import java.lang.annotation.Annotation;

/**
 * A fake <code>RequestHandler</code> that simply returns the value specified at construction.
 * Most useful for testing purposes.
 *
 * @param <AnnotationType> the type of annotation to look for (consider using {@link HmacAuth})
 * @param <PrincipalType> the type of principal the handler returns
 */
public class PassThroughRequestHandler<AnnotationType extends Annotation, PrincipalType> implements RequestHandler<AnnotationType, PrincipalType> {
    private final PrincipalType value;

    public PassThroughRequestHandler(PrincipalType value) {
        this.value = value;
    }

    @Override
    public PrincipalType handle(AnnotationType annotation, HttpRequestContext request) throws NotAuthorizedException, InternalServerException {
        return value;
    }
}
