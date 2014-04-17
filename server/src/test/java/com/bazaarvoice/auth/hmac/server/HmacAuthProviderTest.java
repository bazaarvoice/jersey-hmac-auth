package com.bazaarvoice.auth.hmac.server;

import com.bazaarvoice.auth.hmac.common.Credentials;
import com.bazaarvoice.auth.hmac.common.RequestConstants;
import com.bazaarvoice.auth.hmac.common.Version;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.core.ScanningResourceConfig;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.LowLevelAppDescriptor;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class HmacAuthProviderTest extends JerseyTest {
    private static final String GOOD_API_KEY = "good-api-key";
    private static final String BAD_API_KEY = "bad-api-key";
    private static final String INTERNAL_ERROR = "internal-error";

    @Path("/auth")
    @Produces(MediaType.TEXT_PLAIN)
    public static class AuthResource {
        @GET
        @Path("/required")
        public String testRequiredAuth(@HmacAuth String principal) {
            return principal;
        }

        @GET
        @Path("/optional")
        public String testOptionalAuth(@HmacAuth String principal) {
            return principal;
        }

        @POST
        public String testWithContent(@HmacAuth String principal, String content) {
            return content;
        }
    }

    @Override
    protected AppDescriptor configure() {
        final Authenticator<String> authenticator = new Authenticator<String>() {
            @Override
            public String authenticate(Credentials credentials) {
                if (GOOD_API_KEY.equals(credentials.getApiKey())) {
                    return GOOD_API_KEY;
                }
                if (INTERNAL_ERROR.equals(credentials.getApiKey())) {
                    throw new IllegalStateException("An internal error occurred");
                }
                return null;
            }
        };

        ResourceConfig config = new ScanningResourceConfig();
        config.getSingletons().add(new HmacAuthProvider<HmacAuth, String>(new DefaultRequestHandler<HmacAuth, String>(authenticator)) {});
        config.getSingletons().add(new AuthResource());
        return new LowLevelAppDescriptor.Builder(config).build();
    }

    @Test
    public void transformsCredentialsToPrincipals() {
        String response = queryWithValidCredentials(true);
        assertEquals(GOOD_API_KEY, response);
    }

    @Test
    public void transformsCredentialsToPrincipalsWhenAuthIsOptional() {
        String response = queryWithValidCredentials(false);
        assertEquals(GOOD_API_KEY, response);
    }

    @Test
    public void respondsToInvalidCredentialsWith401() {
        try {
            queryWithInvalidCredentials(true);
            fail("UniformInterfaceException exception was expected");

        } catch (UniformInterfaceException e) {
            assertEquals(ClientResponse.Status.UNAUTHORIZED, e.getResponse().getClientResponseStatus());
        }
    }

    @Test
    public void respondsToMissingCredentialsWith401() {
        try {
            queryWithoutCredentials(true);
            fail("UniformInterfaceException exception was expected");

        } catch (UniformInterfaceException e) {
            assertEquals(ClientResponse.Status.UNAUTHORIZED, e.getResponse().getClientResponseStatus());
        }
    }

    @Test
    public void respondsToExceptionsWith500() throws Exception {
        try {
            queryAndCauseInternalError(true);
            fail("UniformInterfaceException exception was expected");

        } catch (UniformInterfaceException e) {
            assertEquals(ClientResponse.Status.INTERNAL_SERVER_ERROR, e.getResponse().getClientResponseStatus());
        }
    }

    private String queryWithValidCredentials(boolean authRequired) {
        return get(GOOD_API_KEY, authRequired);
    }

    private String queryWithInvalidCredentials(boolean authRequired) {
        return get(BAD_API_KEY, authRequired);
    }

    private String queryWithoutCredentials(boolean authRequired) {
        return getWithoutCredentials(authRequired);
    }

    private String queryAndCauseInternalError(boolean authRequired) {
        return get(INTERNAL_ERROR, authRequired);
    }

    private String get(String apiKey, boolean authRequired) {
        return client().resource(chooseEndpoint(authRequired))
                .queryParam(RequestConstants.API_KEY_QUERY_PARAM, apiKey)
                .header(RequestConstants.SIGNATURE_HTTP_HEADER, "signature")
                .header(RequestConstants.TIMESTAMP_HTTP_HEADER, "timestamp")
                .header(RequestConstants.VERSION_HTTP_HEADER, Version.V1)
                .get(String.class);
    }

    private String getWithoutCredentials(boolean authRequired) {
        return client().resource(chooseEndpoint(authRequired)).get(String.class);
    }

    private String chooseEndpoint(boolean authRequired) {
        return authRequired ? "/auth/required" : "/auth/optional";
    }

    @Test
    public void preservesRequestContent() {
        preserves("Some content in the request body");
    }

    @Test
    public void preservesRequestContentWhenEmpty() {
        preserves("");
    }

    private void preserves(String content) {
        // If not careful, the auth provider will destroy the request content when it reads it.
        // Make sure that the request content is preserved so that it successfully reaches the
        // resource method.
        String response = post(GOOD_API_KEY, content);
        assertEquals(content, response);
    }

    private String post(String apiKey, String content) {
        return client().resource("/auth")
                .queryParam(RequestConstants.API_KEY_QUERY_PARAM, apiKey)
                .header(RequestConstants.SIGNATURE_HTTP_HEADER, "signature")
                .header(RequestConstants.TIMESTAMP_HTTP_HEADER, "timestamp")
                .header(RequestConstants.VERSION_HTTP_HEADER, Version.V1)
                .entity(content)
                .post(String.class);
    }
}
