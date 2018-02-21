package com.bazaarvoice.auth.hmac.server;

import com.bazaarvoice.auth.hmac.common.Credentials;

/**
 * An interface for classes which authenticate user-supplied credentials and return principal
 * objects
 *
 * @param <Principal> the type of principal the authenticator returns
 */
public interface Authenticator<Principal> {

    String DEFAULT_API_KEY_PARAM = "apiKey";

    /**
     * Configuration point that helps customize query parameter name which is used to
     * lookup query for credentials.
     *
     * @return parameter name
     */
    String getApiKeyName();

    /**
     * Given a set of user-supplied credentials, return an principal.
     * <p/>
     * If the credentials are valid and map to a principal, returns a non-null principal object.
     * <p/>
     * If the credentials are invalid, returns null;
     * <p/>
     * If the credentials cannot be validated due to an underlying error condition, throws an
     * <code>AuthenticationException</code> to indicate that an internal error occurred.
     *
     * @param credentials a set of user-supplied credentials
     * @return either an authenticated principal or null
     */
    Principal authenticate(Credentials credentials);
}
