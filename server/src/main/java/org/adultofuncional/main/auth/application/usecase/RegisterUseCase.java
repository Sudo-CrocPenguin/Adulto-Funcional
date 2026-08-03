package org.adultofuncional.main.auth.application.usecase;

import java.time.Clock;

import org.adultofuncional.main.account.domain.model.Account;
import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.auth.application.dto.AuthResponse;
import org.adultofuncional.main.auth.application.dto.RegisterRequest;
import org.adultofuncional.main.auth.application.dto.SessionTokens;
import org.adultofuncional.main.auth.application.service.AuthenticationSessionService;
import org.adultofuncional.main.auth.domain.model.AccountRole;
import org.adultofuncional.main.auth.domain.repository.AccountRoleRepository;
import org.adultofuncional.main.shared.exception.ConflictException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * Caso de uso para registrar un nuevo usuario en el sistema.
 *
 * <p>
 * Orquesta el flujo completo de registro: valida la unicidad del correo,
 * hashea las credenciales con Argon2, crea la cuenta en el dominio y
 * persiste la entidad. Finalmente, genera un token JWT para que el
 * usuario quede autenticado de inmediato.
 *
 * <p>
 * La Master Key es opcional; si se proporciona, se hashea y se
 * almacena junto con la cuenta.
 *
 * @author Lydis Ester Jaraba
 * @since 0.0.1
 */
@Service
@RequiredArgsConstructor
public class RegisterUseCase {

  private final AccountRepository accountRepository;
  private final AccountRoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationSessionService sessionService;
  private final Clock clock;

  /**
   * Ejecuta el registro de un nuevo usuario.
   *
   * @param request DTO con los datos del nuevo usuario (nombres, apellidos,
   *                teléfono, email, contraseña y, opcionalmente, Master Key)
   * @return {@link AuthResponse} con el token JWT y los datos de la cuenta creada
   * @throws ConflictException si el correo electrónico ya está registrado
   */
  @Transactional
  public AuthResponse execute(RegisterRequest request) {
    // 1. Verificar unicidad del email
    accountRepository.findByEmail(request.getEmail())
        .ifPresent(existing -> {
          throw new ConflictException("El correo ya está registrado");
        });

    // 2. Hashear contraseña (y Master Key opcional)
    String hashedPassword = passwordEncoder.encode(request.getPassword());
    String hashedMasterKey = (request.getMasterKey() != null && !request.getMasterKey().isBlank())
        ? passwordEncoder.encode(request.getMasterKey())
        : null;

    // 3. Crear modelo de dominio (genera UUID v7 y createdAt)
    Account account = Account.create(
        request.getNames(),
        request.getLastnames(),
        request.getEmail(),
        request.getPhone(),
        hashedPassword,
        hashedMasterKey,
        clock);

    // 4. Persistir
    Account savedAccount = accountRepository.save(account);
    roleRepository.grant(savedAccount.getId(), AccountRole.USER);

    SessionTokens tokens = sessionService.create(savedAccount);
    return AuthResponse.from(savedAccount, tokens, clock.instant());
  }
}
