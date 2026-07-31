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
 * Implementa {@link OwnedResource} por compatibilidad con contratos previos.
 * La validación de ownership actual se hace con el campo {@code id}, porque el
 * UUID de cuenta es estable y no cambia cuando el usuario actualiza su email.
 *
 * <p>
 * Nunca expone campos sensibles como {@code account_password} ni
 * {@code account_master_key} — el filtrado ocurre en el mapper de la
 * capa de infraestructura al construir este objeto.
 *
 * @author Miguel Angel Blandon Montes, Juan Sebastian Rios
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
   * Se conserva como dato visible del perfil, pero no se usa como identificador
   * de ownership porque puede cambiar.
   */
  private final String email;

  /** Teléfono de contacto. Corresponde a {@code account_phone}. */
  private final String phone;

  /** Fecha de creación de la cuenta. Corresponde a {@code account_created_at}. */
  private final LocalDateTime createdAt;
}
