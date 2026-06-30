package com.attendance.security;

import com.attendance.entity.User;
import com.attendance.exception.UnauthorizedException;
import com.attendance.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
/**
 * Once-per-request filter that extracts and validates the Bearer JWT.
 * <p>On each authenticated request: parses the JWT, checks the token blacklist,
 * verifies account state (locked/disabled), and sets the Spring Security context
 * with the user's role as a granted authority. Returns 401 immediately on any failure.
 * Layer: security.</p>
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final TokenBlacklistService tokenBlacklist;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // ponytail: early return on writeUnauthorized avoids polluting the filter chain
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                Jws<Claims> jws = tokenProvider.parse(token);
                Claims claims = jws.getBody();
                String jti = claims.getId();
                if (jti != null && tokenBlacklist.isBlacklisted(jti)) {
                    writeUnauthorized(response, "Token is blacklisted");
                    return;
                }
                String email = claims.getSubject();
                User user = userRepository.findByEmail(email)
                        .orElse(null);
                if (user == null) {
                    writeUnauthorized(response, "User not found");
                    return;
                }
                if (user.isLocked()) {
                    writeUnauthorized(response, "Account is locked");
                    return;
                }
                if (!user.isEnabled()) {
                    writeUnauthorized(response, "Account is disabled");
                    return;
                }
                String role = claims.get("role", String.class);
                List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(user, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                writeUnauthorized(response, "Invalid or expired JWT");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"" + message + "\"}");
    }
}
