package com.bazaarvoice.auth.hmac.server;

import com.bazaarvoice.auth.hmac.common.Credentials;
import com.bazaarvoice.auth.hmac.common.RequestConfiguration;
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
    private final RequestConfiguration requestConfiguration;

    public RequestDecoder(RequestConfiguration requestConfiguration) {

        this.requestConfiguration = requestConfiguration;
    }

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
        // Get the path and any query parameters (e.g. /api/v1/pizza?sort=toppings&apiKey=someKey)
        return String.format("%s?%s", request.getRequestUri().getPath(), request.getRequestUri().getQuery());
    }

    private String getApiKey(HttpRequestContext request) {
        String apiKey = request.getQueryParameters().getFirst(this.requestConfiguration.getApiKeyQueryParamName());
        checkArgument(!isNullOrEmpty(apiKey), "Missing required API key");
        return apiKey;
    }

    private String getSignature(HttpRequestContext request) {
        return getRequiredHeaderField(request, this.requestConfiguration.getSignatureHttpHeader());
    }

    private String getTimestamp(HttpRequestContext request) {
        return getRequiredHeaderField(request, this.requestConfiguration.getTimestampHttpHeader());
    }

    private byte[] getContent(HttpRequestContext request) {
        if (this.requestConfiguration.isDataInSignature()) {
            return safelyGetContent(request);
        }
        return null;
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
            ReaderWriter.writeTo(in, out);
            byte[] content = out.toByteArray();

            // Reset the input stream so that it can be read again by another filter or resource
            containerRequest.setEntityInputStream(new ByteArrayInputStream(content));
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
