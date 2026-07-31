package org.adultofuncional.main.shared.security;

import java.util.UUID;

import org.adultofuncional.main.shared.exception.UnauthorizedException;
import org.springframework.stereotype.Component;

/**
 * Componente transversal que valida que el usuario autenticado sea el
 * propietario del recurso que intenta acceder o modificar.
 *
 * <p>
 * Centraliza la lógica de control de acceso por ownership para evitar
 * duplicación entre controladores. Cualquier controlador que maneje
 * recursos de usuario debe usarlo antes de ejecutar operaciones de
 * lectura, escritura o eliminación.
 *
 * <p>
 * La comparación se hace por UUID de cuenta, que es estable y no reutilizable.
 * El email no debe usarse para ownership porque el usuario puede modificarlo
 * y un JWT vigente podría quedar asociado a un correo reutilizado.
 *
 * @author Juan Sebastian Rios
 * @since 0.0.1
 */
@Component
public class OwnershipValidator {

  /**
   * Verifica que el usuario autenticado sea el propietario del recurso.
   *
   * <p>
   * Compara el identificador de la cuenta propietaria del recurso con el
   * {@code accountId} extraído del claim {@code sub} del JWT por
   * {@link org.adultofuncional.main.config.security.JwtAuthenticationFilter}.
   *
   * @param resourceAccountId      identificador de la cuenta dueña del recurso
   * @param authenticatedAccountId identificador de la cuenta autenticada
   * @throws UnauthorizedException si el usuario autenticado no es el propietario
   */
  public void validate(UUID resourceAccountId, UUID authenticatedAccountId) {
    if (resourceAccountId == null || !resourceAccountId.equals(authenticatedAccountId)) {
      throw new UnauthorizedException("No tienes permiso para acceder a este recurso");
    }
  }
}
