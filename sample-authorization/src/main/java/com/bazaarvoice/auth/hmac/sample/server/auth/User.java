package com.bazaarvoice.auth.hmac.sample.server.auth;

public class User {
    private final String name;
    private final UserRole role;
    private final String apiKey;
    private final String secretKey;

    public User(String name, UserRole role, String apiKey, String secretKey) {
        this.name = name;
        this.role = role;
        this.apiKey = apiKey;
        this.secretKey = secretKey;
    }

    public String getName() {
        return name;
    }

    public UserRole getRole() {
        return role;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public boolean hasRight(UserRight ... rights) {
        return role.hasRight(rights);
    }
}
