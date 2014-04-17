package com.bazaarvoice.auth.hmac.server;

import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;

import java.lang.annotation.Annotation;

/**
 * An implementation of Jersey's InjectableProvider to perform the actual integration with Jersey.
 *
 * @param <A> the type of annotation to look for (consider using {@link HmacAuth})
 * @param <P> the type of principal the handler returns
 */
public class HmacAuthProvider<A extends Annotation, P> implements InjectableProvider<A, Parameter> {
    private final RequestHandler<P> requestHandler;

    public HmacAuthProvider(RequestHandler<P> requestHandler) {
        this.requestHandler = requestHandler;
    }

    @Override
    public ComponentScope getScope() {
        return ComponentScope.PerRequest;
    }

    @Override
    public Injectable getInjectable(ComponentContext componentContext, A annotation, Parameter parameter) {
        return new HmacAuthInjectable<>(requestHandler);
    }

    private static class HmacAuthInjectable<T> extends AbstractHttpContextInjectable<T> {
        private final RequestHandler<T> requestHandler;

        private HmacAuthInjectable(RequestHandler<T> requestHandler) {
            this.requestHandler = requestHandler;
        }

        @Override
        public T getValue(HttpContext httpContext) {
            return requestHandler.handle(httpContext.getRequest());
        }
    }
}
