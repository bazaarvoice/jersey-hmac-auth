package com.bazaarvoice.auth.hmac.server;

import com.bazaarvoice.auth.hmac.common.Credentials;
import com.bazaarvoice.auth.hmac.common.SignatureGenerator;
import com.bazaarvoice.auth.hmac.common.TimeUtils;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.util.concurrent.TimeUnit;

import static com.bazaarvoice.auth.hmac.common.TimeUtils.nowInUTC;

/**
 * AbstractAuthenticator is an abstract implementation of {@link Authenticator} that validates a set of
 * request credentials and returns the principal that the credentials identify. This class provides common
 * validation features, such as ensuring that the request has a valid timestamp and signature.
 *
 * @param <Principal> the type of principal the authenticator returns
 */
public abstract class AbstractAuthenticator<Principal> implements Authenticator<Principal> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractAuthenticator.class);

    private final long allowedTimestampRange;           // in milliseconds

    /**
     * Constructs an instance using a default timestamp range of 15 minutes. This is the length of time
     * for which the timestamp on a request can differ from the time on the server when the server receives
     * the request. If the difference exceeds this range, then the request will be denied.
     */
    protected AbstractAuthenticator() {
        this(15, TimeUnit.MINUTES);
    }

    /**
     * Constructs an instance using the specified timestamp range. This is the length of time for which
     * the timestamp on a request can differ from the time on the server when the server receives the
     * request. If the difference exceeds this range, then the request will be denied.
     *
     * @param allowedTimestampSlop the length of time for which the timestamp on a request can differ
     *                             from the server time when the request is received
     * @param timeUnit the unit {@code allowedTimestampSlop} is expressed in
     */
    protected AbstractAuthenticator(long allowedTimestampSlop, TimeUnit timeUnit) {
        this.allowedTimestampRange = timeUnit.toMillis(allowedTimestampSlop);
    }

    @Override
    public Principal authenticate(Credentials credentials) {
        // Make sure the timestamp has not expired - this is to protect against replay attacks
        if (!validateTimestamp(credentials.getTimestamp())) {
            LOG.info("Invalid timestamp");
            return null;
        }

        // Get the principal identified by the credentials
        Principal principal = getPrincipal(credentials);
        if (principal == null) {
            LOG.info("Could not get principal");
            return null;
        }

        // Get the secret key and use it to validate the request signature
        String secretKey = getSecretKeyFromPrincipal(principal);
        if (!validateSignature(credentials, secretKey)) {
            LOG.info("Invalid signature");
            return null;
        }

        return principal;
    }

    /**
     * Retrieve the principal object identified by the request credentials.
     *
     * @param credentials the credentials specified on the request
     * @return the principal object
     */
    protected abstract Principal getPrincipal(Credentials credentials);

    /**
     * Retrieve the secret key for the given principal.
     *
     * @param principal the principal for which to retrieve the secret key
     * @return the secret key
     */
    protected abstract String getSecretKeyFromPrincipal(Principal principal);

    /**
     * To protect against replay attacks, make sure the timestamp on the request is valid
     * by ensuring that the difference between the request time and the current time on the
     * server does not fall outside the acceptable time range. Note that the request time
     * may have been generated on a different machine and so it may be ahead or behind the
     * current server time.
     *
     * @param timestamp the timestamp specified on the request (in standard ISO8601 format)
     * @return true if the timestamp is valid
     */
    private boolean validateTimestamp(String timestamp) {
        DateTime requestTime = TimeUtils.parse(timestamp);
        long difference = Math.abs(new Duration(requestTime, nowInUTC()).getMillis());
        return difference <= allowedTimestampRange;
    }

    /**
     * Validate the signature on the request by generating a new signature here and making sure
     * they match. The only way for them to match is if both signature are generated using the
     * same secret key. If they match, this means that the requester has a valid secret key and
     * can be a trusted source.
     *
     * @param credentials the credentials specified on the request
     * @param secretKey the secret key that will be used to generate the signature
     * @return true if the signature is valid
     */
    private boolean validateSignature(Credentials credentials, String secretKey) {
        String clientSignature = credentials.getSignature();
        String serverSignature = createSignature(credentials, secretKey);
        return MessageDigest.isEqual(clientSignature.getBytes(), serverSignature.getBytes());
    }

    /**
     * Create a signature given the set of request credentials and a secret key.
     *
     * @param credentials the credentials specified on the request
     * @param secretKey the secret key that will be used to generate the signature
     * @return the signature
     */
    private String createSignature(Credentials credentials, String secretKey) {
        return new SignatureGenerator().generate(
                secretKey,
                credentials.getMethod(),
                credentials.getTimestamp(),
                credentials.getPath(),
                credentials.getContent());
    }

    @Override
    public String getApiKeyName() {
        return DEFAULT_API_KEY_PARAM;
    }
}