package com.bazaarvoice.auth.hmac.server;

import com.bazaarvoice.auth.hmac.common.Credentials;
import com.bazaarvoice.auth.hmac.common.Credentials.CredentialsBuilder;
import com.bazaarvoice.auth.hmac.common.RequestConfiguration;
import com.bazaarvoice.auth.hmac.common.Version;
import com.google.common.io.ByteStreams;
import org.apache.commons.lang.Validate;
import org.glassfish.hk2.api.Factory;
import org.glassfish.jersey.server.ContainerRequest;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;


/**
 * {@link Factory} for creating a principal wherever it is required for a request.
 *
 * @param <P> The type of principal
 * @see Authenticator
 */
public class PrincipalFactory<P> implements Factory<P> {

    private final Authenticator<? extends P> authenticator;
    private final Provider<? extends ContainerRequest> requestProvider;
    private final RequestConfiguration requestConfiguration;

    /**
     * @param authenticator the application's credential authenticator (required)
     * @param requestProvider object that provides access to the active request (required)
     */
    @Inject
    public PrincipalFactory(final Authenticator<P> authenticator,
                            final Provider<ContainerRequest> requestProvider,
                            final RequestConfiguration requestConfiguration) {

        // we could technically declare the dependency as Authenticator<? extends P>, but that complicates HK2
        // dependency-injection
        Validate.notNull(authenticator, "authenticator cannot be null");
        this.authenticator = authenticator;
        this.requestProvider = requestProvider;
        this.requestConfiguration = requestConfiguration;
    }

    public P provide() {
        final ContainerRequest request = getRequestProvider().get();
        final UriInfo uriInfo = request.getUriInfo();
        final URI requestUri = uriInfo.getRequestUri();

        final MultivaluedMap<? super String, ? extends String> queryParameters = uriInfo
                .getQueryParameters();
        String apiKeyName = getAuthenticator().getApiKeyName();
        final List<? extends String> apiKeys = queryParameters.get(apiKeyName);
        if (apiKeys == null || apiKeys.isEmpty()) {
            throw new BadRequestException("apiKey is required in param: " + apiKeyName);
        }
        if (request.getHeaderString(requestConfiguration.getSignatureHttpHeader()) == null ||
            request.getHeaderString(requestConfiguration.getTimestampHttpHeader()) == null ||
            request.getHeaderString(requestConfiguration.getVersionHttpHeader()) == null) {

            throw new BadRequestException("Required auth headers not present: " +
                    requestConfiguration.getSignatureHttpHeader() + ", " +
                    requestConfiguration.getTimestampHttpHeader() + ", " +
                    requestConfiguration.getVersionHttpHeader());
        }

        Version version = Version.fromValue(request.getHeaderString(requestConfiguration.getVersionHttpHeader()));

        final CredentialsBuilder builder = Credentials.builder();
        builder.withApiKey(!apiKeys.isEmpty() ? apiKeys.get(0) : null);
        builder.withSignature(request.getHeaderString(requestConfiguration.getSignatureHttpHeader()));
        builder.withTimestamp(request.getHeaderString(requestConfiguration.getTimestampHttpHeader()));
        builder.withVersion(version);
        builder.withMethod(request.getMethod());
        builder.withPath(requestUri.getPath() + "?" + requestUri.getQuery());

        // Content
        if (requestConfiguration.isDataInSignature(version) && request.hasEntity()) {
            try {
                final InputStream inputStream = request.getEntityStream();
                try {
                    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    ByteStreams.copy(inputStream, outputStream);

                    final byte[] bytes = outputStream.toByteArray();
                    builder.withContent(bytes);
                    request.setEntityStream(new ByteArrayInputStream(bytes));
                } finally {
                    inputStream.close();
                }
            } catch (final IOException ioe) {
                throw new InternalServerErrorException("Error reading content", ioe);
            }
        }

        final P retval = getAuthenticator().authenticate(builder.build());
        if (retval == null) {
            throw new NotAuthorizedException(Response.status(Response.Status.UNAUTHORIZED).build());
        }
        return retval;
    }

    public void dispose(final P instance) {
    }

    protected Authenticator<? extends P> getAuthenticator() {
        return authenticator;
    }

    protected Provider<? extends ContainerRequest> getRequestProvider() {
        return requestProvider;
    }

}