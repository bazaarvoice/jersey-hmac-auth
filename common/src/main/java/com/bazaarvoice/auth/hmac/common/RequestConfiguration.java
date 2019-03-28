package com.bazaarvoice.auth.hmac.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Those settings which one might need to change.
 * These may need to differ between HMAC connections.
 * It is an exercise for the user to ensure that the configuration on the client and the server match.
 */
public class RequestConfiguration {

    public static final String DEFAULT_SIGNATURE_HTTP_HEADER = "X-Auth-Signature";
    public static final String DEFAULT_TIMESTAMP_HTTP_HEADER = "X-Auth-Timestamp";
    public static final String DEFAULT_VERSION_HTTP_HEADER   = "X-Auth-Version";
    public static final String DEFAULT_API_KEY_QUERY_PARAM   = "apiKey";
    public static final Version DEFAULT_VERSION              = Version.V3;

    private Map<Version, Boolean> dataInSignatureMap = new HashMap<Version, Boolean>();

    private final String signatureHttpHeader;
    private final String timestampHttpHeader;
    private final String versionHttpHeader;
    private final String apiKeyQueryParamName;
    private final Version version;

    public static Builder builder() {
        return new Builder();
    }

    public RequestConfiguration() {
        this(DEFAULT_SIGNATURE_HTTP_HEADER, DEFAULT_TIMESTAMP_HTTP_HEADER, DEFAULT_VERSION_HTTP_HEADER, DEFAULT_API_KEY_QUERY_PARAM, DEFAULT_VERSION, null);
    }

    private RequestConfiguration(String signatureHttpHeader, String timestampHttpHeader, String versionHttpHeader, String apiKeyQueryParamName, Version version, Map<Version, Boolean> dataInSignatureMap) {
        this.signatureHttpHeader = signatureHttpHeader;
        this.timestampHttpHeader = timestampHttpHeader;
        this.versionHttpHeader = versionHttpHeader;
        this.apiKeyQueryParamName = apiKeyQueryParamName;
        this.version = version;

        if (dataInSignatureMap != null) {
            this.dataInSignatureMap.putAll(dataInSignatureMap);
        } else {
            // Initialize dataInSignatureMap defaults
            this.dataInSignatureMap.put(Version.V1, Version.V1.isDataInSignature());
            this.dataInSignatureMap.put(Version.V2, Version.V2.isDataInSignature());
            this.dataInSignatureMap.put(Version.V3, Version.V3.isDataInSignature());
        }
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

    public Version getVersion() {
        return version;
    }

    public boolean isDataInSignature() {
        return isDataInSignature(version);
    }

    public boolean isDataInSignature(Version version) {
        if (!dataInSignatureMap.containsKey(version)) {
            throw new IllegalArgumentException("Unsupported version: " + version);
        }
        return dataInSignatureMap.get(version);
    }

    public static class Builder {
        private String signatureHttpHeader = DEFAULT_SIGNATURE_HTTP_HEADER;
        private String timestampHttpHeader = DEFAULT_TIMESTAMP_HTTP_HEADER;
        private String versionHttpHeader = DEFAULT_VERSION_HTTP_HEADER;
        private String apiKeyQueryParamName = DEFAULT_API_KEY_QUERY_PARAM;
        private Version version = DEFAULT_VERSION;
        private Map<Version, Boolean> dataInSignatureMap = new HashMap<Version, Boolean>();

        private Builder() {}

        public Builder withSignatureHttpHeader(String signatureHttpHeader) {
            this.signatureHttpHeader = signatureHttpHeader;
            return this;
        }

        public Builder withTimestampHttpHeader(String timestampHttpHeader) {
            this.timestampHttpHeader = timestampHttpHeader;
            return this;
        }

        public Builder withVersionHttpHeader(String versionHttpHeader) {
            this.versionHttpHeader = versionHttpHeader;
            return this;
        }

        public Builder withApiKeyQueryParamName(String paramName) {
            this.apiKeyQueryParamName = paramName;
            return this;
        }

        public Builder withVersion(Version version) {
            this.version = version;
            return this;
        }

        public Builder withDataInSignature(Version version, boolean dataInSignature) {
            this.dataInSignatureMap.put(version, dataInSignature);
            return this;
        }

        public RequestConfiguration build() {
            if (version == null) {
                throw new IllegalArgumentException("Version cannot be null");
            }

            return new RequestConfiguration(signatureHttpHeader, timestampHttpHeader, versionHttpHeader, apiKeyQueryParamName, version, dataInSignatureMap);
        }
    }
}
