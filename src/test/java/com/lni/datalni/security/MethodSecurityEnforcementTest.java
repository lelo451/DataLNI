package com.lni.datalni.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.stereotype.Service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Proves the service-layer authorization rule ({@link SecurityRoles#CAN_EDIT}) is enforced
 * by {@code @EnableMethodSecurity}. Uses a minimal security-only context (no JPA/DB) with a
 * stand-in guarded bean carrying the same expression the real services use.
 */
@SpringBootTest(classes = {MethodSecurityConfig.class, MethodSecurityEnforcementTest.Guarded.class})
class MethodSecurityEnforcementTest {

    @Service
    static class Guarded {
        @PreAuthorize(SecurityRoles.CAN_EDIT)
        public String mutate() {
            return "ok";
        }
    }

    @Autowired
    private Guarded guarded;

    @Test
    @WithMockUser(roles = "LNI_EDITOR")
    void editorIsAllowedToMutate() {
        assertThat(guarded.mutate()).isEqualTo("ok");
    }

    @Test
    @WithMockUser(roles = "LNI_VIEWER")
    void viewerIsDenied() {
        assertThatThrownBy(guarded::mutate).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithAnonymousUser
    void anonymousIsDenied() {
        assertThatThrownBy(guarded::mutate).isInstanceOf(AccessDeniedException.class);
    }
}
