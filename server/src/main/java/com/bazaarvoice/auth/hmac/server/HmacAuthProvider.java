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
public abstract class HmacAuthProvider<A extends Annotation, P> implements InjectableProvider<A, Parameter> {
    private final RequestHandler<A, P> requestHandler;

    public HmacAuthProvider(RequestHandler<A, P> requestHandler) {
        this.requestHandler = requestHandler;
    }

    @Override
    public ComponentScope getScope() {
        return ComponentScope.PerRequest;
    }

    @Override
    public Injectable getInjectable(ComponentContext componentContext, A annotation, Parameter parameter) {
        return new HmacAuthInjectable<>(annotation, requestHandler);
    }

    private static class HmacAuthInjectable<A extends Annotation, P> extends AbstractHttpContextInjectable<P> {
        private final A annotation;
        private final RequestHandler<A, P> requestHandler;

        private HmacAuthInjectable(A annotation, RequestHandler<A, P> requestHandler) {
            this.annotation = annotation;
            this.requestHandler = requestHandler;
        }

        @Override
        public P getValue(HttpContext httpContext) {
            return requestHandler.handle(annotation, httpContext.getRequest());
        }
    }
}
