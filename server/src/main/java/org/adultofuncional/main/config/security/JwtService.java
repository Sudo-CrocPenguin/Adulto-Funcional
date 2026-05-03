package org.adultofuncional.main.config.security;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class JwtService {

  private final SecretKey secretKey;
  private final long expiration;

  public JwtService(
      @Value("${JWT_SECRET}") String secret,
      @Value("${JWT_EXPIRATION}") long expiration) {

    if (secret == null || secret.length() < 32) {
      throw new IllegalStateException(
          "JWT_SECRET debe tener mínimo 32 caracteres. Longitud actual: "
              + (secret == null ? 0 : secret.length()));
    }

    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.expiration = expiration;
  }

  public String generateToken(String accountId, String email, Collection<? extends GrantedAuthority> authorities) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + expiration);

    List<String> roles = authorities.stream()
        .map(GrantedAuthority::getAuthority)
        .toList();

    return Jwts.builder()
        .subject(accountId)
        .claim("email", email)
        .claim("roles", roles) // <-- nuevo claim
        .issuedAt(now)
        .expiration(expiryDate)
        .signWith(secretKey)
        .compact();
  }

  public UUID extractAccountId(String token) {
    return UUID.fromString(extractClaims(token).getSubject());
  }

  public String extractEmail(String token) {
    return extractClaims(token).get("email", String.class);
  }

  public List<String> extractRoles(String token) {
    return extractClaims(token).get("roles", List.class);
  }

  public long getExpiration() {
    return expiration;
  }

  private Claims extractClaims(String token) {
    return Jwts.parser()
        .verifyWith(secretKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  // Método paquete-interno para que JwtAuthenticationFilter lo use
  Claims parseAndValidate(String token) {
    return extractClaims(token);
  }
}
