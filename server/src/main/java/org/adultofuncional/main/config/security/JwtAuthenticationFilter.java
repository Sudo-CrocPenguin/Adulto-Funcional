package org.adultofuncional.main.config.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain)
      throws ServletException, IOException {

    final String authHeader = request.getHeader("Authorization");

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    final String jwt = authHeader.substring(7);

    try {
      Claims claims = jwtService.parseAndValidate(jwt);
      String userEmail = claims.get("email", String.class);

      if (userEmail != null &&
          SecurityContextHolder.getContext().getAuthentication() == null) {

        List<String> roles = claims.get("roles", List.class);
        List<SimpleGrantedAuthority> authorities = roles == null
            ? List.of(new SimpleGrantedAuthority("ROLE_USER"))
            : roles.stream().map(SimpleGrantedAuthority::new).toList();

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
            userEmail,
            null,
            authorities);

        authToken.setDetails(
            new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authToken);
      }

    } catch (ExpiredJwtException e) {
      log.debug("Token JWT expirado para request {}: {}", request.getRequestURI(), e.getMessage());
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token JWT expirado");
      return;
    } catch (SignatureException e) {
      log.warn("Firma JWT inválida en request {}: {}", request.getRequestURI(), e.getMessage());
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Firma JWT inválida");
      return;
    } catch (JwtException | IllegalArgumentException e) {
      log.warn("Token JWT inválido en request {}: {}", request.getRequestURI(), e.getMessage());
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token JWT inválido");
      return;
    }

    filterChain.doFilter(request, response);
  }
}
