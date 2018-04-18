package edu.pw.eiti.pik.config.security;

import io.jsonwebtoken.Jwts;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Optional;

public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    private static final String AUTHORIZATION_SCHEMA = "bearer ";

    private final byte[] secretKey;

    JwtAuthorizationFilter(
            final AuthenticationManager authenticationManager, String secretKey) {
        super(authenticationManager);
        this.secretKey = secretKey.getBytes(Charset.forName("UTF-8"));
    }

    @Override
    protected void doFilterInternal(
            final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain)
            throws IOException, ServletException {
        Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION))
                .ifPresent(this::handleAuthorizationHeader);
        chain.doFilter(request, response);
    }

    private void handleAuthorizationHeader(final String header) {
        if (!header.toLowerCase().startsWith(AUTHORIZATION_SCHEMA)) {
            return;
        }

        try {
            final String subject =
                    Jwts.parser()
                            .setSigningKey(secretKey)
                            .parseClaimsJws(header.split(" ")[1])
                            .getBody()
                            .getSubject();

            if (null != subject) {
                SecurityContextHolder.getContext()
                        .setAuthentication(
                                new UsernamePasswordAuthenticationToken(subject, null, Collections.emptyList()));
            }
        } catch (final Exception e) {
            // Ignore invalid JWT
        }
    }
}
