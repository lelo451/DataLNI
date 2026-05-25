package com.lni.datalni.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * LDAP / Active Directory settings bound from {@code app.ldap.*}. Used by the prod
 * security profile to configure {@code ActiveDirectoryLdapAuthenticationProvider}.
 */
@ConfigurationProperties(prefix = "app.ldap")
@Getter
@Setter
public class LdapProperties {

    /** e.g. {@code ldaps://ad-host:636}. */
    private String url;

    /** AD domain, e.g. {@code corp.example.com}. */
    private String domain;

    /** Root/base DN, e.g. {@code DC=corp,DC=example,DC=com}. */
    private String baseDn;

    /** Subtree to search for group membership, e.g. {@code OU=Groups}. */
    private String groupSearchBase;

    /** AD group name -> application role (e.g. {@code LNI_ADMINS -> LNI_ADMIN}). */
    private Map<String, String> roleMapping = new LinkedHashMap<>();
}
