Introduction
============

This library provides HMAC authentication to secure RESTful web service API's implemented using
[Jersey](https://jersey.java.net). This library can be used by any Jersey-based application, which includes
applications built using the [Dropwizard](http://dropwizard.codahale.com) framework.

Hash-based message authentication code (HMAC) is a mechanism for calculating a signature by using a hash function
in combination with a secret key. This can be used to verify the integrity and authenticity of a message.

The way it works at a high-level is as follows. A web service API is restricted to only users with a valid API key and
secret key. Users of the API build every request by calculating a signature using the secret key and various parameters
and then passing the signature on the request. The server then recalculates the signature using the secret key that
is associated with the user (via the supplied API key), and it compares the newly-generated signature to the one passed 
in by the user. If they match, then the user is authenticated and the web service can process the request. Because the
secret key is never passed across the "wire", the only way for the signatures to match is if the user has a valid secret
key. Therefore, if the signatures match, then the user is considered to be a trusted source.

This libary provides client and server implementations for HMAC authentication. The `server` module can be used to  
enable authentication for your RESTful API. If you have a Java client that is implemented using Jersey, then
you can use the `client` module to enable the client to build API requests that can be authenticated by the `server`
module code.


Details
-------

- [Overview](docs/overview.md)
- [Getting Started](docs/getting-started.md)
- [Contributing](docs/contributing.md)
