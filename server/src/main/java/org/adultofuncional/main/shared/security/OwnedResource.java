package org.adultofuncional.main.shared.security;

/**
 * Contrato que deben implementar los DTOs de respuesta cuyos recursos
 * pertenecen a un usuario específico.
 *
 * <p>
 * Permite que {@link OwnershipValidator} valide el acceso sin acoplarse
 * a ningún módulo concreto. Cada módulo expone el email del propietario
 * a través de este contrato.
 *
 * @author Juan Sebastian Rios
 * @since 0.0.1
 * @see OwnershipValidator
 */
public interface OwnedResource {

  /**
   * Retorna el email del propietario del recurso.
   *
   * @return email del usuario propietario
   */
  String getEmail();
}
