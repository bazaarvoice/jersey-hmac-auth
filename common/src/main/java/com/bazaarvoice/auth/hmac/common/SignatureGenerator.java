package com.bazaarvoice.auth.hmac.common;

/**
 * Generates an HMAC-based signature using a secret key and various other properties
 */
public class SignatureGenerator {

    public String generate(String secretKey, String method, String timestamp, String path, String content) {
        Signer signer = new Signer(secretKey);
        return signer.createSignature(method, timestamp, path, content);
    }
}
