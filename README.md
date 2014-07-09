# jersey-hmac-auth

Jersey-based HMAC authentication for the client and server.

This library makes it easy to implement HMAC authentication for REST API's implemented using the 
[Jersey](https://jersey.java.net) library. (Note that this works for any Jersey-based application or framework,
including the [Dropwizard](http://dropwizard.io/) framework.) 

You can use jersey-hmac-auth to ensure that only authorized callers are permitted to access your API. You do this by 
granting each API caller an API key and secret key, and they use these to construct API requests that can be authenticated 
by the server.


## Getting Started

### Server Side

Add this maven dependency:

```xml
<dependency>
    <groupId>com.bazaarvoice.auth</groupId>
    <artifactId>jersey-hmac-auth-server</artifactId>
    <version>${version}</version>
</dependency>
```

Modify your Jersey resource methods to include a principal annotated with `@HmacAuth`. For example:

```java
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

```java
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

```java
environment.addProvider(new HmacAuthProvider(new DefaultRequestHandler(new MyAuthenticator())));
```

### Client Side

On the client side, e.g. in an SDK library that interfaces with the API, the client must build requests following the
authentication contract that jersey-hmac-auth implements. You can do this in any language. However, the jersey-hmac-auth
library provides support in Java for client libraries that use the Jersey 
[Client](https://jersey.java.net/nonav/apidocs/1.17/jersey/com/sun/jersey/api/client/Client.html) for making HTTP requests.

Add this maven dependency:

```xml
<dependency>
    <groupId>com.bazaarvoice.auth</groupId>
    <artifactId>jersey-hmac-auth-client</artifactId>
    <version>${version}</version>
</dependency>
``` 

Add this filter to your Jersey client (assuming you have already have a Jersey client instance):

```java
client.addFilter(new HmacClientFilter(yourApiKey, yourSecretKey, client.getMessageBodyWorkers()));
```


## User Guide

See the [User Guide](https://github.com/bazaarvoice/jersey-hmac-auth/wiki) for more details.


## Contributing

To build and run tests locally:

```sh
$ git clone git@github.com:bazaarvoice/jersey-hmac-auth.git
$ cd jersey-hmac-auth
$ mvn clean install
```

To submit a new request or issue, please visit the [Issues](https://github.com/bazaarvoice/jersey-hmac-auth/issues) page.

Pull requests are always welcome.
