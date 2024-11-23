package ru.alexander.GameServer.configs.auth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class HeaderAuthProvider {

    private final AuthenticationManager authenticationManager;

    public HeaderAuthProvider(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public void auth(String authHeader) {
        String[] s = authHeader.split(" ");
        if (s[0].equals("Basic")) {
            String data = new String(Base64.getDecoder().decode(s[1]), StandardCharsets.UTF_8);
            String[] credentials = data.split(":");
            Authentication authRequest = UsernamePasswordAuthenticationToken.unauthenticated(credentials[0], credentials[1]);
            Authentication authResponse = authenticationManager.authenticate(authRequest);
            if (authResponse.isAuthenticated())
                SecurityContextHolder.getContext().setAuthentication(authResponse);
        }
        // TODO: JWT Credential (Bearer/OAuth)
    }
}
