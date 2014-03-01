package com.bazaarvoice.auth.hmac.client;

import com.bazaarvoice.auth.hmac.common.Credentials;
import com.bazaarvoice.auth.hmac.common.RequestConstants;
import com.bazaarvoice.auth.hmac.common.Version;
import com.google.common.base.Throwables;
import com.google.common.io.ByteStreams;
import com.sun.jersey.api.client.ClientResponse;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.Server;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;

/**
 * This abstract class represents an in-memory HTTP server that validates inbound requests in some way.
 */
public abstract class ValidatingHttpServer implements Container {
    private int port;

    protected ValidatingHttpServer(int port) {
        this.port = port;
    }

    protected abstract void validate(Credentials credentials) throws Exception;

    @Override
    public void handle(Request request, Response response) {
        PrintStream body = null;
        try {
            body = response.getPrintStream();

            // Validate the request credentials
            Credentials credentials = Decoder.decode(request);
            validate(credentials);

            // And we're done
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

    public Connection connect() throws Exception {
        Server server = new ContainerServer(this);
        Connection connection = new SocketConnection(server);
        SocketAddress address = new InetSocketAddress(port);
        connection.connect(address);
        return connection;
    }

    public URI getUri() {
        return UriBuilder.fromUri("http://localhost").port(port).build();
    }

    private static class Decoder {
        private static Credentials decode(Request request) {
            return Credentials.builder()
                    .withApiKey(getApiKey(request))
                    .withSignature(getSignature(request))
                    .withPath(getPath(request))
                    .withTimestamp(getTimestamp(request))
                    .withContent(getContent(request))
                    .withMethod(getMethod(request))
                    .withVersion(Version.V1)
                    .build();
        }

        private static String getApiKey(Request request) {
            return request.getQuery().get(RequestConstants.API_KEY_QUERY_PARAM);
        }

        private static String getSignature(Request request) {
            return request.getValue(RequestConstants.SIGNATURE_HTTP_HEADER);
        }

        private static String getPath(Request request) {
            // Get the path and any query parameters (e.g. /api/v1/pizza?sort=toppings&apiKey=someKey)
            return request.getTarget();
        }

        private static String getTimestamp(Request request) {
            return request.getValue(RequestConstants.TIMESTAMP_HTTP_HEADER);
        }

        private static byte[] getContent(Request request) {
            try {
                return ByteStreams.toByteArray(request.getInputStream());

            } catch (IOException e) {
                throw Throwables.propagate(e);
            }
        }

        private static String getMethod(Request request) {
            return request.getMethod();
        }
    }
}