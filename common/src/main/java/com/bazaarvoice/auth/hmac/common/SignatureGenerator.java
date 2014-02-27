package com.bazaarvoice.auth.hmac.common;

import com.google.common.base.Throwables;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Generates an HMAC-based signature using a secret key and various other properties
 */
public class SignatureGenerator {
    private static final char DELIMITER = '\n';

    public String generate(String secretKey, String method, String timestamp, String path, byte[] content) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write(method.getBytes());
            out.write(DELIMITER);
            out.write(timestamp.getBytes());
            out.write(DELIMITER);
            out.write(path.getBytes());
            if (content != null && content.length > 0) {
                out.write(DELIMITER);
                out.write(content);
            }

            Signer signer = new Signer(secretKey);
            return signer.createSignature(out.toByteArray());

        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }
}
