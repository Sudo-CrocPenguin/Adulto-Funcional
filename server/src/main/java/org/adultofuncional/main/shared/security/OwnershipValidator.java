package org.adultofuncional.main.shared.security;

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
 * El recurso a validar debe implementar {@link OwnedResource} para exponer
 * el email del propietario sin acoplar este validador a ningún módulo
 * concreto.
 *
 * @author Juan Sebastian Rios
 * @since 0.0.1
 * @see OwnedResource
 */
@Component
public class OwnershipValidator {

  /**
   * Verifica que el usuario autenticado sea el propietario del recurso.
   *
   * <p>
   * Compara el email del recurso con el email extraído del JWT por
   * {@link org.adultofuncional.main.config.security.JwtAuthenticationFilter}.
   * Si no coinciden, lanza {@link UnauthorizedException} antes de que
   * el caso de uso sea invocado.
   *
   * @param resource    recurso que expone el email de su propietario
   * @param loggedEmail email del usuario autenticado, extraído del JWT
   * @throws UnauthorizedException si el usuario autenticado no es el propietario
   */
  public void validate(OwnedResource resource, String loggedEmail) {
    if (!resource.getEmail().equals(loggedEmail)) {
      throw new UnauthorizedException("No tienes permiso para acceder a este recurso");
    }
  }
}
