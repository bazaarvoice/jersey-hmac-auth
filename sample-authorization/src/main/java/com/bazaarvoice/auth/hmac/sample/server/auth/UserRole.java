package com.bazaarvoice.auth.hmac.sample.server.auth;

import java.util.EnumSet;
import java.util.Set;

public enum UserRole {
    ADMINISTRATOR(EnumSet.allOf(UserRight.class)),
    USER(EnumSet.of(UserRight.VIEW_NOTES)),;

    private final Set<UserRight> _rights;

    UserRole(Set<UserRight> rights) {
        _rights = rights;
    }

    public Set<UserRight> getRights() {
        return _rights;
    }

    public boolean hasRights(UserRight... rights) {
        for (UserRight right : rights) {
            if (!_rights.contains(right)) {
                return false;
            }
        }
        return true;
    }
}
