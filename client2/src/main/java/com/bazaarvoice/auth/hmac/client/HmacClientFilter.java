package com.bazaarvoice.auth.hmac.client;

import com.bazaarvoice.auth.hmac.common.RequestConfiguration;
import com.bazaarvoice.auth.hmac.common.SignatureGenerator;
import org.glassfish.jersey.client.ClientRequest;

import java.io.IOException;
import java.net.URI;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.ext.Provider;

import javax.ws.rs.client.ClientRequestFilter;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A Jersey client filter that modifies outbound HTTP requests to encode them for HMAC authentication.
 * This filter takes each request and modifies it to include the appropriate security parameters.
 */
@Provider
public class HmacClientFilter implements ClientRequestFilter {
    private final URI endpointToSecure;
    private final RequestEncoder requestEncoder;

    /**
     * Create a filter that will encode every request that is made by the Jersey client
     * using this filter.
     *
     * @param apiKey the API key
     * @param secretKey the secret key
     */
    public HmacClientFilter(String apiKey, String secretKey) {
        this(apiKey, secretKey, null, new RequestConfiguration());
    }

    /**
     * Create a filter that will encode every request that is made by the Jersey client
     * using this filter.
     *
     * @param apiKey the API key
     * @param secretKey the secret key
     * @param requestConfiguration your settings for this Jersey client
     */
    public HmacClientFilter(String apiKey, String secretKey, RequestConfiguration requestConfiguration) {
        this(apiKey, secretKey, null, requestConfiguration);
    }

    /**
     * Create a filter that will encode every request made to the specified
     * endpoint. This is useful if the Jersey client to which this filter is applied
     * is used to invoke other services as well, ensuring that the specified API key
     * and secret keys are used to secure only the requests that are made to the given
     * endpoint.
     * <p>
     * The portion of the endpoint {@link URI} that is used is the "authority" of the URI, which
     * is essentially just the host and port, or just the host if there is no port specified.
     *
     * @param apiKey the API key
     * @param secretKey the secret key
     * @param endpointToSecure the endpoint {@link URI} to secure with this filter
     */
    public HmacClientFilter(String apiKey, String secretKey, URI endpointToSecure) {
        this(apiKey, secretKey, endpointToSecure, new RequestConfiguration());
    }

    /**
     * Create a filter that will encode every request made to the specified
     * endpoint. This is useful if the Jersey client to which this filter is applied
     * is used to invoke other services as well, ensuring that the specified API key
     * and secret keys are used to secure only the requests that are made to the given
     * endpoint.
     * <p>
     * The portion of the endpoint {@link URI} that is used is the "authority" of the URI, which
     * is essentially just the host and port, or just the host if there is no port specified.
     *
     * @param apiKey the API key
     * @param secretKey the secret key
     * @param endpointToSecure the endpoint {@link URI} to secure with this filter
     */
    public HmacClientFilter(String apiKey, String secretKey, URI endpointToSecure, RequestConfiguration requestConfiguration) {
        checkNotNull(apiKey, "apiKey");
        checkNotNull(secretKey, "secretKey");

        this.endpointToSecure = endpointToSecure;
        this.requestEncoder = new RequestEncoder(apiKey, secretKey, new SignatureGenerator(), requestConfiguration);
    }

    @Override
    public void filter(ClientRequestContext clientRequestContext) throws IOException {
        ClientRequest request = (ClientRequest) clientRequestContext;
        // Modify the request to include security credentials if appropriate
        if (shouldEncode(request)) {
            requestEncoder.encode(request);
        }
    }

    private boolean shouldEncode(ClientRequest request) {
        return endpointToSecure == null || shouldEncodeEndpoint(request.getUri());
    }

    private boolean shouldEncodeEndpoint(URI endpoint) {
        // Compare the "authority" portion of the URI (e.g. "localhost:5002" or "10.100.29.52").
        return endpointToSecure.getAuthority().equalsIgnoreCase(endpoint.getAuthority());
    }
}