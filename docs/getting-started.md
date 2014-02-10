Getting Started
===============

This document shows how to get started using the server side code and the client side code. They can be used 
independently, though, if you only need one or the other. One such case would be that you have implemented your
API using Jersey and want to use the server-side code, but you do not provide a client SDK to your users or you 
provide one that is not Jersey-based.

Server
------

The `server` module provides support for HMAC authentication on the server. To implement authentication for your  
API, just follow these steps:

(1) Add this maven dependency:

```
<dependency>
    <groupId>com.bazaarvoice.auth</groupId>
    <artifactId>jersey-hmac-auth-server</artifactId>
    <version>0.1</version>
</dependency>
```

(2) Add a parameter annotated with `@HmacAuth` to your Jersey resource methods.

For example:

```
@Path("/pizza")
@Produces(MediaType.TEXT_PLAIN)
public class PizzaResource {
    @GET
    public String get(@HmacAuth String principal) {
        // Add logic to get a pizza
    }
}
```

In this example, the `get` method will only get control after the request has been authenticated. The authenticated
principal will be injected into the method parameter. If authentication fails, then jersey-hmac-auth will 
automatically return a 401 status code.

(3) Implement an authenticator.

The easiest way to do this is to implement a class that extends `AbstractAuthenticator`. This will give you an
authenticator that knows how to check the request timestamp, validate the request signature, etc., and leaves
just a small amount of application-specific knowledge for your class to provide.

```
public class MyAuthenticator extends AbstractAuthenticator<String> {
    // some code is intentially missing 
    
    @Override
    protected String getPrincipal(Credentials credentials) {
        // Return the principal identified by the credentials from the API request
    } 

    @Override
    protected String getSecretKeyFromPrincipal(String principal) {
        // Return the secret key for the given principal
    }
}
```

If you want more control, you can create an authenticator that implements the `Authenticator` interface.

(4) Register the authentication provider with Jersey.

If using Dropwizard:

```
environment.addProvider(new HmacAuthProvider(new MyAuthenticator()));
```

If using straight Jersey, you basically do the same, but add the `HmacAuthProvider` to your Jersey environment.


Client
------

The `client` module provides support for HMAC authentication on the client. You are welcome to implement the 
security contract without this, for instance if your client SDK is not in Java or is but does not use Jersey.
If your client does use Jersey to build and send requests to the server, then you are in luck!

To implement this in a Jersey-based client SDK:

(1) Add this maven dependency:

```
<dependency>
    <groupId>com.bazaarvoice.auth</groupId>
    <artifactId>jersey-hmac-auth-client</artifactId>
    <version>0.1</version>
</dependency>
``` 

(2) Add the `HmacClientFilter` to your Jersey client:

```
Client jerseyClient = createJerseyClient();         // assuming this is implemented in your code
jerseyClient.addFilter(new HmacClientFilter(apiKey, secretKey));
```
