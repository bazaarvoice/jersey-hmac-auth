package com.bazaarvoice.auth.hmac.server.exception;

/**
 * An exception thrown to indicate that an Authenticator is unable to check the
 * validity of the given credentials. It is used to indicate an internal server
 * error and NOT to indicate that the credentials are invalid.
 */
public class AuthenticationException extends Exception {
    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthenticationException(Throwable cause) {
        super(cause);
    }
}
