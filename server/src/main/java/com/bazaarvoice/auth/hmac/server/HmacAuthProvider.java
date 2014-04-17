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
 * @param <AnnotationType> the type of annotation to look for (consider using {@link HmacAuth})
 * @param <PrincipalType> the type of principal the {@link RequestHandler} returns
 */
public abstract class HmacAuthProvider<AnnotationType extends Annotation, PrincipalType> implements InjectableProvider<AnnotationType, Parameter> {
    private final RequestHandler<AnnotationType, PrincipalType> requestHandler;

    public HmacAuthProvider(RequestHandler<AnnotationType, PrincipalType> requestHandler) {
        this.requestHandler = requestHandler;
    }

    @Override
    public ComponentScope getScope() {
        return ComponentScope.PerRequest;
    }

    @Override
    public Injectable getInjectable(ComponentContext componentContext, AnnotationType annotation, Parameter parameter) {
        return new HmacAuthInjectable<>(annotation, requestHandler);
    }

    private static class HmacAuthInjectable<AnnotationType extends Annotation, PrincipalType> extends AbstractHttpContextInjectable<PrincipalType> {
        private final AnnotationType annotation;
        private final RequestHandler<AnnotationType, PrincipalType> requestHandler;

        private HmacAuthInjectable(AnnotationType annotation, RequestHandler<AnnotationType, PrincipalType> requestHandler) {
            this.annotation = annotation;
            this.requestHandler = requestHandler;
        }

        @Override
        public PrincipalType getValue(HttpContext httpContext) {
            return requestHandler.handle(annotation, httpContext.getRequest());
        }
    }
}
