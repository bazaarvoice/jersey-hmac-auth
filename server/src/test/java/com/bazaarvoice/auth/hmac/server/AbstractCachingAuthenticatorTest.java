package com.bazaarvoice.auth.hmac.server;

import com.bazaarvoice.auth.hmac.common.Credentials;
import com.bazaarvoice.auth.hmac.server.exception.AuthenticationException;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.bazaarvoice.auth.hmac.server.TestCredentials.createCredentials;
import static org.junit.Assert.assertEquals;

public class AbstractCachingAuthenticatorTest {

    public static final Credentials aCredentials = createCredentials("a", "a");
    public static final Credentials bCredentials = createCredentials("b", "b");
    public static final Credentials cCredentials = createCredentials("c", "c");

    @Test
    public void testCacheWorks() throws AuthenticationException {
        // do two requests for 'a', three requests for 'b', one request for 'c'; make sure we get only three calls to load
        Authenticator unit = new Authenticator(5000, 20);

        unit.authenticate(aCredentials);
        unit.authenticate(aCredentials);

        unit.authenticate(bCredentials);
        unit.authenticate(bCredentials);
        unit.authenticate(bCredentials);

        unit.authenticate(cCredentials);

        assertEquals(3, unit.getNumLoads());
    }

    @Test
    public void testClearCache() throws AuthenticationException {
        // do three requests for 'b', followed by a cache clear, then two requests for 'b'; ensure we get only 2 calls to load
        Authenticator unit = new Authenticator(5000, 20);

        unit.authenticate(bCredentials);
        unit.authenticate(bCredentials);
        unit.authenticate(bCredentials);

        unit.clearCache();

        unit.authenticate(bCredentials);
        unit.authenticate(bCredentials);

        assertEquals(2, unit.getNumLoads());
    }

    @Test
    public void testCacheSize() throws AuthenticationException {
        // do a request for each of 'a', 'b', 'c' in a cache that can't hold that many, and do it several times;
        // ensure that the cache was never used
        Authenticator unit = new Authenticator(5000, 2); // only holds 2 things

        unit.authenticate(aCredentials);
        unit.authenticate(bCredentials);
        unit.authenticate(cCredentials);

        unit.authenticate(aCredentials);
        unit.authenticate(bCredentials);
        unit.authenticate(cCredentials);

        unit.authenticate(aCredentials);
        unit.authenticate(bCredentials);
        unit.authenticate(cCredentials);

        assertEquals(9, unit.getNumLoads());
    }

    @Test
    public void testCacheExpiration() throws AuthenticationException, InterruptedException {
        // do a request for each of 'a', 'b', 'c' a few times, then sleep a bit longer than the cache timeout;
        // ensure that the cache was invalidated appropriately
        Authenticator unit = new Authenticator(300, 20);

        unit.authenticate(aCredentials);
        unit.authenticate(bCredentials);
        unit.authenticate(cCredentials);

        unit.authenticate(aCredentials);
        unit.authenticate(bCredentials);
        unit.authenticate(cCredentials);

        Thread.sleep(310);

        unit.authenticate(aCredentials);
        unit.authenticate(aCredentials);
        unit.authenticate(aCredentials);
        unit.authenticate(bCredentials);
        unit.authenticate(bCredentials);
        unit.authenticate(bCredentials);
        unit.authenticate(cCredentials);
        unit.authenticate(cCredentials);
        unit.authenticate(cCredentials);

        assertEquals(6, unit.getNumLoads());
    }

    @Test
    public void testApiKeyIsTheKey() throws AuthenticationException, InterruptedException {
        // two requests that are different in time or path alone should still resolve to the same cache

        Authenticator unit = new Authenticator(300, 20);

        unit.authenticate(aCredentials);
        Thread.sleep(10);
        unit.authenticate(createCredentials("a", "a"));

        assertEquals(1, unit.getNumLoads());
    }

    private static class Authenticator extends AbstractCachingAuthenticator<SimplePrincipal> {
        private final AtomicInteger numLoads = new AtomicInteger(0);

        public int getNumLoads() {
            return numLoads.get();
        }

        public Authenticator(int cacheTimeoutMillis, int maxCacheElements) {
            super(5000, cacheTimeoutMillis, TimeUnit.MILLISECONDS, maxCacheElements);
        }

        @Override
        protected SimplePrincipal loadPrincipal(Credentials credentials) {
            numLoads.incrementAndGet();
            return new SimplePrincipal(credentials.getApiKey());
        }

        @Override
        protected String getSecretKeyFromPrincipal(SimplePrincipal principal) {
            return principal.id;
        }
    }

    static class SimplePrincipal {
        private final String id;

        SimplePrincipal(String id) {
            this.id = id;
        }
    }
}
