# jersey-hmac-auth

Jersey-based HMAC authentication for the client and server.

This library makes it easy to add HMAC authentication to REST API's that are implemented using the 
[Jersey](https://jersey.java.net) library. Note that this also works for Jersey-based frameworks, like
[Dropwizard](http://dropwizard.io/). 

HMAC authentication provides a way for you to ensure the integrity and authenticity of API requests. You grant 
API access to permitted callers by giving each one an API key and a secret key that they use when generating requests.
You can use this library to add support for HMAC authentication on the client and server.


## Getting Started

### Server Side (Jersey 2.x / `org.glassfish.jersey` packages)

If your application uses Jersey 2.x, add this Maven dependency:
```xml
<dependency>
    <groupId>com.bazaarvoice.auth</groupId>
    <artifactId>jersey-hmac-auth-server2</artifactId>
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

Register the authenticator with Jersey.

```java
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

See the [jersey-hmac-auth-sample2](sample-jersey2) project for a complete working example.

### Server Side (Jersey 1.x / `com.sun.jersey` packages)

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

### Client Side (Jersey 2.x / `org.glassfish.jersey` packages)            

If your application uses Jersey 2.x, add this Maven dependency:

```xml
<dependency>
    <groupId>com.bazaarvoice.auth</groupId>
    <artifactId>jersey-hmac-auth-client2</artifactId>
    <version>${version}</version>
</dependency>
``` 

Add this filter to your Jersey client (assuming you have already have a Jersey client instance):

```java
client.register(new HmacClientFilter(apiKey, secretKey));
```

### Client Side (Jersey 1.x / `com.sun.jersey` packages)

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

## Continuous Integration

[![Build Status](https://travis-ci.org/bazaarvoice/jersey-hmac-auth.png?branch=master)](https://travis-ci.org/bazaarvoice/jersey-hmac-auth)
