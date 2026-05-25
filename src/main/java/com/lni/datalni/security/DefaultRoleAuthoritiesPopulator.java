package com.lni.datalni.security;

import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;

import java.util.List;

/**
 * Grants every successfully authenticated LDAP user a single base role. UEM's directory
 * has no LNI_* groups, so authorization roles are not read from LDAP; elevated roles are
 * managed elsewhere. The base role comes from {@code app.ldap.default-role}.
 */
public class DefaultRoleAuthoritiesPopulator implements LdapAuthoritiesPopulator {

    private final List<GrantedAuthority> authorities;

    public DefaultRoleAuthoritiesPopulator(String defaultRole) {
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + defaultRole));
    }

    @Override
    public List<GrantedAuthority> getGrantedAuthorities(DirContextOperations userData, String username) {
        return authorities;
    }
}
