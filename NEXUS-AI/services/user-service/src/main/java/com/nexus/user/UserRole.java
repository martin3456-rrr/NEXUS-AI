package com.nexus.user;

import lombok.Getter;

@Getter
public enum UserRole {
    USER("USER", "Regular user with basic permissions"),
    MODERATOR("MODERATOR", "Moderator with content management permissions"),
    ADMIN("ADMIN", "Administrator with full system access");

    private final String roleName;
    private final String description;

    UserRole(String roleName, String description) {
        this.roleName = roleName;
        this.description = description;
    }

}
