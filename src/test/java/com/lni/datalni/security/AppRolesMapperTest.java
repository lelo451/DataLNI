package com.lni.datalni.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AppRolesMapperTest {

    private final AppRolesMapper mapper = new AppRolesMapper(Map.of(
            "LNI_ADMINS", "LNI_ADMIN",
            "LNI_EDITORS", "LNI_EDITOR"));

    @Test
    void mapsKnownGroupsCaseInsensitivelyAndPrefixesRole() {
        var result = mapper.mapAuthorities(List.of(
                new SimpleGrantedAuthority("lni_admins"),
                new SimpleGrantedAuthority("UNKNOWN_GROUP")));

        assertThat(result).extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_LNI_ADMIN");
    }
}
