package com.bazaarvoice.auth.hmac.server;

import com.bazaarvoice.auth.hmac.common.Credentials;
import com.bazaarvoice.auth.hmac.common.SignatureGenerator;
import com.bazaarvoice.auth.hmac.common.Version;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import static com.bazaarvoice.auth.hmac.common.TimeUtils.nowInUTC;

class TestCredentials {
    static Credentials createCredentials(String apiKey, String secretKey) {
        return createCredentials(apiKey, secretKey, nowInUTC());
    }

    static Credentials createCredentials(String apiKey, String secretKey, DateTime requestTime) {
        String method = "GET";
        String timestamp = ISODateTimeFormat.dateTime().print(requestTime);
        String path = "/example?apiKey=foo";
        byte[] content = "some request content".getBytes();
        String signature = new SignatureGenerator().generate(secretKey, method, timestamp, path, content);

        return Credentials.builder()
            .withVersion(Version.V1)
            .withApiKey(apiKey)
            .withTimestamp(timestamp)
            .withMethod(method)
            .withPath(path)
            .withContent(content)
            .withSignature(signature)
            .build();
    }

}
