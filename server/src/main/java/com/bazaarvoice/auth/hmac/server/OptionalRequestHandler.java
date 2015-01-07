package com.bazaarvoice.auth.hmac.server;

import com.bazaarvoice.auth.hmac.common.Credentials;
import com.bazaarvoice.auth.hmac.common.RequestConfiguration;
import com.bazaarvoice.auth.hmac.server.exception.InternalServerException;
import com.bazaarvoice.auth.hmac.server.exception.NotAuthorizedException;
import com.google.common.annotations.VisibleForTesting;
import com.sun.jersey.api.core.HttpRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;

/**
 * A relaxed implementation of a <code>RequestHandler</code>, which does not require a request
 * to contain authentication credentials, but still validates credentials if provided.
 *
 * @param <A> the type of annotation to look for (consider using {@link HmacAuth})
 * @param <P> the type of principal the handler returns
 */
public class OptionalRequestHandler<A extends Annotation, P> implements RequestHandler<A, P> {
    private static final Logger LOG = LoggerFactory.getLogger(OptionalRequestHandler.class);

    private final RequestDecoder requestDecoder;
    private final Authenticator<P> authenticator;

    public OptionalRequestHandler(Authenticator<Principal> authenticator) {
        this(new RequestDecoder(new RequestConfiguration()), authenticator);
    }

    public OptionalRequestHandler(Authenticator<Principal> authenticator, RequestConfiguration requestConfiguration) {
        this(new RequestDecoder(requestConfiguration), authenticator);
    }

    @VisibleForTesting
    OptionalRequestHandler(RequestDecoder requestDecoder, Authenticator<P> authenticator) {
        this.requestDecoder = requestDecoder;
        this.authenticator = authenticator;
    }

    @Override
    public P handle(A annotation, HttpRequestContext request) throws NotAuthorizedException, InternalServerException {
        try {
            Credentials credentials = requestDecoder.decode(request);
            P result = authenticator.authenticate(credentials);
            if (result != null) {
                return result;
            }
        } catch (IllegalArgumentException e) {
            return null;
        } catch (Exception e) {
            LOG.warn("Error while authenticating credentials", e);
            throw new InternalServerException();
        }

        throw new NotAuthorizedException();
    }
}
