package com.bazaarvoice.auth.hmac.server;

import com.bazaarvoice.auth.hmac.common.Credentials;
import com.bazaarvoice.auth.hmac.common.RequestConstants;
import com.bazaarvoice.auth.hmac.common.Version;
import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.api.core.HttpRequestContext;
import com.sun.jersey.core.util.ReaderWriter;
import com.sun.jersey.spi.container.ContainerRequest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;

public class RequestDecoder {

    public Credentials decode(HttpRequestContext request) {
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

    private String getPath(HttpRequestContext request) {
        // Get the path and any query parameters included (e.g. //localhost:8080/api/1/clients?apiKey=xxx)
        return request.getRequestUri().getSchemeSpecificPart();
    }

    private String getApiKey(HttpRequestContext request) {
        String apiKey = request.getQueryParameters().getFirst(RequestConstants.API_KEY_QUERY_PARAM);
        checkArgument(!isNullOrEmpty(apiKey), "Missing required API key");
        return apiKey;
    }

    private String getSignature(HttpRequestContext request) {
        return getRequiredHeaderField(request, RequestConstants.SIGNATURE_HTTP_HEADER);
    }

    private String getTimestamp(HttpRequestContext request) {
        return getRequiredHeaderField(request, RequestConstants.TIMESTAMP_HTTP_HEADER);
    }

    private byte[] getContent(HttpRequestContext request) {
        return safelyGetContent(request);
    }

    /**
     * Under normal circumstances, the body of the request can only be read once, because it is
     * backed by an {@code InputStream}, and thus is not easily consumed multiple times. This
     * method gets the request content and resets it so it can be read again later if necessary.
     */
    private byte[] safelyGetContent(HttpRequestContext request) {
        ContainerRequest containerRequest = (ContainerRequest) request;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InputStream in = containerRequest.getEntityInputStream();

        try {
            byte[] content = null;
            if (in.available() > 0) {
                ReaderWriter.writeTo(in, out);
                content = out.toByteArray();

                // Reset the input stream so that it can be read again by another filter or resource
                containerRequest.setEntityInputStream(new ByteArrayInputStream(content));
            }
            return content;

        } catch (IOException ex) {
            throw new ContainerException(ex);
        }
    }

    private String getMethod(HttpRequestContext request) {
        return request.getMethod();
    }

    private String getRequiredHeaderField(HttpRequestContext request, String name) {
        String value = request.getHeaderValue(name);
        checkArgument(!isNullOrEmpty(value), "Missing required HTTP header: " + name);
        return value;
    }
}
