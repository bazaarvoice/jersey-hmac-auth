package com.bazaarvoice.auth.hmac.server;

import static org.apache.commons.lang.Validate.notNull;
import static org.glassfish.jersey.server.model.Parameter.Source.UNKNOWN;

import javax.inject.Inject;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.internal.inject.AbstractValueFactoryProvider;
import org.glassfish.jersey.server.internal.inject.MultivaluedParameterExtractorProvider;
import org.glassfish.jersey.server.model.Parameter;

/**
 * {@link org.glassfish.jersey.server.spi.internal.ValueFactoryProvider
 * ValueFactoryProvider} that makes a {@link PrincipalFactory} available to the
 * request if an {@link HmacAuth} annotation is present.
 *
 * @param <P> the type of principal
 */
public class PrincipalValueFactoryProvider<P> extends AbstractValueFactoryProvider {

    private final PrincipalFactory<? extends P> factory;

    @Inject
    public PrincipalValueFactoryProvider(final MultivaluedParameterExtractorProvider mpep,
            final ServiceLocator locator, final PrincipalFactory<P> factory) {
        super(mpep, locator, UNKNOWN);
        notNull(factory, "factory cannot be null");
        this.factory = factory;
    }

    protected Factory<? extends P> createValueFactory(final Parameter parameter) {
        final HmacAuth auth = parameter.getAnnotation(HmacAuth.class);
        if (auth != null) {
            // TODO introduce type checking
            return getFactory();
        }
        return null;
    }

    protected PrincipalFactory<? extends P> getFactory() {
        return factory;
    }

}