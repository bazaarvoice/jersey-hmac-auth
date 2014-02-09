package com.bazaarvoice.auth.hmac.client;

import com.bazaarvoice.auth.hmac.common.RequestConstants;
import com.bazaarvoice.auth.hmac.common.Version;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import org.junit.Test;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.Server;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

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

    @Test
    public void addsCredentialsToRequest() throws Exception {
        Connection connection = null;
        try {
            // Create an in-memory HTTP server that validates that all security credentials are on the request
            ValidatingHttpServer server = new ValidatingHttpServer() {
                @Override
                protected void validate(Request request) throws Exception {
                    validateApiKey(request);
                    validateVersion(request);
                    validateTimestamp(request);
                    validateSignature(request);
                }

                private void validateApiKey(Request request) throws Exception {
                    String apiKey = request.getQuery().get(RequestConstants.API_KEY_QUERY_PARAM);
                    if (!apiKey.equals("myApiKey")) {
                        throw new Exception("Invalid apiKey: " + apiKey);
                    }
                }

                private void validateVersion(Request request) throws Exception {
                    String version = request.getValue(RequestConstants.VERSION_HTTP_HEADER);
                    if (Version.fromValue(version) != Version.V1) {
                        throw new Exception("Invalid version: " + version);
                    }
                }

                private void validateTimestamp(Request request) throws Exception {
                    String timestamp = request.getValue(RequestConstants.TIMESTAMP_HTTP_HEADER);
                    if (isNullOrEmpty(timestamp)) {
                        throw new Exception("Invalid timestamp");
                    }
                }

                private void validateSignature(Request request) throws Exception {
                    String signature = request.getValue(RequestConstants.SIGNATURE_HTTP_HEADER);
                    if (isNullOrEmpty(signature)) {
                        throw new Exception("Invalid signature");
                    }
                }
            };

            // Launch the server
            connection = server.connect();

            // Send a request to the server. If validation does not succeed, the Jersey client
            // will throw an exception and the overall test will fail.
            Client client = new Client();
            client.addFilter(new HmacClientFilter("myApiKey", "mySecretKey"));
            client.resource(server.getUrl()).get(String.class);

        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    /**
     * This abstract class represents an in-memory HTTP server that can receive requests and validate
     * them in a particular way. Classes that extend this must provide the validation logic.
     */
    private static abstract class ValidatingHttpServer implements Container {
        private static final int PORT = 9999;

        @Override
        public void handle(Request request, Response response) {
            PrintStream body = null;
            try {
                body = response.getPrintStream();

                validate(request);
                response.setCode(ClientResponse.Status.OK.getStatusCode());

            } catch (Exception e) {
                e.printStackTrace();
                response.setCode(ClientResponse.Status.INTERNAL_SERVER_ERROR.getStatusCode());

            } finally {
                // The response body has to be explicitly closed in order for this method to return a response
                if (body != null) {
                    body.close();
                }
            }
        }

        protected abstract void validate(Request request) throws Exception;

        public Connection connect() throws Exception {
            Server server = new ContainerServer(this);
            Connection connection = new SocketConnection(server);
            SocketAddress address = new InetSocketAddress(PORT);
            connection.connect(address);
            return connection;
        }

        public String getUrl() {
            return String.format("http://localhost:%d", PORT);
        }
    }
}
