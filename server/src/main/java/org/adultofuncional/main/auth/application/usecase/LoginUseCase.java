package org.adultofuncional.main.auth.application.usecase;

import java.util.List;

import org.adultofuncional.main.account.domain.model.Account;
import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.auth.application.dto.AuthResponse;
import org.adultofuncional.main.auth.application.dto.LoginRequest;
import org.adultofuncional.main.config.security.JwtService;
import org.adultofuncional.main.shared.exception.UnauthorizedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/**
 * Caso de uso para la autenticación de usuarios registrados.
 *
 * <p>
 * Verifica las credenciales (email y contraseña) y, si son correctas,
 * genera un token JWT firmado que el cliente podrá usar en peticiones
 * posteriores. La respuesta incluye los datos públicos de la cuenta,
 * pero nunca expone el hash de la contraseña ni la master key.
 *
 * <p>
 * <strong>Seguridad:</strong> No se distingue entre email inexistente
 * y contraseña incorrecta; en ambos casos se lanza
 * {@link UnauthorizedException} con el mismo mensaje genérico.
 *
 * @author Juan Sebastian Rios
 * @since 0.0.1
 */
@Service
@RequiredArgsConstructor
public class LoginUseCase {

  private final AccountRepository accountRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  /**
   * Ejecuta el proceso de autenticación.
   *
   * @param request credenciales del usuario (email y contraseña en texto plano)
   * @return respuesta con el token JWT y los datos de la cuenta
   * @throws UnauthorizedException si el email no existe o la contraseña no
   *                               coincide
   */
  public AuthResponse execute(LoginRequest request) {
    // 1. Buscar cuenta y verificar contraseña (sin distinguir causa de fallo)
    Account account = accountRepository.findByEmail(request.getEmail())
        .filter(acc -> passwordEncoder.matches(request.getPassword(), acc.getPasswordHash()))
        .orElseThrow(() -> new UnauthorizedException("Email o contraseña incorrectos"));

    // 2. Generar token JWT
    String token = jwtService.generateToken(
        account.getId().toString(),
        account.getEmail(),
        List.of(new SimpleGrantedAuthority("ROLE_USER")));

    // 3. Construir respuesta sin datos sensibles
    return AuthResponse.builder()
        .token(token)
        .expiresIn(jwtService.getExpiration())
        .accountId(account.getId())
        .email(account.getEmail())
        .names(account.getNames())
        .lastnames(account.getLastnames())
        .phone(account.getPhone())
        .createdAt(account.getCreatedAt())
        .hasMasterKey(account.getMasterKeyHash() != null) // TODO: ajustar cuando el módulo de seguridad esté listo
        .build();
  }
}
