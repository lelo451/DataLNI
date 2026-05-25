package com.lni.datalni.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Translates AD group names (the authorities produced by the LDAP provider) into
 * application roles using the configured {@code app.ldap.role-mapping}. Each mapped
 * group becomes a {@code ROLE_<appRole>} authority; unmapped groups are dropped.
 */
public class AppRolesMapper implements GrantedAuthoritiesMapper {

    private final Map<String, String> groupToRole;

    public AppRolesMapper(Map<String, String> roleMapping) {
        // Normalise keys to upper-case for case-insensitive AD group matching.
        this.groupToRole = new java.util.HashMap<>();
        if (roleMapping != null) {
            roleMapping.forEach((group, role) ->
                    this.groupToRole.put(group.toUpperCase(Locale.ROOT), role));
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> mapAuthorities(
            Collection<? extends GrantedAuthority> authorities) {
        Set<GrantedAuthority> mapped = new LinkedHashSet<>();
        for (GrantedAuthority authority : authorities) {
            String name = stripRolePrefix(authority.getAuthority()).toUpperCase(Locale.ROOT);
            String appRole = groupToRole.get(name);
            if (appRole != null) {
                mapped.add(new SimpleGrantedAuthority("ROLE_" + appRole));
            }
        }
        return mapped;
    }

    private static String stripRolePrefix(String authority) {
        return authority.startsWith("ROLE_") ? authority.substring(5) : authority;
    }
}
