Overview
========

Every request to the server is authenticated by taking various parameters provided by the user and authenticating them.

Here's a sample request:

```
GET /pizza?apiKey=myApiKey HTTP/1.1
X-Auth-Version: 1
X-Auth-Timestamp: 2014-02-10T06:13:15.402Z
X-Auth-Signature: yrVWPUAPAlV0sgAh22MYU-zR5unaoTrNTaTl11XjoMs=
```

These are the key parts of the request:
- `?apiKey` - The API key assigned to the API user
- `X-Auth-Version` - The version of the authentication scheme. Currently, there is only one version. However, if any 
aspect of this scheme changes in the future, it will be necessary to make those changes in a new version to ensure
backwards-compatibility.
- `X-Auth-Timestamp` - The request timestamp. This is used to protect against replay attacks. The request will
only be valid for a particular period of time, and this period is specified by the API implementation.
- `X-Auth-Signature` - The request signature.

The following pseudo-code shows how the signature is generated:

```
method = {HTTP request method - e.g. GET, PUT, POST}
timestamp = {the current UTC timestamp in ISO8601 format}
path = {the request path including all query parameters - e.g. "/pizza?apiKey=myApiKey"}
content = {the content in the request body, if any is specified on the request}

data = {method + '\n' + timestamp + '\n' + path + '\n' + content}
digest = hmac(secretKey, data.encode('utf-8'), sha256).digest()
return base64.urlsafe_b64encode(digest).strip()
```
