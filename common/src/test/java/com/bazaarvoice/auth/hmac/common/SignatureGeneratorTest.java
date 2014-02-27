package com.bazaarvoice.auth.hmac.common;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class SignatureGeneratorTest {
    private static final char DELIMITER = '\n';

    @Test
    public void generatesSignature() throws IOException {
        validateSignature("someContent".getBytes());
    }

    @Test
    public void generatesSignatureWithNullContent() throws IOException {
        validateSignature(null);
    }

    @Test
    public void generatesSignatureWithEmptyContent() throws IOException {
        validateSignature(new byte[0]);
    }

    private void validateSignature(byte[] content) throws IOException {
        String secretKey = "secretKey";
        String method = "method";
        String timestamp = "timestamp";
        String path = "path";

        String expected = createSignature(secretKey, method, timestamp, path, content);
        String actual = new SignatureGenerator().generate(secretKey, method, timestamp, path, content);
        assertEquals(expected, actual);
    }

    // Construct a signature following the same protocol that SignatureGenerator should be using
    private String createSignature(String secretKey, String method, String timestamp, String path, byte[] content)
            throws IOException {

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
    }
}
