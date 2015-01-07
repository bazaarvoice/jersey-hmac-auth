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
 * AnnotationType relaxed implementation of a <code>RequestHandler</code>, which does not require a request
 * to contain authentication credentials, but still validates credentials if provided.
 *
 * @param <AnnotationType> the type of annotation to look for (consider using {@link HmacAuth})
 * @param <PrincipalType> the type of principal the handler returns
 */
public class OptionalRequestHandler<AnnotationType extends Annotation, PrincipalType> implements RequestHandler<AnnotationType, PrincipalType> {
    private static final Logger LOG = LoggerFactory.getLogger(OptionalRequestHandler.class);

    private final RequestDecoder requestDecoder;
    private final Authenticator<PrincipalType> authenticator;

    public OptionalRequestHandler(Authenticator<PrincipalType> authenticator) {
        this(new RequestDecoder(new RequestConfiguration()), authenticator);
    }

    public OptionalRequestHandler(Authenticator<PrincipalType> authenticator, RequestConfiguration requestConfiguration) {
        this(new RequestDecoder(requestConfiguration), authenticator);
    }

    @VisibleForTesting
    OptionalRequestHandler(RequestDecoder requestDecoder, Authenticator<PrincipalType> authenticator) {
        this.requestDecoder = requestDecoder;
        this.authenticator = authenticator;
    }

    @Override
    public PrincipalType handle(AnnotationType annotation, HttpRequestContext request) throws NotAuthorizedException, InternalServerException {
        try {
            Credentials credentials = requestDecoder.decode(request);
            PrincipalType result = authenticator.authenticate(credentials);
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
