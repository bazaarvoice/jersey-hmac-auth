package com.bazaarvoice.auth.hmac.server;

import com.bazaarvoice.auth.hmac.server.exception.InternalServerException;
import com.bazaarvoice.auth.hmac.server.exception.NotAuthorizedException;
import com.sun.jersey.api.core.HttpRequestContext;

/**
 * An interface for classes which handle server requests with regards to authentication
 *
 * @param <Principal> the type of principal the handler returns
 */
public interface RequestHandler<Principal> {
    /**
     * Given a request, return a principal.
     * <p/>
     * If the credentials are valid and map to a principal, returns a principal object or null.
     * <p/>
     * If the credentials are invalid, throws a <code>NotAuthorizedException</code>;
     * <p/>
     * If the credentials cannot be validated due to an underlying error condition, throws an
     * <code>InternalServerException</code> to indicate that an internal error occurred.
     * <p/>
     * Otherwise it's up to the handler to determine when to return a value, or throw a
     * <code>NotAuthorizedException</code> or <code>InternalServerException</code>
     *
     * @param request the request context associated with a server request
     * @return either an authenticated principal or null
     */
    Principal handle(HttpRequestContext request) throws NotAuthorizedException, InternalServerException;
}
