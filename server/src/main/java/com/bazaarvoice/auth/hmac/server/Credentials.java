package com.bazaarvoice.auth.hmac.server;

import com.bazaarvoice.auth.hmac.common.Version;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.nullToEmpty;

public class Credentials {
    private final Version version;
    private final String apiKey;
    private final String signature;
    private final String path;
    private final String timestamp;
    private final String method;
    private final String content;

    public Credentials(Version version, String apiKey, String signature, String path,
                       String timestamp, String method, String content) {

        this.version    = checkNotNull(version);
        this.apiKey     = checkNotNull(apiKey);
        this.signature  = checkNotNull(signature);
        this.path       = checkNotNull(path);
        this.timestamp  = checkNotNull(timestamp);
        this.method     = checkNotNull(method);
        this.content    = nullToEmpty(content);    // optional - not all requests include content in the request body
    }

    public static CredentialsBuilder builder() {
        return new CredentialsBuilder();
    }

    public Version getVersion() {
        return version;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getSignature() {
        return signature;
    }

    public String getPath() {
        return path;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getMethod() {
        return method;
    }

    public String getContent() {
        return content;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) { return true; }
        if (obj == null || (getClass() != obj.getClass())) { return false; }

        Credentials that = (Credentials) obj;

        return version.equals(that.version)
                && apiKey.equals(that.apiKey)
                && signature.equals(that.signature)
                && path.equals(that.path)
                && timestamp.equals(that.timestamp)
                && method.equals(that.method)
                && content.equals(that.content);
    }

    @Override
    public int hashCode() {
        int result = version != null ? version.hashCode() : 0;
        result = 31 * result + (apiKey != null ? apiKey.hashCode() : 0);
        result = 31 * result + (signature != null ? signature.hashCode() : 0);
        result = 31 * result + (path != null ? path.hashCode() : 0);
        result = 31 * result + (timestamp != null ? timestamp.hashCode() : 0);
        result = 31 * result + (content != null ? content.hashCode() : 0);
        result = 31 * result + (method != null ? method.hashCode() : 0);
        return result;
    }

    public static class CredentialsBuilder {
        private Version version = Version.V1;
        private String apiKey = "";
        private String signature = "";
        private String path = "";
        private String timestamp = "";
        private String method = "";
        private String content = "";

        public Credentials build() {
            return new Credentials(version, apiKey, signature, path, timestamp, method, content);
        }

        public CredentialsBuilder withVersion(Version version) {
            this.version = version;
            return this;
        }

        public CredentialsBuilder withApiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public CredentialsBuilder withSignature(String signature) {
            this.signature = signature;
            return this;
        }

        public CredentialsBuilder withPath(String path) {
            this.path = path;
            return this;
        }

        public CredentialsBuilder withTimestamp(String timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public CredentialsBuilder withMethod(String method) {
            this.method = method;
            return this;
        }

        public CredentialsBuilder withContent(String content) {
            this.content = content;
            return this;
        }
    }
}
