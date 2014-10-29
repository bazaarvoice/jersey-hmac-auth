package com.bazaarvoice.auth.hmac.server;

import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;

public class HmacAuthProvider<T> implements InjectableProvider<HmacAuth, Parameter> {
    private final RequestHandler<T> requestHandler;

    public HmacAuthProvider(RequestHandler<T> requestHandler) {
        this.requestHandler = requestHandler;
    }

    @Override
    public ComponentScope getScope() {
        return ComponentScope.PerRequest;
    }

    @Override
    public Injectable getInjectable(ComponentContext componentContext, HmacAuth hmacAuth, Parameter parameter) {
        return new HmacAuthInjectable<T>(requestHandler);
    }

    private static class HmacAuthInjectable<T> extends AbstractHttpContextInjectable<T> {
        private final RequestHandler<T> requestHandler;

        private HmacAuthInjectable( RequestHandler<T> requestHandler) {
            this.requestHandler = requestHandler;
        }

        @Override
        public T getValue(HttpContext httpContext) {
            return requestHandler.handle(httpContext.getRequest());
        }
    }
}
