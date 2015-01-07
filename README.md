# jersey-hmac-auth

Jersey-based HMAC authentication for the client and server.

This library makes it easy to add HMAC authentication to REST API's that are implemented using the 
[Jersey](https://jersey.java.net) library. Note that this also works for Jersey-based frameworks, like
[Dropwizard](http://dropwizard.io/). 

HMAC authentication provides a way for you to ensure the integrity and authenticity of API requests. You grant 
API access to permitted callers by giving each one an API key and a secret key that they use when generating requests.
You can use this library to add support for HMAC authentication on the client and server.


## Getting Started

### Server Side

Add this maven dependency:

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
environment.addProvider(new HmacAuthProvider<HmacAuth, Principal>(new DefaultRequestHandler(new MyAuthenticator())) {});
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
