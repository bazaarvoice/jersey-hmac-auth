package com.bazaarvoice.auth.hmac.server;

import com.bazaarvoice.auth.hmac.common.Credentials;
import com.bazaarvoice.auth.hmac.server.exception.InternalServerException;
import com.bazaarvoice.auth.hmac.server.exception.NotAuthorizedException;
import com.google.common.annotations.VisibleForTesting;
import com.sun.jersey.api.core.HttpRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A relaxed implementation of a <code>RequestHandler</code>, which does not require a request
 * to contain authentication credentials, but still validates credentials if provided.
 *
 * @param <Principal> the type of principal the handler returns
 */
public class OptionalRequestHandler<Principal> implements RequestHandler<Principal> {
    private static final Logger LOG = LoggerFactory.getLogger(OptionalRequestHandler.class);

    private final RequestDecoder requestDecoder;
    private final Authenticator<Principal> authenticator;

    public OptionalRequestHandler(Authenticator<Principal> authenticator) {
        this(new RequestDecoder(), authenticator);
    }

    @VisibleForTesting
    OptionalRequestHandler(RequestDecoder requestDecoder, Authenticator<Principal> authenticator) {
        this.requestDecoder = requestDecoder;
        this.authenticator = authenticator;
    }

    @Override
    public Principal handle(HttpRequestContext request) throws NotAuthorizedException, InternalServerException {
        try {
            Credentials credentials = requestDecoder.decode(request);
            Principal result = authenticator.authenticate(credentials);
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
