package com.bazaarvoice.auth.hmac.common;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents a version of the authentication contract between the client and server
 */
public enum Version {
    V1("1");

    private String value;

    private Version(String value) {
        this.value = value;
    }

    public static Version fromValue(String value) {
        checkNotNull(value);

        for (Version version : Version.values()) {
            if (value.equalsIgnoreCase(version.value)) {
                return version;
            }
        }
        throw new IllegalArgumentException(value + " does not have a valid mapping in Version");
    }

    @Override
    public String toString() {
        return this.value;
    }
}