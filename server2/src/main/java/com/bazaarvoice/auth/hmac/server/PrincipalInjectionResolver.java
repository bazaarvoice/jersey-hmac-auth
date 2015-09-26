package com.bazaarvoice.auth.hmac.server;

import javax.inject.Singleton;

import org.glassfish.jersey.server.internal.inject.ParamInjectionResolver;

/**
 * {@link org.glassfish.hk2.api.InjectionResolver InjectionResolver} for
 * injecting a principal wherever the {@link HmacAuth} annotation is specified.
 */
@Singleton
public class PrincipalInjectionResolver extends ParamInjectionResolver<HmacAuth> {

    public PrincipalInjectionResolver() {
        super(PrincipalValueFactoryProvider.class);
    }

}