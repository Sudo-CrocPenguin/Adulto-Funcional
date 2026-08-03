package org.adultofuncional.main.auth.application.usecase;

import java.time.Clock;
import java.util.Optional;

import org.adultofuncional.main.account.domain.model.Account;
import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.auth.application.dto.AuthResponse;
import org.adultofuncional.main.auth.application.dto.LoginRequest;
import org.adultofuncional.main.auth.application.dto.SessionTokens;
import org.adultofuncional.main.auth.application.service.AuthenticationSessionService;
import org.adultofuncional.main.shared.exception.UnauthorizedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
public class LoginUseCase {

  private final AccountRepository accountRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationSessionService sessionService;
  private final Clock clock;
  private final String dummyPasswordHash;

  /**
   * Calcula una única credencial ficticia para igualar el coste de emails
   * existentes e inexistentes sin ejecutar Argon2 dos veces por solicitud.
   */
  public LoginUseCase(
      AccountRepository accountRepository,
      PasswordEncoder passwordEncoder,
      AuthenticationSessionService sessionService,
      Clock clock) {
    this.accountRepository = accountRepository;
    this.passwordEncoder = passwordEncoder;
    this.sessionService = sessionService;
    this.clock = clock;
    this.dummyPasswordHash = passwordEncoder.encode(
        "dummy-password-used-only-to-equalize-login-timing");
  }

  /**
   * Ejecuta el proceso de autenticación.
   *
   * @param request credenciales del usuario (email y contraseña en texto plano)
   * @return respuesta con el token JWT y los datos de la cuenta
   * @throws UnauthorizedException si el email no existe o la contraseña no
   *                               coincide
   */
  public AuthResponse execute(LoginRequest request) {
    Optional<Account> candidate = accountRepository.findByEmail(request.getEmail());
    String passwordHash = candidate.map(Account::getPasswordHash).orElse(dummyPasswordHash);
    boolean passwordMatches = passwordEncoder.matches(request.getPassword(), passwordHash);
    if (candidate.isEmpty() || !passwordMatches) {
      throw new UnauthorizedException("Email o contraseña incorrectos");
    }

    Account account = candidate.orElseThrow();
    SessionTokens tokens = sessionService.create(account);
    return AuthResponse.from(account, tokens, clock.instant());
  }
}
