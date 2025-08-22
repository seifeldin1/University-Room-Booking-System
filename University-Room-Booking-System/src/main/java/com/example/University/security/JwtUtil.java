//package com.example.University.security;
//
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.SignatureAlgorithm;
//import io.jsonwebtoken.security.Keys;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.stereotype.Component;
//
//import java.security.Key;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.function.Function;
//
//@Component
//@Slf4j
//public class JwtUtil {
//
//    @Value("${spring.security.jwt.secret}")
//    private String secret;
//
//    @Value("${spring.security.jwt.expiration}")
//    private Long expiration;
//
//    private Key getSigningKey() {
//        byte[] keyBytes = secret.getBytes();
//        return Keys.hmacShaKeyFor(keyBytes);
//    }
//
//    public String generateToken(UserDetails userDetails) {
//        Map<String, Object> claims = new HashMap<>();
//        return createToken(claims, userDetails.getUsername());
//    }
//
//    public String generateTokenWithClaims(UserDetails userDetails, Map<String, Object> extraClaims) {
//        Map<String, Object> claims = new HashMap<>(extraClaims);
//        return createToken(claims, userDetails.getUsername());
//    }
//
//    private String createToken(Map<String, Object> claims, String subject) {
//        Date now = new Date();
//        Date expiryDate = new Date(now.getTime() + expiration);
//
//        return Jwts.builder()
//                .setClaims(claims)
//                .setSubject(subject)
//                .setIssuedAt(now)
//                .setExpiration(expiryDate)
//                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
//                .compact();
//    }
//
//    public String extractUsername(String token) {
//        return extractClaim(token, Claims::getSubject);
//    }
//
//    public Date extractExpiration(String token) {
//        return extractClaim(token, Claims::getExpiration);
//    }
//
//    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
//        final Claims claims = extractAllClaims(token);
//        return claimsResolver.apply(claims);
//    }
//
//    private Claims extractAllClaims(String token) {
//        try {
//            return Jwts.parserBuilder()
//                    .setSigningKey(getSigningKey())
//                    .build()
//                    .parseClaimsJws(token)
//                    .getBody();
//        } catch (Exception e) {
//            log.error("Error extracting claims from token: {}", e.getMessage());
//            throw e;
//        }
//    }
//
//    public Boolean isTokenExpired(String token) {
//        try {
//            return extractExpiration(token).before(new Date());
//        } catch (Exception e) {
//            log.warn("Token validation failed: {}", e.getMessage());
//            return true;
//        }
//    }
//
//    public Boolean validateToken(String token, UserDetails userDetails) {
//        try {
//            final String username = extractUsername(token);
//            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
//        } catch (Exception e) {
//            log.warn("Token validation failed for user: {}", e.getMessage());
//            return false;
//        }
//    }
//
//    public Boolean validateToken(String token) {
//        try {
//            extractAllClaims(token);
//            return !isTokenExpired(token);
//        } catch (Exception e) {
//            log.warn("Token validation failed: {}", e.getMessage());
//            return false;
//        }
//    }
//}