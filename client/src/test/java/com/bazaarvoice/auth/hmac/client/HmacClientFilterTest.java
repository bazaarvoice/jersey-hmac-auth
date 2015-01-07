package com.bazaarvoice.auth.hmac.client;

import com.bazaarvoice.auth.hmac.common.Credentials;
import com.bazaarvoice.auth.hmac.common.RequestConfiguration;
import com.bazaarvoice.auth.hmac.common.Version;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.GZIPContentEncodingFilter;
import com.sun.jersey.api.json.JSONConfiguration;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.simpleframework.transport.connect.Connection;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Arrays;
import java.util.Random;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * This class tests the client-side Jersey filter to ensure that it encodes requests properly.
 *
 * Note that these tests are implemented by sending requests to an in-memory HTTP server that
 * validates the requests when it receives them. While this is admittedly more like an integration
 * test than a unit test, it is done this way because Jersey client filters are not easily unit
 * tested. (The ClientFilter class contains <code>final</code> methods that cannot be mocked.)
 */
public class HmacClientFilterTest {
    private final static String apiKey = "someApiKey";
    private final static String secretKey = "someSecretKey";
    private static int port = 1111;

    @Before
    public void incrementPort() {
        // This allows each test method that starts/stops an in-memory HTTP server to use its own port,
        // which is useful because it takes some time after closing the connection for the port to become
        // free. An alternative is to sleep for a few seconds between tests, but that would make the
        // tests take longer to complete.
        ++port;
    }

    @Test
    public void sendsCredentials() throws Exception {
        final byte[] content = "something".getBytes();

        Connection connection = null;
        try {
            RequestConfiguration requestConfiguration = RequestConfiguration.builder()
                    .withApiKeyQueryParamName("passkey")
                    .withSignatureHttpHeader("duck-duck-signature-header")
                    .withTimestampHttpHeader("duck-duck-timestamp-header")
                    .withVersionHttpHeader("duck-duck-version-header")
                    .build();

            ValidatingHttpServer server = new ValidatingHttpServer(port, requestConfiguration) {
                @Override
                protected void validate(Credentials credentials) throws Exception {
                    validateApiKey(credentials);
                    validateVersion(credentials);
                    validateTimestamp(credentials);
                    validateContent(credentials);
                    validateSignature(credentials);
                }

                private void validateApiKey(Credentials credentials) throws Exception {
                    if (!credentials.getApiKey().equals(apiKey)) {
                        throw new Exception("Invalid apiKey: " + credentials.getApiKey());
                    }
                }

                private void validateVersion(Credentials credentials) throws Exception {
                    if (credentials.getVersion() != Version.V1) {
                        throw new Exception("Invalid version: " + credentials.getVersion());
                    }
                }

                private void validateTimestamp(Credentials credentials) throws Exception {
                    if (isNullOrEmpty(credentials.getTimestamp())) {
                        throw new Exception("Invalid timestamp");
                    }
                }

                private void validateContent(Credentials credentials) throws Exception {
                    if (!Arrays.equals(credentials.getContent(), content)) {
                        throw new Exception("Invalid content");
                    }
                }

                private void validateSignature(Credentials credentials) throws Exception {
                    if (isNullOrEmpty(credentials.getSignature())) {
                        throw new Exception("Invalid signature");
                    }
                }
            };

            // Launch the server
            connection = server.connect();

            // Send a request to the server. If validation does not succeed, an exception will be thrown.
            Client client = Client.create();
            client.addFilter(new HmacClientFilter(apiKey, secretKey, client.getMessageBodyWorkers(), requestConfiguration));
            client.resource(server.getUri()).put(content);

        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    @Test
    @Ignore
    public void validateSignatureWhenThereIsNoContent() throws Exception {
        Connection connection = null;
        try {
            // Start the server
            ValidatingHttpServer server = new SignatureValidatingHttpServer(port, secretKey);
            connection = server.connect();

            // Create a client with the filter that is under test
            Client client = createClient();
            client.addFilter(new HmacClientFilter(apiKey, secretKey, client.getMessageBodyWorkers()));

            // Send a request with no content in the request body
            client.resource(server.getUri()).get(String.class);

        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    @Test
    @Ignore
    public void validateSignatureWhenContentIsPojo() throws Exception {
        Connection connection = null;
        try {
            // Start the server
            RequestConfiguration requestConfiguration =
                RequestConfiguration.builder().withApiKeyQueryParamName("passkey")
                        .withSignatureHttpHeader("duck-duck-signature-header")
                        .withTimestampHttpHeader("duck-duck-timestamp-header")
                        .withVersionHttpHeader("duck-duck-version-header")
                        .build();
            ValidatingHttpServer server = new SignatureValidatingHttpServer(port, secretKey, requestConfiguration);
            connection = server.connect();

            // Create a client with the filter that is under test
            Client client = createClient();
            client.addFilter(new HmacClientFilter(apiKey, secretKey, client.getMessageBodyWorkers(), requestConfiguration));
            client.addFilter(new GZIPContentEncodingFilter(true));

            // Send a pizza in the request body
            Pizza pizza = new Pizza();
            pizza.setTopping("olive");
            client.resource(server.getUri())
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .put(pizza);

        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    @Test
    @Ignore
    public void validateSignatureWhenContentIsBinary() throws Exception {
        Connection connection = null;
        try {
            // Start the server
            RequestConfiguration requestConfiguration =
                    RequestConfiguration.builder()
                            .withApiKeyQueryParamName("passkey")
                            .withSignatureHttpHeader("duck-duck-signature-header")
                            .withTimestampHttpHeader("duck-duck-timestamp-header")
                            .withVersionHttpHeader("duck-duck-version-header")
                            .build();

            ValidatingHttpServer server = new SignatureValidatingHttpServer(port, secretKey, requestConfiguration);
            connection = server.connect();

            // Create a client with the filter that is under test
            Client client = createClient();
            client.addFilter(new HmacClientFilter(apiKey, secretKey, client.getMessageBodyWorkers(),requestConfiguration));

            // Send some random binary data in the request body
            byte[] binaryData = new byte[2];
            new Random().nextBytes(binaryData);
            client.resource(server.getUri())
                    .type(MediaType.APPLICATION_OCTET_STREAM_TYPE)
                    .put(binaryData);

        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    @Test
    public void validateSignatureWhenPathHasQueryParams() throws Exception {
        Connection connection = null;
        try {
            // Start the server
            RequestConfiguration requestConfiguration =
                    RequestConfiguration.builder()
                            .withApiKeyQueryParamName("passkey")
                            .withSignatureHttpHeader("duck-duck-signature-header")
                            .withTimestampHttpHeader("duck-duck-timestamp-header")
                            .withVersionHttpHeader("duck-duck-version-header")
                            .build();
            ValidatingHttpServer server = new SignatureValidatingHttpServer(port, secretKey, requestConfiguration);
            connection = server.connect();

            // Create a client with the filter that is under test
            Client client = createClient();
            client.addFilter(new HmacClientFilter(apiKey, secretKey, client.getMessageBodyWorkers(), requestConfiguration));

            // Send the request to a path other than "/" and that also includes an additional query parameter
            URI uri = UriBuilder.fromUri(server.getUri())
                    .segment("api", "v1", "pizza")
                    .queryParam("sort", "toppings")
                    .build();
            client.resource(uri).get(String.class);

        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    private Client createClient() {
        ClientConfig config = new DefaultClientConfig();
        config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        return Client.create(config);
    }

    private static class Pizza {
        private String topping;

        public String getTopping() {
            return topping;
        }

        public void setTopping(String topping) {
            this.topping = topping;
        }
    }
}
