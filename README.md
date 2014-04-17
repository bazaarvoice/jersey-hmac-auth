Welcome
=======

This library enables HMAC authentication for web service API's so that only authorized callers are permitted 
to use the API. As the owner of a web service, you grant permission to callers by giving each one an API key and 
a secret key. With these, the caller can construct API requests such that they can be authenticated by the server 
and only processed if authenticated successfully.

This provides support on the server-side for web services implemented in Java using [Jersey](https://jersey.java.net), 
and provides support on the client-side for client libraries that use Jersey to interface with them.


What is HMAC?
=============

Hash-based message authentication code (HMAC) is a mechanism for calculating a signature by using a hash function
in combination with a secret key. This can be used to verify the integrity and authenticity of a message.

Here's an overview of how it works. A web service restricts API access by giving each client an API key and secret
key. Clients construct each request by providing certain parameters, including a signature generated using the
secret key and some parts of the request itself. When the server receives the request, it recalculates the signature 
using the same method as the client and compares it to the one passed by the client. If they match, then the request
is considered authentic and processing continues. Otherwise, the request is rejected as unauthorized. 

Because the secret key is not passed across the "wire", the signatures will only match if the caller has a valid secret
key. Also, since the signature is generated using various parts of the request itself, the signatures will not match if
those aspects of the request were tampered with during transmission or if the request is modified and retransmitted for
malicious purposes. If the signatures match, then the client is considered to be a trusted source and the request is
deemed authentic.


How does it work?
=================

The following is the protocol for authenticating requests:

(1) The client is granted an API key and secret key.

(2) The client generates a request as follows:

```
GET /pizza?apiKey=my-api-key HTTP/1.1
X-Auth-Version: 1
X-Auth-Timestamp: 2014-02-10T06:13:15.402Z
X-Auth-Signature: yrVWPUAPAlV0sgAh22MYU-zR5unaoTrNTaTl11XjoMs=
```

The signature specified by the `X-Auth-Signature` header is created as follows. It is constructed using the secret key and
various request parameters:
  
```python
method = {HTTP request method - e.g. GET, PUT, POST}
timestamp = {the current UTC timestamp in ISO8601 format}
path = {the request path including all query parameters - e.g. "/pizza?apiKey=my-api-key"}
content = {the content in the request body, if any is specified on the request}

data = {method + '\n' + timestamp + '\n' + path}
if content:
    data += {'\n' + content}
digest = hmac(secretKey, data.encode('utf-8'), sha256).digest()
return base64.urlsafe_b64encode(digest).strip()
```
  
(3) The server receives and authenticates the request.

The server uses the API key to identify the caller and retrieve their secret key from where it happens to store it,
generates a signature just like the client did when building the request, and compares its signature to the one passed
by the client. If the signatures match, then the request is valid and the API request can be processed. Otherwise, the
server returns a "401 (Unauthorized)" HTTP status code.


Getting Started
===============

Here's how to implement HMAC authentication for your API. The first section shows how to implement it on the
server to secure your API, and the subsequent section shows how to implement it in Java or Python clients. Clients
can be implemented in any other language just so long as they follow the required protocol when building requests.


Server
------

(1) Add this maven dependency:

```xml
<dependency>
    <groupId>com.bazaarvoice.auth</groupId>
    <artifactId>jersey-hmac-auth-server</artifactId>
    <version>${version}</version>
</dependency>
```

(2) Add a parameter annotated with `@HmacAuth` to your Jersey resource methods.

For example:

```java
@Path("/pizza")
@Produces(MediaType.TEXT_PLAIN)
public class PizzaResource {
    @GET
    public String get(@HmacAuth String principal) {
        // Some logic to get a pizza
    }
}
```

The `get` method will only receive control after the request has been authenticated. The authenticated principal
will be injected into the method parameter should you choose to use it. If authentication fails, then 
jersey-hmac-auth will automatically return a "401" HTTP status code.

(3) Implement an authenticator.

You can extend the `AbstractAuthenticator` class to get the following features out of the box:

- Validate the request timestamp to ensure that it does not fall outside of the allowed time range. This is used 
to reduce the window of time for which a replay attack can occur.
- Make sure the API key identifies an actual principal.
- Validate the signature to ensure that the caller used a valid secret key to generate the request signature and that 
the request has not been tampered with after being sent.

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
        // Return the secret key for the given principal
    }
}
```

Other options:

- For **caching**, you can alternatively extend `AbstractCachingAuthenticator`. This is recommended to reduce API key 
lookups, especially if your service uses a database or calls some other service to retrieve the principal.
- For something more custom, you can create an authenticator that implements the `Authenticator` interface and 
provide all your own authentication logic.

(4) Register the authentication provider with Jersey.

If using Dropwizard:

```java
environment.addProvider(new HmacAuthProvider<HmacAuth, Principal>(new DefaultRequestHandler(new MyAuthenticator())) {});
```

If using straight Jersey, you basically do the same, but add the `HmacAuthProvider` to your Jersey environment.

Both implementations require specifying (or implementing your own) `RequestHandler`.  There are three `RequestHandler`s
provided for use:

* [DefaultRequestHandler](server/src/main/java/com/bazaarvoice/auth/hmac/server/PassThroughRequestHandler.java) - for general use, requires all requests to include proper authentication
* [OptionalRequestHandler](server/src/main/java/com/bazaarvoice/auth/hmac/server/OptionalRequestHandler.java) - relaxed, does not require authentication, but will authenticate if credentials are provided
* [PassThroughRequestHandler](server/src/main/java/com/bazaarvoice/auth/hmac/server/PassThroughRequestHandler.java) - for testing, simply returns the Principal passed in

Java Client
-----------

To implement a Java client (using Jersey) that constructs requests encoded for HMAC authentication:

(1) Add this maven dependency:

```xml
<dependency>
    <groupId>com.bazaarvoice.auth</groupId>
    <artifactId>jersey-hmac-auth-client</artifactId>
    <version>${version}</version>
</dependency>
``` 

(2) Add the `HmacClientFilter` to your Jersey client:

```java
Client client;              // this is your Jersey client constructed someplace else
client.addFilter(new HmacClientFilter(apiKey, secretKey, client.getMessageBodyWorkers()));
```

Python Client
-------------

To implement a Python client that constructs requests encoded for HMAC authentication, please refer to the
[python-hmac-auth](https://github.com/bazaarvoice/python-hmac-auth) library.


Sample application
==================

There is a sample Dropwizard-based service available in the [sample-dropwizard](sample-dropwizard) directory that
demonstrates how to integrate with this library.

Authorization
=============

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

Where UserRight, for the sample app, is an enum. Although, you could communicate this information any way you please. The enum is defined as:
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

Contributing
============

Pull requests are always welcome and appreciated.

To build locally, clone or fork the repository and then run `mvn clean install` from the project directory.

To submit new requests/issues or to see existing requests/issues, please 
visit the [Issues](https://github.com/bazaarvoice/jersey-hmac-auth/issues) page.
