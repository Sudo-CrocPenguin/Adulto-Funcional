package org.adultofuncional.main.security.application.usecase;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.security.application.dto.PasswordResponse;
import org.adultofuncional.main.security.domain.repository.PasswordRepository;
import org.adultofuncional.main.security.domain.service.MasterKeySessionService;
import org.adultofuncional.main.shared.exception.ForbiddenException;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * Caso de uso: Listar todas las credenciales almacenadas de una cuenta.
 *
 * <p>
 * Recupera las credenciales asociadas a la cuenta, verificando que la cuenta
 * exista y que la Master Key esté verificada. No se incluye la contraseña en
 * texto plano ni material criptográfico en la respuesta.
 *
 * <p>
 * <strong>Reglas de negocio:</strong>
 * <ul>
 * <li>La cuenta debe existir.</li>
 * <li>La Master Key debe estar verificada en la sesión.</li>
 * <li>Solo se retornan datos no sensibles (nombre de aplicación, fecha de
 * último cambio).</li>
 * </ul>
 *
 * @author Miguel Angel Blandon Montes, Juan Sebastian Rios
 * @since 0.0.1
 * @see PasswordRepository
 * @see AccountRepository
 * @see MasterKeySessionService
 * @see PasswordResponse
 */
@Service
@RequiredArgsConstructor
public class ListPasswordsUseCase {

  private final PasswordRepository passwordRepository;
  private final AccountRepository accountRepository;
  private final MasterKeySessionService masterKeyService;

  /**
   * Ejecuta el listado de credenciales.
   *
   * @param accountId Identificador de la cuenta propietaria.
   * @return Lista de {@link PasswordResponse} con los datos no sensibles.
   * @throws NotFoundException  si la cuenta no existe.
   * @throws ForbiddenException si la Master Key no está verificada.
   */
  @Transactional(readOnly = true)
  public List<PasswordResponse> execute(UUID accountId) {
    accountRepository.findById(accountId)
        .orElseThrow(() -> new NotFoundException("Cuenta no encontrada con id: " + accountId));

    if (!masterKeyService.isVerified(accountId)) {
      throw new ForbiddenException("Master Key no verificada");
    }

    return passwordRepository.findAllByAccountId(accountId).stream()
        .map(password -> PasswordResponse.builder()
            .id(password.getId())
            .applicationName(password.getApplicationName())
            .lastChangeDate(password.getLastChangeDate())
            .build())
        .collect(Collectors.toList());
  }
}
