package com.bazaarvoice.auth.hmac.client;

import com.bazaarvoice.auth.hmac.common.SignatureGenerator;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.spi.MessageBodyWorkers;

import java.net.URI;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A Jersey client filter that modifies outbound HTTP requests to encode them for HMAC authentication.
 * This filter takes each request and modifies it to include the appropriate security parameters.
 */
public class HmacClientFilter extends ClientFilter {
    private final URI endpointToSecure;
    private final RequestEncoder requestEncoder;

    /**
     * Create a filter that will encode every request that is made by the Jersey client that is
     * using this filter.
     *
     * @param apiKey the API key
     * @param secretKey the secret key
     * @param messageBodyWorkers the {@link MessageBodyWorkers} utilized by the client
     */
    public HmacClientFilter(String apiKey, String secretKey, MessageBodyWorkers messageBodyWorkers) {
        this(apiKey, secretKey, messageBodyWorkers, null);
    }

    /**
     * Create a filter that will encode every request that is made to the specified
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
     * @param messageBodyWorkers the {@link MessageBodyWorkers} utilized by the client
     * @param endpointToSecure the endpoint {@link URI} to secure with this filter
     */
    public HmacClientFilter(String apiKey, String secretKey, MessageBodyWorkers messageBodyWorkers, URI endpointToSecure) {
        checkNotNull(apiKey, "apiKey");
        checkNotNull(secretKey, "secretKey");
        checkNotNull(messageBodyWorkers, "messageBodyWorkers");

        this.endpointToSecure = endpointToSecure;
        this.requestEncoder = new RequestEncoder(apiKey, secretKey, messageBodyWorkers, new SignatureGenerator());
    }

    @Override
    public ClientResponse handle(ClientRequest request) throws ClientHandlerException {
        // Modify the request to include security credentials if appropriate
        if (shouldEncode(request)) {
            requestEncoder.encode(request);
        }

        // Following the ClientFilter protocol, pass the request to the next filter in the chain
        return getNext().handle(request);
    }

    private boolean shouldEncode(ClientRequest request) {
        return endpointToSecure == null || shouldEncodeEndpoint(request.getURI());
    }

    private boolean shouldEncodeEndpoint(URI endpoint) {
        // Compare the "authority" portion of the URI (e.g. "localhost:5002" or "10.100.29.52").
        return endpointToSecure.getAuthority().equalsIgnoreCase(endpoint.getAuthority());
    }
}
