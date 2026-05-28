package com.lni.datalni.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Read-only view of the authenticated principal for the current request thread.
 * Reads from {@link SecurityContextHolder} on every call — Spring Security's session
 * filter restores the {@code SecurityContext} per request, so multiple browser sessions
 * see only their own user.
 */
@Component
public class CurrentUser {

    private Authentication current() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public boolean isAuthenticated() {
        Authentication auth = current();
        return auth != null && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getPrincipal());
    }

    public String getUsername() {
        Authentication auth = current();
        return auth == null ? null : auth.getName();
    }

    /** Application roles without the {@code ROLE_} prefix (e.g. {@code LNI_ADMIN}). */
    public Set<String> getRoles() {
        Authentication auth = current();
        if (auth == null) {
            return Collections.emptySet();
        }
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).filter(Objects::nonNull)
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

    /** Convenience for the header strip: roles joined for display. */
    public String getRolesDisplay() {
        Set<String> roles = getRoles();
        if (roles.isEmpty()) {
            return "-";
        }
        return roles.stream().sorted().collect(Collectors.joining(", "));
    }
}
