package com.bazaarvoice.auth.hmac.server;

import com.bazaarvoice.auth.hmac.common.SignatureGenerator;
import com.bazaarvoice.auth.hmac.common.TimeUtils;
import com.bazaarvoice.auth.hmac.server.exception.AuthenticationException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.bazaarvoice.auth.hmac.common.TimeUtils.nowInUTC;
import static org.joda.time.Minutes.minutesBetween;

/**
 * An abstract authenticator class that validates user-supplied credentials and returns a
 * principal object based on the credentials. This class provides common authentication features
 * such as timestamp validation (to protect against replay attacks) and signature validation.
 *
 * @param <Principal> the type of principal the authenticator returns
 */
public abstract class AbstractAuthenticator<Principal> implements Authenticator {
    private static final Logger LOG = LoggerFactory.getLogger(HmacAuthProvider.class);

    private final int acceptableTimestampRange;

    /**
     * Set the acceptable timestamp range in minutes. This is used to validate the timestamp
     * on the request by making sure that the difference between the request time and the
     * current server time does not fall outside of the acceptable range.
     *
     * @param acceptableTimestampRange the acceptable time range in minutes
     */
    protected AbstractAuthenticator(int acceptableTimestampRange) {
        this.acceptableTimestampRange = acceptableTimestampRange;
    }

    @Override
    public Principal authenticate(Credentials credentials) throws AuthenticationException {
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
     * may have been generated on a different machine and so it may be slightly ahead or behind
     * the current server time.
     *
     * @param timestamp the timestamp specified on the request (in standard ISO8601 format)
     * @return true if the timestamp is valid
     */
    private boolean validateTimestamp(String timestamp) {
        DateTime requestTime = TimeUtils.parse(timestamp);
        int difference = Math.abs(minutesBetween(requestTime, nowInUTC()).getMinutes());
        return difference <= acceptableTimestampRange;
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
        return clientSignature.equals(serverSignature);
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
}