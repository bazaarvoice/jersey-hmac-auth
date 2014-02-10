package com.bazaarvoice.auth.hmac.client;

import com.bazaarvoice.auth.hmac.common.RequestConstants;
import com.bazaarvoice.auth.hmac.common.SignatureGenerator;
import com.bazaarvoice.auth.hmac.common.TimeUtils;
import com.bazaarvoice.auth.hmac.common.Version;
import com.sun.jersey.api.client.ClientRequest;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

/**
 * Encodes outbound HTTP requests with security credentials so that they can be authenticated
 * by the receiving server.
 */
public class RequestEncoder {
    private final String apiKey;
    private final String secretKey;
    private final SignatureGenerator signatureGenerator;

    public RequestEncoder(String apiKey, String secretKey, SignatureGenerator signatureGenerator) {
        this.apiKey = apiKey;
        this.secretKey = secretKey;
        this.signatureGenerator = signatureGenerator;
    }

    public void encode(ClientRequest request) {
        String timestamp = TimeUtils.getCurrentTimestamp();
        addApiKey(request);
        addTimestamp(request, timestamp);
        addSignature(request, timestamp);
        addVersion(request, Version.V1);
    }

    private void addApiKey(ClientRequest request) {
        URI uriWithApiKey = UriBuilder.fromUri(request.getURI())
                .queryParam(RequestConstants.API_KEY_QUERY_PARAM, apiKey)
                .build();

        request.setURI(uriWithApiKey);
    }

    private void addSignature(ClientRequest request, String timestamp) {
        String signature = buildSignature(request, timestamp);
        request.getHeaders().putSingle(RequestConstants.SIGNATURE_HTTP_HEADER, signature);
    }

    private void addTimestamp(ClientRequest request, String timestamp) {
        request.getHeaders().putSingle(RequestConstants.TIMESTAMP_HTTP_HEADER, timestamp);
    }

    private void addVersion(ClientRequest request, Version version) {
        request.getHeaders().putSingle(RequestConstants.VERSION_HTTP_HEADER, version.toString());
    }

    private String buildSignature(ClientRequest request, String timestamp) {
        String method = getMethod(request);
        String path = getPath(request);
        String content = getContent(request);
        return signatureGenerator.generate(secretKey, method, timestamp, path, content);
    }

    private String getMethod(ClientRequest request) {
        return request.getMethod();
    }

    private String getPath(ClientRequest request) {
        // Get the path and any query parameters included (e.g. //localhost:8080/api/1/clients?apiKey=xxx)
        return request.getURI().getSchemeSpecificPart();
    }

    private String getContent(ClientRequest request) {
        String content = "";
        if (request.getEntity() != null) {
            content = request.getEntity().toString();
        }
        return content;
    }
}
