package com.bazaarvoice.auth.hmac.common;

/**
 * Those settings which one might need to change.
 * These may need to differ between HMAC connections.
 * It is an exercise for the user to ensure that the configuration on the client and the server match.
 */
public class RequestConfiguration {

    public static final String DEFAULT_SIGNATURE_HTTP_HEADER = "X-Auth-Signature";
    public static final String DEFAULT_TIMESTAMP_HTTP_HEADER = "X-Auth-Timestamp";
    public static final String DEFAULT_VERSION_HTTP_HEADER = "X-Auth-Version";
    public static final String DEFAULT_API_KEY_QUERY_PARAM = "apiKey";

    private final String signatureHttpHeader;
    private final String timestampHttpHeader;
    private final String versionHttpHeader;
    private final String apiKeyQueryParamName;

    public static Builder builder() {
        return new Builder();
    }

    public RequestConfiguration() {
        this.signatureHttpHeader = DEFAULT_SIGNATURE_HTTP_HEADER;
        this.timestampHttpHeader = DEFAULT_TIMESTAMP_HTTP_HEADER;
        this.versionHttpHeader = DEFAULT_VERSION_HTTP_HEADER;
        this.apiKeyQueryParamName = DEFAULT_API_KEY_QUERY_PARAM;
    }

    private RequestConfiguration(String signatureHttpHeader, String timestampHttpHeader, String versionHttpHeader, String apiKeyQueryParamName) {
        this.signatureHttpHeader = signatureHttpHeader;
        this.timestampHttpHeader = timestampHttpHeader;
        this.versionHttpHeader = versionHttpHeader;
        this.apiKeyQueryParamName = apiKeyQueryParamName;
    }

    public String getSignatureHttpHeader() {
        return signatureHttpHeader;
    }

    public String getTimestampHttpHeader() {
        return timestampHttpHeader;
    }

    public String getVersionHttpHeader() {
        return versionHttpHeader;
    }

    public String getApiKeyQueryParamName() {
        return apiKeyQueryParamName;
    }

    public static class Builder {
        private String signatureHttpHeader = DEFAULT_SIGNATURE_HTTP_HEADER;
        private String timestampHttpHeader = DEFAULT_TIMESTAMP_HTTP_HEADER;
        private String versionHttpHeader = DEFAULT_VERSION_HTTP_HEADER;
        private String apiKeyQueryParamName = DEFAULT_API_KEY_QUERY_PARAM;

        private Builder() {}

        public String getApiKeyQueryParamName() {
            return this.apiKeyQueryParamName;
        }

        public Builder withSignatureHttpHeader(String signatureHttpHeader) {
            this.signatureHttpHeader = signatureHttpHeader;
            return this;
        }

        public String getSignatureHttpHeader() {
            return this.signatureHttpHeader;
        }


        public String getTimestampHttpHeader() {
            return timestampHttpHeader;
        }

        public Builder withTimestampHttpHeader(String timestampHttpHeader) {
            this.timestampHttpHeader = timestampHttpHeader;
            return this;
        }

        public String getVersionHttpHeader() {
            return versionHttpHeader;
        }

        public Builder withVersionHttpHeader(String versionHttpHeader) {
            this.versionHttpHeader = versionHttpHeader;
            return this;
        }

        public Builder withApiKeyQueryParamName(String paramName) {
            this.apiKeyQueryParamName = paramName;
            return this;
        }

        public RequestConfiguration build() {
            return new RequestConfiguration(signatureHttpHeader, timestampHttpHeader, versionHttpHeader, apiKeyQueryParamName);
        }
    }
}
