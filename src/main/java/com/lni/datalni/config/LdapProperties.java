package com.lni.datalni.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * LDAP settings bound from {@code app.ldap.*}. Targets a standard (OpenLDAP-style)
 * directory: users are found by search filter, then bound to verify the password.
 */
@ConfigurationProperties(prefix = "app.ldap")
@Getter
@Setter
public class LdapProperties {

    /** e.g. {@code ldap://ldap.uem.br:389}. */
    private String url;

    /** Root/base DN the context binds to, e.g. {@code dc=uem,dc=br}. */
    private String baseDn;

    /** Subtree (relative to {@link #baseDn}) holding user entries, e.g. {@code ou=People}. */
    private String userSearchBase;

    /** Filter to locate a user by login name; {@code {0}} is the username. */
    private String userSearchFilter;

    /** Optional service account DN for the search bind; blank = anonymous read. */
    private String managerDn;

    /** Password for {@link #managerDn} (ignored when anonymous). */
    private String managerPassword;

    /** Application role granted to every authenticated user (without the ROLE_ prefix). */
    private String defaultRole;
}
