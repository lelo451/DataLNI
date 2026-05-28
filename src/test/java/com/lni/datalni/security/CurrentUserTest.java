package com.lni.datalni.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CurrentUser is a thin read-only adapter over {@link SecurityContextHolder}; these
 * tests prime the holder and observe the projection.
 */
class CurrentUserTest {

    private final CurrentUser currentUser = new CurrentUser();

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    private void authenticate(String username, String role) {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                username, "x", List.of(new SimpleGrantedAuthority("ROLE_" + role)));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void exposesRolesWithoutPrefixAndEditCapability() {
        authenticate("editor", SecurityRoles.EDITOR);

        assertThat(currentUser.isAuthenticated()).isTrue();
        assertThat(currentUser.getUsername()).isEqualTo("editor");
        assertThat(currentUser.getRoles()).containsExactly("LNI_EDITOR");
        assertThat(currentUser.canEdit()).isTrue();
    }

    @Test
    void viewerCannotEdit() {
        authenticate("viewer", SecurityRoles.VIEWER);

        assertThat(currentUser.canEdit()).isFalse();
    }

    @Test
    void clearedUserIsNotAuthenticated() {
        authenticate("a", SecurityRoles.VIEWER);
        SecurityContextHolder.clearContext();

        assertThat(currentUser.isAuthenticated()).isFalse();
        assertThat(currentUser.getRolesDisplay()).isEqualTo("-");
    }
}
