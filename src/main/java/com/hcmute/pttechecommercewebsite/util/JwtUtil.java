package com.hcmute.pttechecommercewebsite.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {

    private final SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS512);
    private final long EXPIRATION_TIME = 3600000;
    private final long REFRESH_TOKEN_EXPIRATION_TIME = 604800000; // 7 days

    // Generate token with roles for a user
    public String generateToken(String userId, String username, List<String> roles) {
        return Jwts.builder()
                .setSubject(username)
                .claim("id", userId)
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS512)
                .compact();
    }

    // Generate Refresh Token
    public String generateRefreshToken(String userId, String username) {
        return Jwts.builder()
                .setSubject(username)
                .claim("id", userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION_TIME))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS512)
                .compact();
    }

    // Extract username from token
    public String extractUsername(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (JwtException e) {
            return null;
        }
    }

    // Extract userId from token
    public String extractUserId(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("id", String.class);
        } catch (JwtException e) {
            return null; // Token không hợp lệ
        }
    }

    // Extract roles from token
    public List<String> extractRoles(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("roles", List.class); // Lấy danh sách vai trò từ token
        } catch (JwtException e) {
            return null; // Token không hợp lệ
        }
    }

    // Check if the token is valid
    public boolean isTokenValid(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return !isTokenExpired(claims);
        } catch (JwtException e) {
            return false;
        }
    }

    // Check if the token is expired
    private boolean isTokenExpired(Claims claims) {
        Date expiration = claims.getExpiration();
        return expiration.before(new Date());
    }
}