package com.bazaarvoice.auth.hmac.server;

import com.bazaarvoice.auth.hmac.common.Credentials;
import com.bazaarvoice.auth.hmac.common.RequestConfiguration;
import com.google.common.io.ByteStreams;
import org.glassfish.jersey.internal.PropertiesDelegate;
import org.glassfish.jersey.internal.util.collection.ImmutableMultivaluedMap;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ExtendedUriInfo;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import javax.inject.Provider;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.SecurityContext;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Test class for {@link PrincipalFactory}.
 */
public class PrincipalFactoryTest {

    @Mock
    private Authenticator<String> authenticator;
    @Mock
    private Provider<ContainerRequest> requestProvider;
    @Mock
    private ContainerRequest request;
    private PrincipalFactory<String> factory;

    @Before
    public void setUp() throws Exception {
        initMocks(this);

        given(requestProvider.get()).willReturn(request);
        given(authenticator.getApiKeyName()).willReturn(Authenticator.DEFAULT_API_KEY_PARAM);
        factory = new PrincipalFactory<String>(authenticator, requestProvider, new RequestConfiguration());
    }

    @Test
    public final void verifyProvideRequiresApiKey() {
        // given
        final ExtendedUriInfo uriInfo = mock(ExtendedUriInfo.class);
        final MultivaluedMap<String, String> parameterMap = ImmutableMultivaluedMap.empty();
        given(uriInfo.getQueryParameters()).willReturn(parameterMap);
        given(request.getUriInfo()).willReturn(uriInfo);

        // when
        try {
            factory.provide();

            // then
            fail("Expected 400 status code");
        } catch (final BadRequestException bre) {
        }
    }

    @Test
    public final void verifyProvideDeniesAccess() throws URISyntaxException {
        // given
        final MultivaluedMap<String, String> parameterMap = new MultivaluedHashMap<String, String>();
        parameterMap.putSingle("apiKey", "invalidApiKey");

        final URI uri = new URI("https://api.example.com/path/to/resource?apiKey=invalidApiKey");
        final ExtendedUriInfo uriInfo = mock(ExtendedUriInfo.class);
        given(uriInfo.getQueryParameters()).willReturn(parameterMap);
        given(uriInfo.getRequestUri()).willReturn(uri);

        given(request.getUriInfo()).willReturn(uriInfo);
        given(request.getHeaderString("X-Auth-Version")).willReturn("1");
        given(request.getHeaderString("X-Auth-Signature")).willReturn("invalidSignature");
        given(request.getHeaderString("X-Auth-Timestamp")).willReturn("two days ago");
        given(request.getMethod()).willReturn("POST");

        given(authenticator.authenticate(any(Credentials.class))).willReturn(null);

        // when
        try {
            factory.provide();

            // then
            fail("Expected 401 status code");
        } catch (final NotAuthorizedException nae) {
        }
    }

    @Test
    public final void verifyProvideGrantsAccess() throws URISyntaxException {
        // given
        final MultivaluedMap<String, String> parameterMap = new MultivaluedHashMap<String, String>();
        parameterMap.putSingle("apiKey", "validApiKey");

        final URI uri = new URI("https://api.example.com/path/to/resource?apiKey=validApiKey");
        final ExtendedUriInfo uriInfo = mock(ExtendedUriInfo.class);
        given(uriInfo.getQueryParameters()).willReturn(parameterMap);
        given(uriInfo.getRequestUri()).willReturn(uri);

        given(request.getUriInfo()).willReturn(uriInfo);
        given(request.getHeaderString("X-Auth-Version")).willReturn("1");
        given(request.getHeaderString("X-Auth-Signature")).willReturn("validSignature");
        given(request.getHeaderString("X-Auth-Timestamp")).willReturn("two seconds ago");
        given(request.getMethod()).willReturn("GET");

        given(authenticator.authenticate(any(Credentials.class))).willReturn("principal");

        // when
        final String result = factory.provide();

        // then
        assertEquals("principal", result);
    }

