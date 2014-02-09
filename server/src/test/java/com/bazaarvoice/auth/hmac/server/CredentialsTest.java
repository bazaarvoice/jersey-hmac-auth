package com.bazaarvoice.auth.hmac.server;

import com.bazaarvoice.auth.hmac.common.Version;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CredentialsTest {
    private final Credentials credentials = createCredentials();

    @Test
    public void hasApiKey() {
        assertEquals("apiKey", credentials.getApiKey());
    }

    @Test
    public void hasSignature() {
        assertEquals("signature", credentials.getSignature());
    }

    @Test
    public void hasPath() {
        assertEquals("/path/to/something?param=pizza", credentials.getPath());
    }

    @Test
    public void hasTimestamp() {
        assertEquals("timestamp", credentials.getTimestamp());
    }

    @Test
    public void hasContent() {
        assertEquals("content", credentials.getContent());
    }

    @Test
    public void hasMethod() {
        assertEquals("GET", credentials.getMethod());
    }

    @Test
    public void hasVersion() {
        assertEquals(Version.V1, credentials.getVersion());
    }

    @Test
    @SuppressWarnings({"ObjectEqualsNull", "EqualsBetweenInconvertibleTypes"})
    public void hasWorkingEqualsMethod() {
        assertTrue(credentials.equals(credentials));
        assertTrue(credentials.equals(createCredentials()));
        assertFalse(credentials.equals(null));
        assertFalse(credentials.equals("string"));
        assertFalse(credentials.equals(Credentials.builder().build()));
    }

    @Test
    public void hasWorkingHashCode() {
        assertTrue(credentials.hashCode() == createCredentials().hashCode());
        assertFalse(credentials.hashCode() == Credentials.builder().build().hashCode());
    }

    private Credentials createCredentials() {
        return Credentials.builder()
                .withApiKey("apiKey")
                .withSignature("signature")
                .withPath("/path/to/something?param=pizza")
                .withTimestamp("timestamp")
                .withContent("content")
                .withMethod("GET")
                .withVersion(Version.V1)
                .build();
    }
}
