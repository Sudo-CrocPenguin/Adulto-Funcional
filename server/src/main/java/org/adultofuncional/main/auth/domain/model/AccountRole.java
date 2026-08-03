package org.adultofuncional.main.auth.domain.model;

/**
 * Rol persistente concedido a una cuenta.
 *
 * <p>El dominio conserva el nombre sin el prefijo técnico de Spring Security.
 * La conversión a una autoridad {@code ROLE_*} se realiza en el borde de
 * seguridad, evitando que el modelo dependa del framework.</p>
 */
public enum AccountRole {
  USER,
  ADMIN;

  /**
   * Convierte el rol de dominio a la autoridad esperada por Spring Security.
   *
   * @return autoridad con prefijo {@code ROLE_}
   */
  public String asAuthority() {
    return "ROLE_" + name();
  }
}
