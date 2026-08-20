package com.example.apigateway.filter;

import com.example.apigateway.service.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;
import java.security.Key;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtGlobalFilter implements Filter {
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    @Value("${security.jwt.secret-key}")
    private String secretKey;

    private final JwtTokenProvider jwtTokenProvider;

    public JwtGlobalFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    // List of public paths that don't require authentication
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/hello",
            "/home/movies"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String requestPath = httpRequest.getRequestURI();

        // Allow public paths without authentication
        if (PUBLIC_PATHS.stream().anyMatch(requestPath::startsWith)) {
            chain.doFilter(request, response);
            return;
        }

        try {
            String authHeader = httpRequest.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
                httpResponse.getWriter().write("{\"error\": \"Missing or invalid token\"}");
                return;
            }

            String token = authHeader.substring(7);

            // Validate token
            if (!jwtTokenProvider.isTokenValid(token) || jwtTokenProvider.isTokenLoggedOut(token)) {
                httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
                httpResponse.getWriter().write("{\"error\": \"Invalid or logged out token\"}");
                return;
            }

            // Extract claims from token
            Key signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // Extract username and authorities
            String username = claims.getSubject();
            List<String> authorities = (List<String>) claims.get("authorities");

            if (authorities == null) {
                authorities = Arrays.asList(); // Empty list if no authorities
            }

            if (!isAuthorizedForApi(httpRequest, authorities)) {
                httpResponse.setStatus(HttpStatus.FORBIDDEN.value());
                httpResponse.getWriter().write("{\"error\": \"Insufficient permission\"}");
                return;
            }

            // Convert to GrantedAuthority
            List<GrantedAuthority> grantedAuthorities = authorities.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            // Create wrapped request to add custom header
            HeaderMapRequestWrapper wrappedRequest = new HeaderMapRequestWrapper(httpRequest);
            wrappedRequest.addHeader("X-User-Name", username);

            // Set authentication in security context
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(username, null, grantedAuthorities);
            SecurityContextHolder.getContext().setAuthentication(authToken);

            chain.doFilter(wrappedRequest, response);
        } catch (Exception e) {
            httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
            String errorMsg = "Unauthorized: " + (e.getMessage() != null ? e.getMessage() : "Invalid token");
            httpResponse.getWriter().write("{\"error\": \"" + errorMsg + "\"}");
            e.printStackTrace();
        }
    }

    private boolean isAuthorizedForApi(HttpServletRequest request, List<String> authorities) {
        String path = request.getRequestURI();

        // AuthService applies its own role rules. A valid JWT is sufficient to reach it.
        if (path.startsWith("/api/auth/")) {
            return true;
        }

        // ADMIN can access every API; other roles need a matching API permission from the JWT.
        if (authorities.contains("ROLE_ADMIN")) {
            return true;
        }

        return authorities.stream()
                .filter(authority -> authority.startsWith("API:"))
                .map(authority -> authority.split(":", 3))
                .anyMatch(parts -> parts.length == 3
                        && parts[1].equalsIgnoreCase(request.getMethod())
                        && PATH_MATCHER.match(parts[2], path));
    }
}
