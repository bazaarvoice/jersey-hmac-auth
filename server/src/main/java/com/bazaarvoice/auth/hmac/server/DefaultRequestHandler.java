package com.bazaarvoice.auth.hmac.server;

import com.bazaarvoice.auth.hmac.common.Credentials;
import com.bazaarvoice.auth.hmac.common.RequestConfiguration;
import com.bazaarvoice.auth.hmac.server.exception.InternalServerException;
import com.bazaarvoice.auth.hmac.server.exception.NotAuthorizedException;
import com.google.common.annotations.VisibleForTesting;
import com.sun.jersey.api.core.HttpRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A strict implementation of a <code>RequestHandler</code>, which requires all requests to an annotated
 * endpoint to contain valid authentication credentials.
 *
 * @param <Principal> the type of principal the handler returns
 */
public class DefaultRequestHandler<Principal> implements RequestHandler<Principal> {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultRequestHandler.class);

    private final RequestDecoder requestDecoder;
    private final Authenticator<Principal> authenticator;

    public DefaultRequestHandler(Authenticator<Principal> authenticator) {
        this(new RequestDecoder(new RequestConfiguration()), authenticator);
    }

    public DefaultRequestHandler(Authenticator<Principal> authenticator, RequestConfiguration requestConfiguration) {
        this(new RequestDecoder(requestConfiguration), authenticator);
    }

    @VisibleForTesting
    DefaultRequestHandler(RequestDecoder requestDecoder, Authenticator<Principal> authenticator) {
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
            LOG.info("Error decoding credentials: {}", e.getMessage());
            throw new NotAuthorizedException();
        } catch (Exception e) {
            LOG.warn("Error while authenticating credentials", e);
            throw new InternalServerException();
        }

        throw new NotAuthorizedException();
    }
}
