package org.adultofuncional.main.security.application.usecase;

import java.util.Set;
import java.util.UUID;

import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.security.application.dto.PasswordFilterRequest;
import org.adultofuncional.main.security.application.dto.PasswordResponse;
import org.adultofuncional.main.security.domain.repository.PasswordRepository;
import org.adultofuncional.main.security.domain.service.MasterKeySessionService;
import org.adultofuncional.main.shared.exception.ForbiddenException;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.adultofuncional.main.shared.pagination.PageQuery;
import org.adultofuncional.main.shared.pagination.PageResult;
import org.adultofuncional.main.shared.pagination.PaginationPolicy;
import org.adultofuncional.main.shared.response.ApiErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;

/**
 * Caso de uso: Listar una página de credenciales de una cuenta.
 *
 * <p>
 * Verifica cuenta y Master Key de la sesión, y después ejecuta ownership,
 * búsqueda, orden y límites en SQL. No incluye contraseña en texto plano ni
 * material criptográfico en la respuesta.
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

  private static final Set<String> ALLOWED_SORTS =
      Set.of("applicationName", "lastChangeDate", "id");

  private final PasswordRepository passwordRepository;
  private final AccountRepository accountRepository;
  private final MasterKeySessionService masterKeyService;

  /**
   * Ejecuta el listado de credenciales.
   *
   * @param accountId Identificador de la cuenta propietaria.
   * @return página de {@link PasswordResponse} con datos no sensibles.
   * @throws NotFoundException  si la cuenta no existe.
   * @throws ForbiddenException si la Master Key no está verificada.
   */
  @Transactional(readOnly = true)
  public PageResult<PasswordResponse> execute(
      UUID accountId,
      UUID sessionId,
      PasswordFilterRequest filter) {
    accountRepository.findById(accountId)
        .orElseThrow(() -> new NotFoundException("Cuenta no encontrada con id: " + accountId));

    if (masterKeyService.find(accountId, sessionId).isEmpty()) {
      throw new ForbiddenException(
          "Master Key no verificada",
          ApiErrorCode.MASTER_KEY_REQUIRED);
    }

    PageQuery pageQuery = PaginationPolicy.resolve(
        filter == null ? null : filter.getPage(),
        filter == null ? null : filter.getSize(),
        filter == null ? null : filter.getSortBy(),
        filter == null ? null : filter.getSortDirection(),
        "applicationName",
        true,
        ALLOWED_SORTS);
    return passwordRepository.findPageByAccountId(
            accountId,
            filter != null && StringUtils.hasText(filter.getSearchTerm())
                ? filter.getSearchTerm().trim()
                : null,
            pageQuery)
        .map(password -> PasswordResponse.builder()
            .id(password.getId())
            .applicationName(password.getApplicationName())
            .lastChangeDate(password.getLastChangeDate())
            .build());
  }
}
