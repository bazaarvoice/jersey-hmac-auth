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

    public String createSignature(String s1, String ... others) {
        if (s1 == null) {
            throw new IllegalArgumentException("s1 must be defined");
        }
        String stringToSign = s1;

        if (others != null) {
            for (String other : others) {
                if (other != null && other.length() > 0) {
                    stringToSign = append(stringToSign, other);
                }
            }
        }

        if (stringToSign.length() == 0) {
            throw new IllegalStateException("Nothing to sign!");
        }

        return createSignature(stringToSign);
    }

    private String createSignature(String stringToSign) {
        try {
            byte[] messageBytes = stringToSign.getBytes(UTF_8);
            byte[] tokenBytes = calculateDigest(messageBytes);
            return BaseEncoding.base64Url().encode(tokenBytes);

        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Need to support UTF_8");
        }
    }

    private byte[] calculateDigest(byte[] messageBytes) {
        try {
            byte[] secretKeyBytes = secretKey.getBytes(UTF_8);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKeyBytes, HMAC_SHA256);
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(secretKeySpec);
            mac.update(messageBytes);
            return mac.doFinal();

        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Invalid character encoding: " + UTF_8, e);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Invalid MAC algorithm: " + HMAC_SHA256, e);
        } catch (InvalidKeyException e) {
            throw new IllegalStateException("Invalid MAC secret key", e);
        }
    }

    private String append(String stringToSign, String parameter) {
        return stringToSign + '\n' + parameter;
    }
}
