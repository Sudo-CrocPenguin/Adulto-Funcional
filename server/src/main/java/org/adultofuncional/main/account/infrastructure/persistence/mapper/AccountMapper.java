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
 * @author Lydis Jaraba
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
   * @param entity entidad JPA. Si es {@code null} retorna {@code null}.
   * @return modelo de dominio reconstituido.
   */
  public Account toDomain(AccountEntity entity) {
    if (entity == null)
      return null;

    return Account.reconstitute(
        entity.getAccount_id(),
        entity.getAccount_names(),
        entity.getAccount_lastnames(),
        entity.getAccount_email(),
        entity.getAccount_phone(),
        entity.getAccount_created_at());
  }

  /**
   * Convierte el modelo de dominio {@link Account} a {@link AccountEntity}.
   *
   * <p>
   * No copia {@code account_password} ni {@code account_master_key} —
   * esos campos los gestiona exclusivamente la capa de seguridad.
   *
   * @param account modelo de dominio. Si es {@code null} retorna {@code null}.
   * @return entidad JPA lista para persistir.
   */
  public AccountEntity toEntity(Account account) {
    if (account == null)
      return null;

    AccountEntity entity = new AccountEntity();
    entity.setAccount_id(account.getId());
    entity.setAccount_names(account.getNames());
    entity.setAccount_lastnames(account.getLastnames());
    entity.setAccount_email(account.getEmail());
    entity.setAccount_phone(account.getPhone());
    entity.setAccount_created_at(account.getCreatedAt());
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
