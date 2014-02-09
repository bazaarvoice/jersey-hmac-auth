package com.bazaarvoice.auth.hmac.server;

import com.bazaarvoice.auth.hmac.server.exception.AuthenticationException;
import com.bazaarvoice.auth.hmac.server.exception.InternalServerException;
import com.bazaarvoice.auth.hmac.server.exception.NotAuthorizedException;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HmacAuthProvider<T> implements InjectableProvider<HmacAuth, Parameter> {
    private static final Logger LOG = LoggerFactory.getLogger(HmacAuthProvider.class);

    private final Authenticator<T> authenticator;
    private final RequestDecoder requestDecoder;

    public HmacAuthProvider(Authenticator<T> authenticator) {
        this.authenticator = authenticator;
        this.requestDecoder = new RequestDecoder();
    }

    @Override
    public ComponentScope getScope() {
        return ComponentScope.PerRequest;
    }

    @Override
    public Injectable getInjectable(ComponentContext componentContext, HmacAuth hmacAuth, Parameter parameter) {
        return new HmacAuthInjectable<>(authenticator, requestDecoder, hmacAuth.required());
    }

    private static class HmacAuthInjectable<T> extends AbstractHttpContextInjectable<T> {
        private final Authenticator<T> authenticator;
        private final RequestDecoder requestDecoder;
        private final boolean required;

        private HmacAuthInjectable(Authenticator<T> authenticator, RequestDecoder requestDecoder, boolean required) {
            this.authenticator = authenticator;
            this.requestDecoder = requestDecoder;
            this.required = required;
        }

        @Override
        public T getValue(HttpContext httpContext) {
            try {
                Credentials credentials = requestDecoder.decode(httpContext.getRequest());
                T result = authenticator.authenticate(credentials);
                if (result != null) {
                    return result;
                }

            } catch (IllegalArgumentException e) {
                LOG.debug("Error decoding credentials", e);
            } catch (AuthenticationException e) {
                LOG.warn("Error authenticating credentials", e);
                throw new InternalServerException();
            }

            if (required) {
                throw new NotAuthorizedException();
            }

            return null;
        }
    }
}
