# jersey-hmac-auth

Jersey-based HMAC authentication for the client and server.

This library makes it easy to add HMAC authentication to REST API's that are implemented using the 
[Jersey](https://jersey.java.net) library. Note that this also works for Jersey-based frameworks, like
[Dropwizard](http://dropwizard.io/). 

HMAC authentication provides a way for you to ensure the integrity and authenticity of API requests. You grant 
API access to permitted callers by giving each one an API key and a secret key that they use when generating requests.
You can use this library to add support for HMAC authentication on the client and server.

On the client side, e.g. in an SDK library that interfaces with the API, the client must build requests following the
authentication contract that jersey-hmac-auth implements. You can do this in any language. However, the jersey-hmac-auth
library provides support in Java for client libraries that use the Jersey 
[Client](https://jersey.java.net/nonav/apidocs/1.17/jersey/com/sun/jersey/api/client/Client.html) for making HTTP requests.

## Getting Started

### Server Side

* [Jersey 2.x](server2) - `org.glassfish.jersey`
* [Jersey 1.x](server) - `com.sun.jersey`
* [Jersey 2.x Sample](sample-jersey2)

### Client Side

* [Jersey 2.x](client2) - `org.glassfish.jersey`
* [Jersey 1.x](client) - `com.sun.jersey`

## HMAC Versions

The HMAC [version](https://github.com/bazaarvoice/jersey-hmac-auth/blob/master/common/src/main/java/com/bazaarvoice/auth/hmac/common/Version.java) so far have only been used to distinguished between using POST/PUT data in signature or not.

The server _may_ support any number of versions, however client must supply a version and appropriate include or not include data in signature. 

* **Version 1** - Has mixed implementations for including content data in signature (deprecated)
* **Version 2** - Will not include content data in signature
* **Version 3** - Will include content data in signature


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
