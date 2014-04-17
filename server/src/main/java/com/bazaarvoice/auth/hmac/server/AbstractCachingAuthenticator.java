package com.bazaarvoice.auth.hmac.server;

import com.bazaarvoice.auth.hmac.common.Credentials;
import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public abstract class AbstractCachingAuthenticator<P> extends AbstractAuthenticator<P> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractCachingAuthenticator.class);

    private final Cache<String, Optional<P>> cache;

    protected AbstractCachingAuthenticator(long allowedTimestampSlop, long cacheTimeout, TimeUnit timeUnit, long maxCacheElements) {
        super(allowedTimestampSlop, timeUnit);
        cache = CacheBuilder.newBuilder()
            .maximumSize(maxCacheElements)
            .expireAfterWrite(cacheTimeout, timeUnit)
            .build();
    }

    /**
     * Clear out all elements from the cache.
     */
    protected void clearCache() {
        cache.invalidateAll();
    }

    /**
     * Do the loading of the P based on the Credentials.  Note that this will only be called if the Credentials
     * object is not found in the in-memory cache.
     * <p/>
     * Note: it is safe to return null from this method if the P is not found for these Credentials, and that will be cached.
     */
    protected abstract P loadPrincipal(Credentials credentials);

    /**
     * If the P for this Credentials is already cached, return it.  Otherwise call {@link #loadPrincipal} and cache the results.
     */
    @Override
    protected final P getPrincipal(final Credentials credentials) {
        try {
            Optional<P> principalOptional = cache.get(credentials.getApiKey(), new Callable<Optional<P>>() {
                public Optional<P> call() throws Exception {
                    return Optional.fromNullable(loadPrincipal(credentials));
                }
            });
            return principalOptional.orNull();
        } catch (ExecutionException e) {
            LOG.warn("Exception when loading the cache for credentials with API key " + credentials.getApiKey());
            return null;
        }
    }
}
