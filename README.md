# jersey-hmac-auth

Jersey-based HMAC authentication for the client and server.

This library makes it easy to add HMAC authentication to REST API's that are implemented using the 
[Jersey](https://jersey.java.net) library. Note that this also works for Jersey-based frameworks, like
[Dropwizard](http://dropwizard.io/). 

HMAC authentication provides a way for you to ensure the integrity and authenticity of API requests. You grant 
API access to permitted callers by giving each one an API key and a secret key that they use when generating requests.
You can use this library to add support for HMAC authentication on the client and server.

## Getting Started

Here's how to implement HMAC authentication for your API. The first section shows how to implement it on the
server to secure your API, and the subsequent section shows how to implement it in Java or Python clients. Clients
can be implemented in any other language just so long as they follow the required protocol when building requests.


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

For example:

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

Here's an example implementation:

```java
public class MyAuthenticator extends AbstractAuthenticator<Principal> {
    // some code is intentially missing 
    
    @Override
    protected Principal getPrincipal(Credentials credentials) {
        // Return the principal identified by the credentials from the API request
    } 

    @Override
    protected String getSecretKeyFromPrincipal(Principal principal) {
        // return the secret key for the given principal
    }
}
```

Register the authenticator with Jersey. For example, using Dropwizard:

```java
environment.addProvider(new HmacAuthProvider<HmacAuth, String>(new DefaultRequestHandler<HmacAuth, String>(new PizzaAuthenticator())) {});
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


## Authorization

Extend your use of this library to prevent some users from accessing higher privileged segments of your API. Checkout
the [sample-authorization](sample-authorization) directory for a completely working template. Instead of using `@HmacAuth`
you can use any annotation of your choosing and then implement `Authorizer` and pass that into the `DefaultRequestHandler`
to automatically authorize every request.

(1) Create an Annotation that exposes the required authorization right.

```java
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface SecureRPC {
    public UserRight[] requiredRights();
}
```

Where UserRight, for the sample app, is an enum. Although, you could communicate this information any way you please. The example enum is defined as:
```java
public enum UserRight {
    CREATE_NOTE,
    DELETE_NOTE,
    VIEW_NOTES
}
```

(2) Annotate your resource(s).

```java
public Collection<Note> createNote(@SecureRPC(requiredRights = UserRight.CREATE_NOTE) User user, ...
```

(3) Implement the `Authorizer` interface.

```java
public class SecureRPCAuthorizer implements Authorizer<SecureRPC, User> {
    @Override
    public boolean authorize(final SecureRPC annotation, final User principal) {
        return principal.hasRights(annotation.requiredRights());
    }
}
```

(4) Register the injectable provider with jersey.

```java
environment.addProvider(new HmacAuthProvider<SecureRPC, User>(new DefaultRequestHandler<>(new SimpleAuthenticator(), new SecureRPCAuthorizer())) {});
```

## Contributing

To build and run tests locally:

```sh
$ git clone git@github.com:bazaarvoice/jersey-hmac-auth.git
$ cd jersey-hmac-auth
$ mvn clean install
```

To submit a new request or issue, please visit the [Issues](https://github.com/bazaarvoice/jersey-hmac-auth/issues) page.

Pull requests are always welcome.
