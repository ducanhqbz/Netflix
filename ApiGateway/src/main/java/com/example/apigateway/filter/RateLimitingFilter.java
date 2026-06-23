package com.example.apigateway.filter;

import com.example.apigateway.service.RateLimitingService;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Filter for implementing API rate limiting using Redis backend
 */
@Component
public class RateLimitingFilter implements Filter {

    private final RateLimitingService rateLimitingService;

    @Value("${rate-limiting.enabled:true}")
    private boolean rateLimitingEnabled;

    @Value("${rate-limiting.exclude-paths:/api/auth/login,/api/auth/register}")
    private String excludePathsStr;

    private static final String RATE_LIMIT_HEADER = "X-RateLimit-Remaining";
    private static final String RATE_LIMIT_RESET = "X-RateLimit-Reset";

    public RateLimitingFilter(RateLimitingService rateLimitingService) {
        this.rateLimitingService = rateLimitingService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String requestPath = httpRequest.getRequestURI();

        // Check if rate limiting is enabled and if the path is not excluded
        if (rateLimitingEnabled && !isExcludedPath(requestPath)) {
            // Get the client identifier (IP address or user ID)
            String clientIdentifier = getClientIdentifier(httpRequest);

            // Check rate limit
            if (!rateLimitingService.isRequestAllowed(clientIdentifier)) {
                // Rate limit exceeded
                httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                httpResponse.setHeader("Content-Type", "application/json");
                httpResponse.getWriter().write(
                    "{\"error\": \"Rate limit exceeded. Maximum 100 requests per minute allowed\"}"
                );
                return;
            }

            long remaining = rateLimitingService.getRemainingRequests(clientIdentifier);
            httpResponse.setHeader(RATE_LIMIT_HEADER, String.valueOf(remaining));
            httpResponse.setHeader(RATE_LIMIT_RESET, String.valueOf(System.currentTimeMillis() + 60000));
        }

        chain.doFilter(request, response);
    }

    /**
     * Check if the request path is excluded from rate limiting
     * 
     * @param requestPath the request path
     * @return true if the path should be excluded from rate limiting
     */
    private boolean isExcludedPath(String requestPath) {
        List<String> excludedPaths = Arrays.asList(excludePathsStr.split(","));
        return excludedPaths.stream()
                .anyMatch(path -> requestPath.startsWith(path.trim()));
    }

    /**
     * Get unique client identifier for rate limiting
     * Priority: User ID from header > Authorization header > Client IP
     * 
     * @param request the HTTP request
     * @return unique client identifier
     */
    private String getClientIdentifier(HttpServletRequest request) {
        // Check if user ID is already set by JWT filter
        String userId = request.getHeader("X-User-Name");
        if (userId != null && !userId.isEmpty()) {
            return "user:" + userId;
        }

        // Fall back to IP address
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getRemoteAddr();
        }
        return "ip:" + clientIp;
    }
}

