# Jersey 2 - Server

Jersey2-server library for authenticating HMAC requests on a server.

For `org.glassfish.jersey` packages.

```
<dependency>
    <groupId>com.bazaarvoice.auth</groupId>
    <artifactId>jersey-hmac-auth-server2</artifactId>
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

Register the authenticator with Jersey.

```
public class PizzaApplication<P> extends ResourceConfig {
    public PizzaApplication() {
        // register the Feature that will tell Jersey to process the @HmacAuth annotations
        // specify your principal type here
        register(new HmacAuthFeature<String>());

        // tell Jersey about your custom Authenticator
        register(new AbstractBinder() {
            protected void configure() {
                // The P parameter is to trick HK2 into injecting the Authenticator where it is needed.
                bind(PizzaAuthenticator.class).to(new TypeLiteral<Authenticator<P>>() {});
            }
        });

        // register your resources
        register(PizzaResource2.class);
    }
}
```