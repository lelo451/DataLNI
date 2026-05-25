package com.lni.datalni.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

/**
 * Development authentication: three in-memory users so role-based UI gating can be
 * exercised without Active Directory. Passwords equal the usernames.
 *
 * <ul>
 *   <li>{@code admin}  / {@code admin}  &rarr; LNI_ADMIN  (full CRUD)</li>
 *   <li>{@code editor} / {@code editor} &rarr; LNI_EDITOR (create/update/delete)</li>
 *   <li>{@code viewer} / {@code viewer} &rarr; LNI_VIEWER (read-only)</li>
 * </ul>
 */
@Configuration
@Profile("dev")
public class DevSecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        return new InMemoryUserDetailsManager(
                User.withUsername("admin").password(encoder.encode("admin"))
                        .roles(SecurityRoles.ADMIN).build(),
                User.withUsername("editor").password(encoder.encode("editor"))
                        .roles(SecurityRoles.EDITOR).build(),
                User.withUsername("viewer").password(encoder.encode("viewer"))
                        .roles(SecurityRoles.VIEWER).build());
    }

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService,
                                                       PasswordEncoder encoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(encoder);
        return new ProviderManager(provider);
    }
}
