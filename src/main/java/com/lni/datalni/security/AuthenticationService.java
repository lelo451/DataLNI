package com.lni.datalni.security;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Programmatic login for the desktop app — there is no servlet filter chain. The login
 * dialog calls {@link #login}; on success the {@link Authentication} is published to both
 * the thread-bound {@link SecurityContextHolder} and the session-wide {@link CurrentUser}.
 */
@Service
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final CurrentUser currentUser;

    public AuthenticationService(AuthenticationManager authenticationManager, CurrentUser currentUser) {
        this.authenticationManager = authenticationManager;
        this.currentUser = currentUser;
    }

    /**
     * @throws AuthenticationException if the credentials are rejected.
     */
    public void login(String username, char[] password) throws AuthenticationException {
        Authentication request = new UsernamePasswordAuthenticationToken(
                username, new String(password));
        Authentication result = authenticationManager.authenticate(request);
        SecurityContextHolder.getContext().setAuthentication(result);
        currentUser.set(result);
    }

    public void logout() {
        SecurityContextHolder.clearContext();
        currentUser.clear();
    }
}
