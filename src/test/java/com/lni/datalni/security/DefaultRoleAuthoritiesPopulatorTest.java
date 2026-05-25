package com.lni.datalni.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultRoleAuthoritiesPopulatorTest {

    @Test
    void grantsConfiguredRoleWithRolePrefixToAnyUser() {
        var populator = new DefaultRoleAuthoritiesPopulator(SecurityRoles.VIEWER);

        var authorities = populator.getGrantedAuthorities(null, "anyone");

        assertThat(authorities).extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_LNI_VIEWER");
    }
}
