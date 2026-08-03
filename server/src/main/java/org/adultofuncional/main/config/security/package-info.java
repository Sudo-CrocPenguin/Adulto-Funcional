/**
 * Integración de Spring Security para JWT, cookies, CSRF y CORS.
 *
 * <p>La cadena no crea {@code HttpSession}, pero valida access tokens ligados a
 * familias persistidas. Extrae primero Bearer y después cookie, publica un
 * principal estable, aplica CSRF a mutaciones autenticadas por cookie y usa
 * handlers JSON uniformes para autenticación, autorización y CORS.</p>
 *
 * <p>También centraliza issuer/audience, atributos de cookies, detección del
 * canal nativo, headers de seguridad y validación fail-fast del perfil
 * {@code prod}.</p>
 *
 * @author Equipo de desarrollo Adulto Funcional
 * @since 0.0.1
 */
package org.adultofuncional.main.config.security;
