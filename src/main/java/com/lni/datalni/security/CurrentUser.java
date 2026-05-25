package com.lni.datalni.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Holds the authenticated principal for the desktop session. A single user is logged
 * in per JVM, so this singleton is the app-level view of {@code SecurityContextHolder}
 * and the source of truth the UI reads to gate controls.
 */
@Component
public class CurrentUser {

    private volatile Authentication authentication;

    public void set(Authentication authentication) {
        this.authentication = authentication;
    }

    public void clear() {
        this.authentication = null;
    }

    public boolean isAuthenticated() {
        return authentication != null && authentication.isAuthenticated();
    }

    public String getUsername() {
        return authentication == null ? null : authentication.getName();
    }

    /** Application roles without the {@code ROLE_} prefix (e.g. {@code LNI_ADMIN}). */
    public Set<String> getRoles() {
        if (authentication == null) {
            return Collections.emptySet();
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring(5))
                .collect(Collectors.toUnmodifiableSet());
    }

    public boolean hasRole(String role) {
        return getRoles().contains(role);
    }

    /** True when the user may create/update/delete. */
    public boolean canEdit() {
        return hasRole(SecurityRoles.ADMIN) || hasRole(SecurityRoles.EDITOR);
    }

    /** Convenience for the status bar: roles joined for display. */
    public String getRolesDisplay() {
        List<String> roles = getRoles().stream().sorted().toList();
        return roles.isEmpty() ? "-" : String.join(", ", roles);
    }
}
