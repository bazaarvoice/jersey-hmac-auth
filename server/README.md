# Jersey 1 - Server

Jersey1-server library for authenticating HMAC requests on a server.

For `com.sun.jersey` packages.

```
<dependency>
    <groupId>com.bazaarvoice.auth</groupId>
    <artifactId>jersey-hmac-auth-server</artifactId>
    <version>${version}</version>
</dependency>
``` 

Modify Jersey resource methods to include a principal annotated with `@HmacAuth`.

```
@Path("/pizza")
@Produces(MediaType.TEXT_PLAIN)
public class PizzaResource {
    @GET
    public String get(@HmacAuth Principal principal) {
        // This gets control only if the request is authenticated. 
        // The principal identifies the API caller (and can be of any type you want).
    }
}
```

Implement an authenticator to authenticate requests: 

```
public class MyAuthenticator extends AbstractCachingAuthenticator<Principal> {
    // some code is intentionally missing 
    
    @Override
    protected Principal loadPrincipal(Credentials credentials) {
        // return the principal identified by the credentials from the API request
    } 

    @Override
    protected String getSecretKeyFromPrincipal(Principal principal) {
        // return the secret key for the given principal
    }
}
```

Register the authenticator with Jersey. For example, using Dropwizard:

```
RequestConfiguration requestConfiguration = RequestConfiguration.builder()
    .withDataInSignature(Version.V3, true)     // Only accept v3 with data in signature
    .build()

DefaultRequestHandler requestHandler = new DefaultRequestHandler(new MyAuthenticator(), requestConfiguration);

environment.addProvider(new HmacAuthProvider(requestHandler));
```
