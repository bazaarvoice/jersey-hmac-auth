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
 * A strict implementation of a <code>RequestHandler</code>, which requires all requests to an annotated
 * endpoint to contain valid authentication credentials.
 *
 * @param <AnnotationType> the type of annotation to look for (consider using {@link HmacAuth})
 * @param <PrincipalType> the type of principal the handler returns
 */
public class DefaultRequestHandler<AnnotationType extends Annotation, PrincipalType> implements RequestHandler<AnnotationType, PrincipalType> {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultRequestHandler.class);

    private final RequestDecoder requestDecoder;
    private final Authenticator<PrincipalType> authenticator;
    private final Authorizer<AnnotationType, PrincipalType> authorizer;

    public DefaultRequestHandler(Authenticator<PrincipalType> authenticator) {
        this(authenticator, new AllAuthorizer<AnnotationType, PrincipalType>());
    }

    public DefaultRequestHandler(Authenticator<PrincipalType> authenticator, RequestConfiguration requestConfiguration) {
        this(new RequestDecoder(requestConfiguration), authenticator, new AllAuthorizer<AnnotationType, PrincipalType>());
    }

    public DefaultRequestHandler(Authenticator<PrincipalType> authenticator, Authorizer<AnnotationType, PrincipalType> authorizer) {
        this(new RequestDecoder(new RequestConfiguration()), authenticator, authorizer);
    }

    public DefaultRequestHandler(Authenticator<PrincipalType> authenticator, Authorizer<AnnotationType, PrincipalType> authorizer, RequestConfiguration requestConfiguration) {
        this(new RequestDecoder(requestConfiguration), authenticator, authorizer);
    }

    @VisibleForTesting
    DefaultRequestHandler(RequestDecoder requestDecoder, Authenticator<PrincipalType> authenticator, Authorizer<AnnotationType, PrincipalType> authorizer) {
        this.requestDecoder = requestDecoder;
        this.authenticator = authenticator;
        this.authorizer = authorizer;
    }

    @Override
    public PrincipalType handle(AnnotationType annotation, HttpRequestContext request) throws NotAuthorizedException, InternalServerException {
        try {
            Credentials credentials = requestDecoder.decode(request);
            PrincipalType principal = authenticator.authenticate(credentials);

            if (principal != null && authorizer.authorize(annotation, principal)) {
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
