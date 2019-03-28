package com.bazaarvoice.auth.hmac.client;

import com.bazaarvoice.auth.hmac.common.RequestConfiguration;
import com.bazaarvoice.auth.hmac.common.SignatureGenerator;
import com.bazaarvoice.auth.hmac.common.TimeUtils;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.RequestWriter;
import com.sun.jersey.spi.MessageBodyWorkers;

import javax.ws.rs.core.UriBuilder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

/**
 * Encodes outbound HTTP requests with security credentials so that they can be authenticated
 * by the receiving server.
 */
public class RequestEncoder extends RequestWriter {

    private final String apiKey;
    private final String secretKey;
    private final SignatureGenerator signatureGenerator;
    private final RequestConfiguration requestConfiguration;

    public RequestEncoder(String apiKey,
                          String secretKey,
                          MessageBodyWorkers messageBodyWorkers,
                          SignatureGenerator signatureGenerator,
                          RequestConfiguration requestConfiguration) {

        super(messageBodyWorkers);
        this.apiKey = apiKey;
        this.secretKey = secretKey;
        this.signatureGenerator = signatureGenerator;
        this.requestConfiguration = requestConfiguration;
    }

    public void encode(ClientRequest request) {
        String timestamp = TimeUtils.getCurrentTimestamp();
        addApiKey(request);
        addTimestamp(request, timestamp);
        addSignature(request, timestamp);
        addVersion(request);
    }

    private void addApiKey(ClientRequest request) {
        URI uriWithApiKey = UriBuilder.fromUri(request.getURI())
                .queryParam(this.requestConfiguration.getApiKeyQueryParamName(), apiKey)
                .build();

        request.setURI(uriWithApiKey);
    }

    private void addSignature(ClientRequest request, String timestamp) {
        String signature = buildSignature(request, timestamp);
        request.getHeaders().putSingle(this.requestConfiguration.getSignatureHttpHeader(), signature);
    }

    private void addTimestamp(ClientRequest request, String timestamp) {
        request.getHeaders().putSingle(this.requestConfiguration.getTimestampHttpHeader(), timestamp);
    }

    private void addVersion(ClientRequest request) {
        request.getHeaders().putSingle(this.requestConfiguration.getVersionHttpHeader(), this.requestConfiguration.getVersion().getValue());
    }

    private String buildSignature(ClientRequest request, String timestamp) {
        String method = getMethod(request);
        String path = getPath(request);
        byte[] content = this.requestConfiguration.isDataInSignature() ? getContent(request) : null;

        return signatureGenerator.generate(secretKey, method, timestamp, path, content);
    }

    private String getMethod(ClientRequest request) {
        return request.getMethod();
    }

    private String getPath(ClientRequest request) {
        // Get the path and any query parameters (e.g. /api/v1/pizza?sort=toppings&apiKey=someKey)
        return String.format("%s?%s", request.getURI().getPath(), request.getURI().getQuery());
    }

    private byte[] getContent(ClientRequest request) {
        return getSerializedEntity(request);
    }

    /**
     * Get the serialized representation of the request entity. This is used when generating the client
     * signature, because this is the representation that the server will receive and use when it generates
     * the server-side signature to compare to the client-side signature.
     *
     * @see com.sun.jersey.client.urlconnection.URLConnectionClientHandler
     */
    private byte[] getSerializedEntity(ClientRequest request) {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            // By using the RequestWriter parent class, we match the behavior of entity writing from
            // for example, com.sun.jersey.client.urlconnection.URLConnectionClientHandler.
            writeRequestEntity(request, new RequestEntityWriterListener() {
                public void onRequestEntitySize(long size) throws IOException {
                }

                public OutputStream onGetOutputStream() throws IOException {
                    return outputStream;
                }
            });

        } catch (IOException e) {
            throw new ClientHandlerException("Unable to serialize request entity", e);
        }

        return outputStream.toByteArray();
    }

}
