package com.bazaarvoice.auth.hmac.server;

import com.bazaarvoice.auth.hmac.common.Credentials;
import com.bazaarvoice.auth.hmac.server.exception.InternalServerException;
import com.bazaarvoice.auth.hmac.server.exception.NotAuthorizedException;
import com.google.common.annotations.VisibleForTesting;
import com.sun.jersey.api.core.HttpRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;

/**
 * A strict implementation of a <code>RequestHandler</code>, which requires all requests to an annotated
 * endpoint to contain valid authentication credentials.
 *
 * @param <A> the type of annotation to look for (consider using {@link HmacAuth})
 * @param <P> the type of principal the handler returns
 */
public class DefaultRequestHandler<A extends Annotation, P> implements RequestHandler<A, P> {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultRequestHandler.class);

    private final RequestDecoder requestDecoder;
    private final Authenticator<P> authenticator;
    private final Authorizer<A, P> authorizer;

    public DefaultRequestHandler(Authenticator<P> authenticator) {
        this(authenticator, null);
    }

    public DefaultRequestHandler(Authenticator<P> authenticator, Authorizer<A, P> authorizer) {
        this(new RequestDecoder(), authenticator, authorizer);
    }

    @VisibleForTesting
    DefaultRequestHandler(RequestDecoder requestDecoder, Authenticator<P> authenticator, Authorizer<A, P> authorizer) {
        this.requestDecoder = requestDecoder;
        this.authenticator = authenticator;
        this.authorizer = authorizer;
    }

    @Override
    public P handle(A annotation, HttpRequestContext request) throws NotAuthorizedException, InternalServerException {
        try {
            Credentials credentials = requestDecoder.decode(request);
            P principal = authenticator.authenticate(credentials);

            if (principal != null && (authorizer == null || authorizer.authorize(annotation, principal))) {
                return principal;
            }
        } catch (IllegalArgumentException e) {
            LOG.info("Error decoding credentials: {}", e.getMessage());
            throw new NotAuthorizedException();
        } catch (Exception e) {
            LOG.warn("Error while authenticating credentials", e);
            throw new InternalServerException();
        }

        throw new NotAuthorizedException();
    }
}
