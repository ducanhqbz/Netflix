package com.example.authservice.Service;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JWTService {

    private static final String LOGGED_OUT_TOKEN_PREFIX = "auth:logged-out-token:";

    private final StringRedisTemplate redisTemplate;

    public JWTService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value("${security.jwt.expiration-ms:3600000}")
    private long jwtExpirationMs;

    public String generateToken(UserDetails userDetails) {
        // Add authorities to extra claims
        Map<String, Object> extraClaims = Map.of(
                "authorities", userDetails.getAuthorities().stream()
                        .map(auth -> auth.getAuthority())
                        .collect(Collectors.toList())
        );
        return generateToken(extraClaims, userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getSignInKey())
                .compact();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String username = extractEmail(token);
            return username.equals(userDetails.getUsername())
                    && !isTokenExpired(token)
                    && !isTokenLoggedOut(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public void logout(String token) {
        long remainingLifetime = extractExpiration(token).getTime() - System.currentTimeMillis();
        if (remainingLifetime > 0) {
                redisTemplate.opsForValue().set(
                        LOGGED_OUT_TOKEN_PREFIX + tokenHash(token),
                        "true",
                        remainingLifetime,
                        TimeUnit.MILLISECONDS
                );
        }
    }

    private boolean isTokenLoggedOut(String token) {
        return Boolean.TRUE.equals(
                redisTemplate.hasKey(LOGGED_OUT_TOKEN_PREFIX + tokenHash(token))
        );
    }

    private boolean isTokenExpired(String token) {
        Date exp = extractExpiration(token);
        return exp.before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract authorities/roles from token
     */
    @SuppressWarnings("unchecked")
    public List<String> extractAuthorities(String token) {
        Claims claims = extractAllClaims(token);
        return (List<String>) claims.get("authorities");
    }

    private String tokenHash(String token) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (java.security.NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
