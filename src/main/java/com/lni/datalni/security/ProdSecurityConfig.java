package com.lni.datalni.security;

import com.lni.datalni.config.LdapProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;

/**
 * Production authentication against a standard LDAP directory (e.g. ldap.uem.br).
 * Users are located by {@code app.ldap.user-search-filter} under
 * {@code app.ldap.user-search-base}, then bound to verify the password
 * ({@link BindAuthenticator}). Every authenticated user receives the configured base
 * role via {@link DefaultRoleAuthoritiesPopulator}.
 */
@Configuration
@Profile("!dev")
@EnableConfigurationProperties(LdapProperties.class)
public class ProdSecurityConfig {

    @Bean
    public AuthenticationManager authenticationManager(LdapContextSource contextSource,
                                                       LdapProperties props) {
        FilterBasedLdapUserSearch userSearch = new FilterBasedLdapUserSearch(
                props.getUserSearchBase(), props.getUserSearchFilter(), contextSource);

        BindAuthenticator authenticator = new BindAuthenticator(contextSource);
        authenticator.setUserSearch(userSearch);

        LdapAuthenticationProvider provider = new LdapAuthenticationProvider(
                authenticator, new DefaultRoleAuthoritiesPopulator(props.getDefaultRole()));
        return new ProviderManager(provider);
    }
}
