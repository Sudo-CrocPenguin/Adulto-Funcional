/**
 * Capa de aplicación del módulo de cuentas.
 *
 * <p>
 * Orquesta los casos de uso y define los DTOs de entrada/salida.
 * Los casos de uso coordinan el dominio y los puertos de repositorio
 * sin depender de la infraestructura externa (JPA, REST).
 * </p>
 *
 * <p>
 * <strong>Componentes:</strong>
 * <ul>
 * <li>{@code usecase} — Casos de uso: {@code GetAccountUseCase},
 * {@code UpdateAccountUseCase}. Próximamente {@code DeleteAccountUseCase}.</li>
 * <li>{@code dto} — Objetos de transferencia:
 * <ul>
 * <li>{@code AccountResponse} — Datos de cuenta retornados al cliente (nunca
 * expone el hash de contraseña ni la master key).</li>
 * <li>{@code UpdateAccountRequest} — Datos de entrada validados con Jakarta
 * Validation. Incluye {@code @NoHtml} en todos los campos de texto para
 * prevenir la inyección de HTML malicioso (Stored XSS).</li>
 * </ul>
 * </li>
 * </ul>
 *
 * <p>
 * Seguridad: La validación anti-XSS se aplica mediante la anotación
 * personalizada
 * {@link org.adultofuncional.main.shared.security.NoHtml}, que rechaza
 * cualquier string
 * que contenga HTML (basado en Jsoup con {@code Safelist.none()}). Esto impide
 * que un
 * atacante almacene scripts en la base de datos a través de los campos de
 * texto.
 * </p>
 *
 * @author Miguel Angel Blandon Montes, Juan Sebastian Rios
 * @since 0.0.1
 */
package org.adultofuncional.main.account.application;
