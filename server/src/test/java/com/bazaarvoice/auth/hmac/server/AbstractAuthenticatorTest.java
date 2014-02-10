package com.bazaarvoice.auth.hmac.server;

import com.bazaarvoice.auth.hmac.common.SignatureGenerator;
import com.bazaarvoice.auth.hmac.common.Version;
import com.bazaarvoice.auth.hmac.server.exception.AuthenticationException;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Test;

import static com.bazaarvoice.auth.hmac.common.TimeUtils.nowInUTC;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.joda.time.Minutes.minutes;
import static org.junit.Assert.assertEquals;

public class AbstractAuthenticatorTest {
    private static final String API_KEY = "api-key";
    private static final String SECRET_KEY = "secret-key";
    private static final String PRINCIPAL = "principal";
    private static final int ACCEPTABLE_TIMESTAMP_RANGE = minutes(5).getMinutes();

    private final AbstractAuthenticator<String> authenticator = createAuthenticator(ACCEPTABLE_TIMESTAMP_RANGE);

    @Test
    public void respondsToValidCredentialsWithPrincipal() throws AuthenticationException {
        Credentials credentials = createCredentials();
        String principal = authenticator.authenticate(credentials);
        assertNotNull(principal);
        assertEquals(PRINCIPAL, principal);
    }

    @Test
    public void respondsToExpiredPastTimestampWithNull() throws AuthenticationException {
        DateTime requestTime = nowInUTC().minusMinutes(ACCEPTABLE_TIMESTAMP_RANGE + 2);
        Credentials credentials = createCredentialsWithRequestTime(requestTime);
        String principal = authenticator.authenticate(credentials);
        assertNull(principal);
    }

    @Test
    public void respondsToExpiredFutureTimestampWithNull() throws AuthenticationException {
        DateTime requestTime = nowInUTC().plusMinutes(ACCEPTABLE_TIMESTAMP_RANGE + 2);
        Credentials credentials = createCredentialsWithRequestTime(requestTime);
        String principal = authenticator.authenticate(credentials);
        assertNull(principal);
    }

    @Test
    public void respondsToInvalidSignatureWithNull() throws AuthenticationException {
        Credentials credentials = createCredentialsWithInvalidSecretKey();
        String principal = authenticator.authenticate(credentials);
        assertNull(principal);
    }

    private Credentials createCredentials() {
        return createCredentials(nowInUTC(), SECRET_KEY);
    }

    private Credentials createCredentialsWithRequestTime(DateTime requestTime) {
        return createCredentials(requestTime, SECRET_KEY);
    }

    private Credentials createCredentialsWithInvalidSecretKey() {
        return createCredentials(nowInUTC(), SECRET_KEY + "-invalid");
    }

    private Credentials createCredentials(DateTime requestTime, String secretKey) {
        String method = "GET";
        String timestamp = ISODateTimeFormat.dateTime().print(requestTime);
        String path = "/example?apiKey=foo";
        String content = "some request content";
        String signature = new SignatureGenerator().generate(secretKey, method, timestamp, path, content);

        return Credentials.builder()
                .withVersion(Version.V1)
                .withApiKey(API_KEY)
                .withTimestamp(timestamp)
                .withMethod(method)
                .withPath(path)
                .withContent(content)
                .withSignature(signature)
                .build();
    }

    private AbstractAuthenticator<String> createAuthenticator(int acceptableTimestampRange) {
        return new AbstractAuthenticator<String>(acceptableTimestampRange) {
            @Override
            protected String getPrincipal(Credentials credentials) {
                return PRINCIPAL;
            }

            @Override
            protected String getSecretKeyFromPrincipal(String s) {
                return SECRET_KEY;
            }
        };
    }
}
