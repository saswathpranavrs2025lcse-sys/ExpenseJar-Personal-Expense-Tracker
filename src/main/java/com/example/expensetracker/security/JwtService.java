package com.example.expensetracker.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    @Value("${app.jwt.secret:PersonalExpenseTrackerSecretKey2026SaaSSecureAuthenticationKeyMustBe32BytesLong}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms:86400000}") // 24 hours
    private long jwtExpirationMs;

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(UserPrincipal principal, boolean rememberMe) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", principal.getId());
        extraClaims.put("userIdCode", principal.getUserIdCode());
        extraClaims.put("name", principal.getName());

        long expiration = rememberMe ? (jwtExpirationMs * 30) : jwtExpirationMs; // 30 days if remember me
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
            .claims(extraClaims)
            .subject(principal.getEmail())
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(getSigningKey())
            .compact();
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long extractUserId(String token) {
        Object val = extractClaim(token, claims -> claims.get("userId"));
        if (val instanceof Number) {
            return ((Number) val).longValue();
        }
        return null;
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claims != null ? claimsResolver.apply(claims) : null;
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        } catch (Exception ex) {
            log.debug("Invalid JWT token: {}", ex.getMessage());
            return null;
        }
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        if (isTokenExpired(token)) return false;
        if (userDetails instanceof UserPrincipal principal) {
            Long tokenUserId = extractUserId(token);
            if (tokenUserId != null && tokenUserId.equals(principal.getId())) {
                return true;
            }
        }
        final String email = extractEmail(token);
        return (email != null && email.equalsIgnoreCase(userDetails.getUsername()));
    }

    private boolean isTokenExpired(String token) {
        Date expiration = extractClaim(token, Claims::getExpiration);
        return expiration == null || expiration.before(new Date());
    }
}
