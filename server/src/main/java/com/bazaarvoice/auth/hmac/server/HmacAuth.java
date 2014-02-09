package com.bazaarvoice.auth.hmac.server;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to inject authenticated principal objects into protected
 * Jersey resource methods.
 */
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HmacAuth {
    /**
     * If true, authentication is required in order for requests to be processed. If false,
     * requests will be processed even if they are not authenticated. In this case, authentication
     * is considered "optional", and for any requests that are not authenticated, a null will
     * be passed in to the resource method as the principal object. Defaults to true.
     */
    boolean required() default true;
}

