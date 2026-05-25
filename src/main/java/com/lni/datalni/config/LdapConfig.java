package com.lni.datalni.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

import java.util.Map;

/**
 * LDAP connection beans for the non-dev profiles. Binds with the optional service
 * account ({@code app.ldap.manager-dn}); when blank, searches read-only as anonymous.
 * Connect/read timeouts keep a slow or unreachable directory from hanging login.
 */
@Configuration
@Profile("!dev")
public class LdapConfig {

    @Bean
    public LdapContextSource ldapContextSource(LdapProperties props) {
        LdapContextSource source = new LdapContextSource();
        source.setUrl(props.getUrl());
        source.setBase(props.getBaseDn());
        source.setUserDn(props.getManagerDn());
        source.setPassword(props.getManagerPassword());
        source.setAnonymousReadOnly(props.getManagerDn() == null || props.getManagerDn().isBlank());
        source.setBaseEnvironmentProperties(Map.of(
                "com.sun.jndi.ldap.connect.timeout", "3000",
                "com.sun.jndi.ldap.read.timeout", "5000"
        ));
        return source;
    }

    @Bean
    public LdapTemplate ldapTemplate(LdapContextSource ldapContextSource) {
        return new LdapTemplate(ldapContextSource);
    }
}
