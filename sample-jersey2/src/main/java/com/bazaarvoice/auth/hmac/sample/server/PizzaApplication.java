package com.bazaarvoice.auth.hmac.sample.server;

import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

import com.bazaarvoice.auth.hmac.server.Authenticator;
import com.bazaarvoice.auth.hmac.server.HmacAuthFeature;

/**
 * Jersey 2.x JAX-RS application that demonstrates HMAC authentication.
 */
public class PizzaApplication<P> extends ResourceConfig {

    private final Binder pizzaApplicationBinder = new AbstractBinder() {
        protected void configure() {
            // The P parameter is to trick HK2 into injecting the Authenticator where it is needed.
            bind(PizzaAuthenticator.class).to(new TypeLiteral<Authenticator<P>>() {});
        }
    };

    public PizzaApplication() {
        // features
        // specify your principal type here
        register(new HmacAuthFeature<String>());

        // dependencies
        register(getPizzaApplicationBinder());

        // resources
        register(PizzaResource2.class);
    }

    protected Binder getPizzaApplicationBinder() {
        return pizzaApplicationBinder;
    }

}