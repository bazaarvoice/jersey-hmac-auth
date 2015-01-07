package com.bazaarvoice.auth.hmac.server;

import java.lang.annotation.Annotation;

/**
 * An interface for classes which authorize the annotation against the principal.
 *
 * @param <A> the type of annotation to look for (consider using {@link HmacAuth})
 * @param <P> the type of principal the handler returns
 */
public interface Authorizer<A extends Annotation, P> {
    /**
     * Given the annotation, which includes authorization requirements, and a verified
     * principal, return whether the request is authorized.
     *
     * @param annotation the specific annotation on the invoked method
     * @param principal the specific pre-authenticated user principal
     * @return true if user is allowed access to method
     */
    boolean authorize(A annotation, P principal);
}
