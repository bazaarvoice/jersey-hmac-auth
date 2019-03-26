# Jersey 2 - Client

Jersey2-client library for making HMAC authentication request to a server.

For `org.glassfish.jersey` packages.

```
<dependency>
    <groupId>com.bazaarvoice.auth</groupId>
    <artifactId>jersey-hmac-auth-client2</artifactId>
    <version>${version}</version>
</dependency>
``` 

Add filter to Jersey client.

```
Client httpClient = ClientBuilder.newClient();

RequestConfiguration requestConfiguration = RequestConfiguration.builder()
        // Whether or not to sign content body
        .withDataInSignature(false)
        .build();

httpClient.register(new HmacClientFilter(apiKey, apiKeySecret, requestConfiguration));
```