    @Test
    public final void verifyProvideAppliesContentToSignature() throws URISyntaxException, UnsupportedEncodingException {
        // given
        final MultivaluedMap<String, String> parameterMap = new MultivaluedHashMap<String, String>();
        parameterMap.putSingle("apiKey", "validApiKey");

        final URI uri = new URI("https://api.example.com/path/to/resource?apiKey=validApiKey");
        final ExtendedUriInfo uriInfo = mock(ExtendedUriInfo.class);
        given(uriInfo.getQueryParameters()).willReturn(parameterMap);
        given(uriInfo.getRequestUri()).willReturn(uri);

        given(request.getUriInfo()).willReturn(uriInfo);
        given(request.getHeaderString("X-Auth-Version")).willReturn("1");
        given(request.getHeaderString("X-Auth-Signature")).willReturn("validSignature");
        given(request.getHeaderString("X-Auth-Timestamp")).willReturn("two seconds ago");
        given(request.getMethod()).willReturn("PUT");
        given(request.hasEntity()).willReturn(true);
        given(request.getEntityStream()).willReturn(new ByteArrayInputStream("content".getBytes("UTF-8")));

        given(authenticator.authenticate(any(Credentials.class))).willReturn("principal");

        // when
        final String result = factory.provide();

        // then
        assertEquals("principal", result);
        final ArgumentCaptor<Credentials> credentialsCaptor = ArgumentCaptor.forClass(Credentials.class);
        verify(authenticator).authenticate(credentialsCaptor.capture());
        final Credentials credentials = credentialsCaptor.getValue();
        assertEquals("validApiKey", credentials.getApiKey());
        assertEquals("two seconds ago", credentials.getTimestamp());
        assertEquals("PUT", credentials.getMethod());
        assertEquals("/path/to/resource?apiKey=validApiKey", credentials.getPath());
        assertEquals("content", new String(credentials.getContent(), "UTF-8"));
    }

    @Test
    public final void verifyProvidePassesContentDownstream() throws URISyntaxException, IOException {
        // given
        final String content = "content";

        final SecurityContext securityContext = mock(SecurityContext.class);
        final PropertiesDelegate propertiesDelegate = mock(PropertiesDelegate.class);
        final MultivaluedMap<String, String> parameterMap = new MultivaluedHashMap<String, String>();
        parameterMap.putSingle("apiKey", "validApiKey");

        final URI uri = new URI("https://api.example.com/path/to/resource?apiKey=validApiKey");
        final ExtendedUriInfo uriInfo = mock(ExtendedUriInfo.class);
        given(uriInfo.getQueryParameters()).willReturn(parameterMap);
        given(uriInfo.getRequestUri()).willReturn(uri);

        final ContainerRequest containerRequest = new ContainerRequest(new URI("https://api.example.com"),
                new URI("https://api.example.com/path/to/resource?apiKey=validApiKey"), "POST", securityContext,
                propertiesDelegate);
        request = spy(containerRequest);
        request.setEntityStream(new ByteArrayInputStream(content.getBytes("UTF-8")));
        request.header("X-Auth-Version", "1");
        request.header("X-Auth-Signature", "validSignature");
        request.header("X-Auth-Timestamp", "two seconds ago");
        given(request.getMethod()).willReturn("POST");
        given(request.getUriInfo()).willReturn(uriInfo);

        given(requestProvider.get()).willReturn(request);

        given(authenticator.authenticate(any(Credentials.class))).willReturn("principal");

        // when
        final String result = factory.provide();

        // then
        assertEquals("principal", result);
        assertTrue(request.hasEntity());
        final InputStream entityStream = request.getEntityStream();
        try {
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ByteStreams.copy(entityStream, outputStream);

            assertEquals(content, new String(outputStream.toByteArray(), "UTF-8"));
        } finally {
            entityStream.close();
        }
    }

}