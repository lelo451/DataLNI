package com.lni.datalni.security;

/**
 * Application role names and reusable method-security expressions.
 *
 * <p>Spring Security prefixes roles with {@code ROLE_}; {@code hasRole('LNI_ADMIN')}
 * therefore checks for authority {@code ROLE_LNI_ADMIN}. The granted authorities are
 * assembled from AD group membership (prod) or in-memory users (dev).
 *
 * <p>The expression strings are {@code static final} compile-time constants so they
 * can be referenced directly from {@code @PreAuthorize}.
 */
public final class SecurityRoles {

    private SecurityRoles() {
    }

    public static final String ADMIN = "LNI_ADMIN";
    public static final String EDITOR = "LNI_EDITOR";
    public static final String VIEWER = "LNI_VIEWER";

    /** Allowed to create/update/delete. */
    public static final String CAN_EDIT = "hasAnyRole('LNI_ADMIN', 'LNI_EDITOR')";

    /** Any successfully authenticated user (read access). */
    public static final String AUTHENTICATED = "isAuthenticated()";
}
