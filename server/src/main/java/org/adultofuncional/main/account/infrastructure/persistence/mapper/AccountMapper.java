package org.adultofuncional.main.account.infrastructure.persistence.mapper;

import org.adultofuncional.main.account.domain.model.Account;
import org.adultofuncional.main.account.application.dto.AccountResponse;
import org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity;
import org.springframework.stereotype.Component;

/**
 * Componente que convierte entre las distintas representaciones de una cuenta.
 *
 * <p>
 * Traduce entre:
 * <ul>
 * <li>{@link AccountEntity} (JPA) ↔ {@link Account} (dominio)</li>
 * <li>{@link Account} (dominio) → {@link AccountResponse} (DTO de
 * respuesta)</li>
 * </ul>
 *
 * <p>
 * Las columnas {@code account_password} y {@code account_master_key} nunca
 * se exponen en las respuestas hacia el cliente.
 *
 * @author Lydis Ester Jaraba
 * @since 0.0.1
 */
@Component
public class AccountMapper {

  /**
   * Convierte una {@link AccountEntity} al modelo de dominio {@link Account}.
   *
   * <p>
   * Usa el método de fábrica {@code Account.reconstitute()} para respetar
   * el constructor privado del modelo de dominio.
   *
   * @param entity entidad JPA; si es {@code null} retorna {@code null}
   * @return modelo de dominio reconstituido o {@code null}
   */
  public Account toDomain(AccountEntity entity) {
    if (entity == null)
      return null;

    return Account.reconstitute(
        entity.getAccountId(),
        entity.getAccountNames(),
        entity.getAccountLastNames(),
        entity.getAccountEmail(),
        entity.getAccountPhone(),
        entity.getAccountCreatedAt(),
        entity.getAccountPassword(),
        entity.getAccountMasterKey());
  }

  /**
   * Convierte el modelo de dominio {@link Account} a {@link AccountEntity}.
   *
   * <p>
   * Copia {@code account_password} y {@code account_master_key} desde el
   * modelo de dominio. Ambos se almacenan como hash Argon2.
   *
   * @param account modelo de dominio. Si es {@code null} retorna {@code null}.
   * @return entidad JPA lista para persistir.
   */
  public AccountEntity toEntity(Account account) {
    if (account == null)
      return null;
    AccountEntity entity = new AccountEntity();
    // Solo asignar el ID si no es null (cuenta existente)
    entity.setAccountId(account.getId()); // El ID siempre existe en el dominio (generado o reconstituido)
    entity.setAccountNames(account.getNames());
    entity.setAccountLastNames(account.getLastnames());
    entity.setAccountEmail(account.getEmail());
    entity.setAccountPhone(account.getPhone());
    entity.setAccountCreatedAt(account.getCreatedAt());
    entity.setAccountPassword(account.getPasswordHash());
    entity.setAccountMasterKey(account.getMasterKeyHash());
    return entity;
  }

  /**
   * Convierte el modelo de dominio {@link Account} al DTO
   * {@link AccountResponse}.
   *
   * <p>
   * No expone {@code password} ni {@code masterKey} al cliente.
   *
   * @param account modelo de dominio. Si es {@code null} retorna {@code null}.
   * @return DTO listo para serializar y enviar al cliente.
   */
  public AccountResponse toResponse(Account account) {
    if (account == null)
      return null;

    return AccountResponse.builder()
        .id(account.getId())
        .names(account.getNames())
        .lastnames(account.getLastnames())
        .email(account.getEmail())
        .phone(account.getPhone())
        .createdAt(account.getCreatedAt())
        .build();
  }
}
