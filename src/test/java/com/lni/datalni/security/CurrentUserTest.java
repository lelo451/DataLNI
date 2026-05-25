package com.lni.datalni.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CurrentUserTest {

    @Test
    void exposesRolesWithoutPrefixAndEditCapability() {
        CurrentUser currentUser = new CurrentUser();
        currentUser.set(new UsernamePasswordAuthenticationToken("editor", "x",
                List.of(new SimpleGrantedAuthority("ROLE_LNI_EDITOR"))));

        assertThat(currentUser.isAuthenticated()).isTrue();
        assertThat(currentUser.getUsername()).isEqualTo("editor");
        assertThat(currentUser.getRoles()).containsExactly("LNI_EDITOR");
        assertThat(currentUser.canEdit()).isTrue();
    }

    @Test
    void viewerCannotEdit() {
        CurrentUser currentUser = new CurrentUser();
        currentUser.set(new UsernamePasswordAuthenticationToken("viewer", "x",
                List.of(new SimpleGrantedAuthority("ROLE_LNI_VIEWER"))));

        assertThat(currentUser.canEdit()).isFalse();
    }

    @Test
    void clearedUserIsNotAuthenticated() {
        CurrentUser currentUser = new CurrentUser();
        currentUser.set(new UsernamePasswordAuthenticationToken("a", "b", List.of()));
        currentUser.clear();

        assertThat(currentUser.isAuthenticated()).isFalse();
        assertThat(currentUser.getRolesDisplay()).isEqualTo("-");
    }
}
