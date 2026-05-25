package com.lni.datalni.security;

import com.lni.datalni.config.LdapProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.springframework.util.StringUtils;

/**
 * Production authentication against Active Directory. Uses
 * {@link ActiveDirectoryLdapAuthenticationProvider}, which performs the AD
 * {@code userPrincipalName} bind and reads group membership; {@link AppRolesMapper}
 * then converts AD groups to application roles.
 */
@Configuration
@Profile("!dev")
@EnableConfigurationProperties(LdapProperties.class)
public class ProdSecurityConfig {

    @Bean
    public AuthenticationManager authenticationManager(LdapProperties props) {
        ActiveDirectoryLdapAuthenticationProvider provider =
                StringUtils.hasText(props.getBaseDn())
                        ? new ActiveDirectoryLdapAuthenticationProvider(
                                props.getDomain(), props.getUrl(), props.getBaseDn())
                        : new ActiveDirectoryLdapAuthenticationProvider(
                                props.getDomain(), props.getUrl());
        provider.setConvertSubErrorCodesToExceptions(true);
        provider.setAuthoritiesMapper(new AppRolesMapper(props.getRoleMapping()));
        return new ProviderManager(provider);
    }
}
