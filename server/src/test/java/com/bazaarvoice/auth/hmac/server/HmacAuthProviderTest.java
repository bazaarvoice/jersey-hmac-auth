package com.bazaarvoice.auth.hmac.server;

import com.bazaarvoice.auth.hmac.common.RequestConstants;
import com.bazaarvoice.auth.hmac.common.Version;
import com.bazaarvoice.auth.hmac.server.exception.AuthenticationException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.core.ScanningResourceConfig;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.LowLevelAppDescriptor;
import org.junit.Test;

import javax.ws.rs.GET;
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
        public String testOptionalAuth(@HmacAuth(required = false) String principal) {
            return principal;
        }
    }

    @Override
    protected AppDescriptor configure() {
        final Authenticator<String> authenticator = new Authenticator<String>() {
            @Override
            public String authenticate(Credentials credentials) throws AuthenticationException {
                if (GOOD_API_KEY.equals(credentials.getApiKey())) {
                    return GOOD_API_KEY;
                }
                if (INTERNAL_ERROR.equals(credentials.getApiKey())) {
                    throw new AuthenticationException("An internal error occurred");
                }
                return null;
            }
        };

        ResourceConfig config = new ScanningResourceConfig();
        config.getSingletons().add(new HmacAuthProvider<>(authenticator));
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
    public void respondsToInvalidCredentialsWithNullWhenAuthIsOptional() {
        // This test ensures that the provider injects a null principal object into the resource
        // method by asserting that the resource method returns a 204 status code
        try {
            queryWithInvalidCredentials(false);
            fail("UniformInterfaceException exception was expected");

        } catch (UniformInterfaceException e) {
            assertEquals(ClientResponse.Status.NO_CONTENT, e.getResponse().getClientResponseStatus());
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
    public void respondsToMissingCredentialsWithNullWhenAuthIsOptional() {
        // This test ensures that the provider injects a null principal object into the resource
        // method by asserting that the resource method returns a 204 status code
        try {
            queryWithoutCredentials(false);
            fail("UniformInterfaceException exception was expected");

        } catch (UniformInterfaceException e) {
            assertEquals(ClientResponse.Status.NO_CONTENT, e.getResponse().getClientResponseStatus());
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
        return execute(GOOD_API_KEY, authRequired);
    }

    private String queryWithInvalidCredentials(boolean authRequired) {
        return execute(BAD_API_KEY, authRequired);
    }

    private String queryWithoutCredentials(boolean authRequired) {
        return executeWithoutCredentials(authRequired);
    }

    private String queryAndCauseInternalError(boolean authRequired) {
        return execute(INTERNAL_ERROR, authRequired);
    }

    private String execute(String apiKey, boolean authRequired) {
        return client().resource(chooseEndpoint(authRequired))
                .queryParam(RequestConstants.API_KEY_QUERY_PARAM, apiKey)
                .header(RequestConstants.SIGNATURE_HTTP_HEADER, "signature")
                .header(RequestConstants.TIMESTAMP_HTTP_HEADER, "timestamp")
                .header(RequestConstants.VERSION_HTTP_HEADER, Version.V1)
                .entity("Some content in the request body")
                .get(String.class);
    }

    private String executeWithoutCredentials(boolean authRequired) {
        return client().resource(chooseEndpoint(authRequired)).get(String.class);
    }

    private String chooseEndpoint(boolean authRequired) {
        return authRequired ? "/auth/required" : "/auth/optional";
    }
}
