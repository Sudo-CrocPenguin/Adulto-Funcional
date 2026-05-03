package org.adultofuncional.main.auth.application.usecase;

import java.util.List;

import org.adultofuncional.main.account.domain.model.Account;
import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.auth.application.dto.AuthResponse;
import org.adultofuncional.main.auth.application.dto.LoginRequest;
import org.adultofuncional.main.config.security.JwtService;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.adultofuncional.main.shared.exception.UnauthorizedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/**
 * Caso de uso para autenticar un usuario existente.
 *
 * <p>
 * Verifica las credenciales del usuario mediante email y contraseña,
 * y genera un token JWT si la autenticación es exitosa.
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

  public AuthResponse execute(LoginRequest request) {

    // 1. Buscar cuenta por email
    Account account = accountRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new NotFoundException("Cuenta no encontrada"));

    // 2. Verificar contraseña
    if (!passwordEncoder.matches(request.getPassword(), account.getPasswordHash())) {
      throw new UnauthorizedException("Credenciales incorrectas");
    }

    // 3. Generar token
    String token = jwtService.generateToken(
        account.getId().toString(),
        account.getEmail(), List.of(new SimpleGrantedAuthority("ROLE_USER")));

    // 4. Retornar respuesta
    return AuthResponse.builder()
        .token(token)
        .expiresIn(jwtService.getExpiration())
        .accountId(account.getId())
        .email(account.getEmail())
        .names(account.getNames())
        .lastnames(account.getLastnames())
        .phone(account.getPhone())
        .createdAt(account.getCreatedAt())
        .hasMasterKey(account.getMasterKeyHash() != null) // TODO: cuando se implemente el módulo de seguridad
        .build();
  }
}
