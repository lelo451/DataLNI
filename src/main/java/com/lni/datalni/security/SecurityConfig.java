package com.lni.datalni.security;

import com.lni.datalni.config.LdapProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.ArrayList;
import java.util.List;

/**
 * Single-file web security configuration.
 *
 * <p>The {@link AuthenticationManager} is a composite: whichever of the in-memory
 * {@link DaoAuthenticationProvider} ({@code dev} profile, see {@link DevSecurityConfig})
 * and {@link LdapAuthenticationProvider} (non-dev, when {@link LdapContextSource} is
 * present) are wired in. This lets a hardcoded admin coexist with LDAP if both beans are
 * exposed in the same profile, and keeps the filter chain identical across environments.
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(LdapProperties.class)
public class SecurityConfig {

    @Autowired(required = false)
    private DaoAuthenticationProvider inMemoryAuthenticationProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // ZK's AU engine and the plain login.zul form don't carry Spring CSRF
                // tokens; the app sits on the intranet so CSRF is off across the board.
                .csrf(csrf -> csrf.disable())
                // Same-origin frames (ZK pages load their own update endpoint via XHR
                // and occasionally iframes for downloads).
                .headers(h -> h.frameOptions(frame -> frame.sameOrigin()))
                .authorizeHttpRequests(auth -> auth
                        .antMatchers(
                                "/login.zul",
                                "/error",
                                "/zkau/**",
                                "/zkcomet/**",
                                "/css/**",
                                "/img/**",
                                "/js/**",
                                "/webjars/**",
                                "/favicon.ico"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login.zul")
                        .loginProcessingUrl("/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/index.zul", true)
                        .failureUrl("/login.zul?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login.zul?logout=true")
                        // Allow GET /logout so a plain anchor link can sign out.
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                )
                .sessionManagement(session -> session
                        .sessionFixation().changeSessionId()
                );

        return http.build();
    }

    /**
     * Only created when an {@link LdapContextSource} is on the context (i.e. non-dev
     * profile, see {@link com.lni.datalni.config.LdapConfig}). Default role comes from
     * {@code app.ldap.default-role} since the UEM directory has no LNI_* groups.
     */
    @Bean
    @ConditionalOnBean(LdapContextSource.class)
    public LdapAuthenticationProvider ldapAuthenticationProvider(LdapContextSource ldapContextSource,
                                                                 LdapProperties props) {
        FilterBasedLdapUserSearch userSearch = new FilterBasedLdapUserSearch(
                props.getUserSearchBase(), props.getUserSearchFilter(), ldapContextSource);

        BindAuthenticator authenticator = new BindAuthenticator(ldapContextSource);
        authenticator.setUserSearch(userSearch);

        DefaultLdapAuthoritiesPopulator authoritiesPopulator =
                new DefaultLdapAuthoritiesPopulator(ldapContextSource, "");
        authoritiesPopulator.setDefaultRole("ROLE_" + props.getDefaultRole());
        authoritiesPopulator.setIgnorePartialResultException(true);

        return new LdapAuthenticationProvider(authenticator, authoritiesPopulator);
    }

    @Bean
    public AuthenticationManager authenticationManager(
            @Autowired(required = false) LdapAuthenticationProvider ldapAuthenticationProvider) {
        List<AuthenticationProvider> providers = new ArrayList<>();
        if (inMemoryAuthenticationProvider != null) {
            providers.add(inMemoryAuthenticationProvider);
        }
        if (ldapAuthenticationProvider != null) {
            providers.add(ldapAuthenticationProvider);
        }
        return new ProviderManager(providers);
    }
}
