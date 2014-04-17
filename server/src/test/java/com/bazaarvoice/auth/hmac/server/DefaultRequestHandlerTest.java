package com.bazaarvoice.auth.hmac.server;

import com.bazaarvoice.auth.hmac.common.Credentials;
import com.bazaarvoice.auth.hmac.server.exception.InternalServerException;
import com.bazaarvoice.auth.hmac.server.exception.NotAuthorizedException;
import com.sun.jersey.api.core.HttpRequestContext;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultRequestHandlerTest {
    private static final HttpRequestContext request = mock(HttpRequestContext.class);
    private static final Credentials credentials = mock(Credentials.class);
    private static final String principal = "TEST_KEY";
    private static final HmacAuth annotation = new HmacAuth() {
        @Override
        public Class<? extends java.lang.annotation.Annotation> annotationType() {
            return HmacAuth.class;
        }
    };

    private static RequestDecoder decoder;
    private static Authenticator<String> authenticator;
    private static Authorizer<HmacAuth, String> authorizer;
    private static RequestHandler<HmacAuth, String> handler;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() {
        decoder = mock(RequestDecoder.class);
        authenticator = mock(Authenticator.class);
        authorizer = mock(Authorizer.class);
        handler = new DefaultRequestHandler<>(decoder, authenticator, authorizer);
    }

    @Test(expected = NotAuthorizedException.class)
    public void testHandleWithoutCredentials() {
        when(decoder.decode(any(HttpRequestContext.class))).thenThrow(new IllegalArgumentException());
        handler.handle(annotation, request);
        fail();
    }

    @Test
    public void testHandleWithValidCredentials() {
        HttpRequestContext request = mock(HttpRequestContext.class);
        when(decoder.decode(any(HttpRequestContext.class))).thenReturn(credentials);
        when(authenticator.authenticate(any(Credentials.class))).thenReturn(principal);

        String value = handler.handle(annotation, request);
        assertNotNull(value);
        assertEquals(principal, value);
    }

    @Test(expected = NotAuthorizedException.class)
    public void testHandleWithInvalidCredentials() {
        HttpRequestContext request = mock(HttpRequestContext.class);
        when(decoder.decode(any(HttpRequestContext.class))).thenReturn(credentials);
        when(authenticator.authenticate(any(Credentials.class))).thenReturn(null);

        handler.handle(annotation, request);
        fail();
    }

    @Test(expected = InternalServerException.class)
    public void testHandleWithInternalError() {
        HttpRequestContext request = mock(HttpRequestContext.class);
        when(decoder.decode(any(HttpRequestContext.class))).thenReturn(credentials);
        when(authenticator.authenticate(any(Credentials.class))).thenThrow(new NullPointerException());

        handler.handle(annotation, request);
        fail();
    }
}
