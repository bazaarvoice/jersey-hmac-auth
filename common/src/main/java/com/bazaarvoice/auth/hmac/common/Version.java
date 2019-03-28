package com.bazaarvoice.auth.hmac.common;

/**
 * Represents a version of the authentication contract between the client and server.
 */
public enum Version {

    /**
     * Version 1
     * <p> - Has mix implementations for including content body in signature
     */
    @Deprecated
    V1("1", true),

    /**
     * Version 2
     * <p> - do not include content body in signature
     */
    V2("2", false),

    /**
     * Version 3
     * <p> - include content body in signature
     */
    V3("3", true),

    ;

    private String value;
    private boolean dataInSignature;

    Version(String value, boolean dataInSignature) {
        this.value = value;
        this.dataInSignature = dataInSignature;
    }

    public static Version fromValue(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Version value cannot be null");
        }

        for (Version version : Version.values()) {
            if (value.equalsIgnoreCase(version.value)) {
                return version;
            }
        }
        throw new IllegalArgumentException(value + " does not have a valid mapping in Version");
    }

    public String getValue() {
        return value;
    }

    public boolean isDataInSignature() {
        return dataInSignature;
    }

    @Override
    public String toString() {
        return value;
    }
}