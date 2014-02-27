package com.bazaarvoice.auth.hmac.common;

import com.google.common.io.BaseEncoding;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class Signer {
    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final String UTF_8 = "UTF-8";

    private final String secretKey;

    public Signer(String secretKey) {
        this.secretKey = secretKey;
    }

    public String createSignature(byte[] message) {
        byte[] digest = calculateDigest(message);
        return BaseEncoding.base64Url().encode(digest);
    }

    private byte[] calculateDigest(byte[] message) {
        try {
            byte[] secretKeyBytes = secretKey.getBytes(UTF_8);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKeyBytes, HMAC_SHA256);
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(secretKeySpec);
            mac.update(message);
            return mac.doFinal();

        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Invalid character encoding: " + UTF_8, e);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Invalid MAC algorithm: " + HMAC_SHA256, e);
        } catch (InvalidKeyException e) {
            throw new IllegalStateException("Invalid MAC secret key", e);
        }
    }
}
