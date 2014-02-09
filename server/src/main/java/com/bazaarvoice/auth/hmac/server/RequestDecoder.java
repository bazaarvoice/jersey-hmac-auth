package com.bazaarvoice.auth.hmac.server;

import com.bazaarvoice.auth.hmac.common.RequestConstants;
import com.bazaarvoice.auth.hmac.common.Version;
import com.sun.jersey.api.core.HttpRequestContext;

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

    private String getContent(HttpRequestContext request) {
        return request.getEntity(String.class);
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
