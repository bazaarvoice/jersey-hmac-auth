package com.bazaarvoice.auth.hmac.client;

import com.bazaarvoice.auth.hmac.common.RequestConfiguration;
import com.bazaarvoice.auth.hmac.common.SignatureGenerator;
import org.glassfish.jersey.client.ClientRequest;

import java.io.IOException;

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
    private final RequestEncoder requestEncoder;

    /**
     * Create a filter that will encode every request that is made by the Jersey client
     * using this filter.
     *
     * @param apiKey the API key
     * @param secretKey the secret key
     */
    public HmacClientFilter(String apiKey, String secretKey) {
        checkNotNull(apiKey, "apiKey");
        checkNotNull(secretKey, "secretKey");

        this.requestEncoder = new RequestEncoder(apiKey, secretKey, new SignatureGenerator(), new RequestConfiguration());
    }

    @Override
    public void filter(ClientRequestContext clientRequestContext) throws IOException {
        requestEncoder.encode((ClientRequest) clientRequestContext);
    }
}