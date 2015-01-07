package com.bazaarvoice.auth.hmac.server;

import com.sun.jersey.api.core.HttpRequestContext;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

public class PassThroughRequestHandlerTest {
    private static final HttpRequestContext request = mock(HttpRequestContext.class);

    @Test
    public void testPassThroughHandlerWithValue() {
        String expected = "STRING";
        PassThroughRequestHandler<HmacAuth, String> handler = new PassThroughRequestHandler<HmacAuth, String>(expected);
        String value = handler.handle(null, request);
        assertNotNull(value);
        assertEquals(expected, value);
    }
}
