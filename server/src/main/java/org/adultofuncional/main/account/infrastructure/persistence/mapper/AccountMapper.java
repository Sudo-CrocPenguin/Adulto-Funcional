package org.adultofuncional.main.account.infrastructure.persistence.mapper;

import org.adultofuncional.main.account.application.dto.AccountResponse;
import org.adultofuncional.main.account.domain.model.Account;

//por ahora en comentario hasta que estén disponibles
//import org.adultofuncional.main.account.domain.model.Account;
//import org.adultofuncional.main.account.application.dto.AccountResponse;

import org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity;
import org.springframework.stereotype.Component;

/**
 * Componente que convierte entre las distintas representaciones de una cuenta.
 *
 * <p>
 * Traduce entre:
 * <ul>
 * <li>{@link AccountEntity} (persistencia JPA) → modelo de dominio</li>
 * <li>modelo de dominio → DTO de respuesta para el frontend</li>
 * </ul>
 *
 * <p>
 * Las columnas {@code account_password} y {@code account_master_key} nunca
 * se exponen en las respuestas hacia el cliente.
 *
 * @author Lydis Jaraba
 * @since 0.0.1
 * @see AccountEntity
 */
@Component
public class AccountMapper {

  /**
   * Convierte una entidad JPA al modelo de dominio.
   *
   * @param entity entidad de persistencia
   * @return modelo de dominio, o {@code null} si la entidad es null
   */
  // TODO: reemplazar Object por Account cuando Account.java esté disponible
  public Account toDomain(AccountEntity entity) {
    if (entity == null)
      return null;

    Account account = new Account();
    account.setId(entity.getAccount_id());
    account.setNames(entity.getAccount_names());
    account.setLastnames(entity.getAccount_lastnames());
    account.setEmail(entity.getAccount_email());
    account.setPhone(entity.getAccount_phone());
    account.setCreatedAt(entity.getAccount_created_at());
    account.setHasMasterKey(entity.getAccount_master_key() != null);

    return account;

  }

  /**
   * Convierte el modelo de dominio al DTO de respuesta HTTP.
   *
   * <p>
   * No incluye {@code password} ni {@code master_key} en la respuesta.
   *
   * @param account modelo de dominio
   * @return DTO de respuesta, o {@code null} si account es null
   */
  // TODO: reemplazar ambos Object por Account y AccountResponse cuando los DTOs
  // estén disponibles
  public Account toResponse(Account account) {
    if (account == null)
      return null;

    AccountResponse response = new AccountResponse();
    response.setId(account.getId());
    response.setNames(account.getNames());
    response.setLastnames(account.getLastnames());
    response.setEmail(account.getEmail());
    response.setPhone(account.getPhone());
    response.setCreatedAt(account.getCreatedAt());
    response.setHasMasterKey(account.isHasMasterKey());

    return response;

  }
}
