package com.bazaarvoice.auth.hmac.sample.server.auth;

import com.bazaarvoice.auth.hmac.common.Credentials;
import com.bazaarvoice.auth.hmac.server.AbstractCachingAuthenticator;

import java.util.concurrent.TimeUnit;

public class SimpleAuthenticator extends AbstractCachingAuthenticator<User> {
    private static final long ALLOWED_TIMESTAMP_SLOP_MINUTES = 5;
    private static final long CACHE_TIMEOUT_MINUTES = 1;
    private static final long MAX_CACHE_ELEMENTS = 1000;

    public static final User[] USERS = new User[]{
            new User("Admin", UserRole.ADMINISTRATOR, "admin-key", "admin-secret"),
            new User("User", UserRole.USER, "user-key", "user-secret")
    };

    public SimpleAuthenticator() {
        super(ALLOWED_TIMESTAMP_SLOP_MINUTES, CACHE_TIMEOUT_MINUTES, TimeUnit.MINUTES, MAX_CACHE_ELEMENTS);
    }

    @Override
    protected User loadPrincipal(final Credentials credentials) {
        for (User user : USERS) {
            if (user.getApiKey().equals(credentials.getApiKey())) {
                return user;
            }
        }

        return null;
    }

    @Override
    protected String getSecretKeyFromPrincipal(final User user) {
        return user.getSecretKey();
    }
}
