package com.lni.datalni.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

/**
 * Dev-only in-memory authentication. Exposes a {@link DaoAuthenticationProvider} bean
 * so {@link SecurityConfig} can pick it up via {@code @Autowired(required = false)} and
 * fold it into the composite {@code AuthenticationManager}.
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
    public DaoAuthenticationProvider inMemoryAuthenticationProvider(PasswordEncoder encoder) {
        UserDetailsService users = new InMemoryUserDetailsManager(
                User.withUsername("admin").password(encoder.encode("admin"))
                        .roles(SecurityRoles.ADMIN).build(),
                User.withUsername("editor").password(encoder.encode("editor"))
                        .roles(SecurityRoles.EDITOR).build(),
                User.withUsername("viewer").password(encoder.encode("viewer"))
                        .roles(SecurityRoles.VIEWER).build());

        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(users);
        provider.setPasswordEncoder(encoder);
        return provider;
    }
}
