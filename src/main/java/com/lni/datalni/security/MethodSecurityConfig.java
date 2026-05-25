package com.lni.datalni.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Enables {@code @PreAuthorize} on the service layer. The service is the source of truth
 * for authorization; the UI additionally hides/disables controls for UX.
 */
@Configuration
@EnableMethodSecurity
public class MethodSecurityConfig {
}
