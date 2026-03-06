package com.edutech.edutechbackend.util;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component   // tells Spring to manage this class as a bean (so we can @Autowired it anywhere)
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;             // pulled from application.yml

    @Value("${jwt.expiration}")
    private long expiration;           // pulled from application.yml (86400000 = 24hrs)

    // ─── builds the signing key from our secret string ───────────────────────
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // ─── GENERATE token ──────────────────────────────────────────────────────
    // called after successful login/register
    public String generateToken(String email) {
        return Jwts.builder()
                .subject(email)                          // who this token belongs to
                .issuedAt(new Date())                    // when it was created
                .expiration(new Date(System.currentTimeMillis() + expiration)) // when it expires
                .signWith(getSigningKey())               // sign it with our secret
                .compact();                              // build the final token string
    }

    // ─── EXTRACT email from token ─────────────────────────────────────────────
    // called on every protected request to know who is making the request
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    // ─── VALIDATE token ───────────────────────────────────────────────────────
    // checks: is the email correct? is the token expired?
    public boolean validateToken(String token, String email) {
        final String extractedEmail = extractEmail(token);
        return (extractedEmail.equals(email) && !isTokenExpired(token));
    }

    // ─── CHECK if token is expired ────────────────────────────────────────────
    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    // ─── EXTRACT all claims (payload data) from token ─────────────────────────
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())   // verify the signature using our secret
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}