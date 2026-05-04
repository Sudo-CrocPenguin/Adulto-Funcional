package org.adultofuncional.main.account.application.dto;

import lombok.Builder;
import lombok.Getter;
import org.adultofuncional.main.shared.security.OwnedResource;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de respuesta que expone únicamente los datos no sensibles de una cuenta.
 *
 * <p>
 * Implementa {@link OwnedResource} para permitir que
 * {@link org.adultofuncional.main.shared.security.OwnershipValidator}
 * valide el acceso sin acoplarse al módulo de cuentas. El método
 * {@link #getEmail()} ya existe en este DTO y satisface el contrato
 * sin requerir ningún cambio adicional.
 *
 * <p>
 * Nunca expone campos sensibles como {@code account_password} ni
 * {@code account_master_key} — el filtrado ocurre en el mapper de la
 * capa de infraestructura al construir este objeto.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 * @see OwnedResource
 * @see org.adultofuncional.main.shared.security.OwnershipValidator
 */
@Getter
@Builder
public class AccountResponse implements OwnedResource {

  /** Identificador UUID v7 de la cuenta. Corresponde a {@code account_id}. */
  private final UUID id;

  /** Nombres del titular. Corresponde a {@code account_names}. */
  private final String names;

  /** Apellidos del titular. Corresponde a {@code account_lastnames}. */
  private final String lastnames;

  /**
   * Correo electrónico del titular. Corresponde a {@code account_email}.
   * Usado como username en la autenticación JWT y como identificador
   * de ownership en
   * {@link org.adultofuncional.main.shared.security.OwnershipValidator}.
   */
  private final String email;

  /** Teléfono de contacto. Corresponde a {@code account_phone}. */
  private final String phone;

  /** Fecha de creación de la cuenta. Corresponde a {@code account_created_at}. */
  private final LocalDateTime createdAt;
}
