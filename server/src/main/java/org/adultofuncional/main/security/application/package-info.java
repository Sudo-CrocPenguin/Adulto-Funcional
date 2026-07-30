/**
 * Capa de aplicación del módulo de seguridad (gestor de contraseñas).
 *
 * <p>
 * Orquesta los casos de uso y define los DTOs de entrada/salida para la
 * gestión de credenciales. Los casos de uso coordinan el dominio y los
 * puertos de repositorio y servicios sin depender de la infraestructura
 * externa (JPA, REST), manteniendo la lógica de negocio desacoplada de los
 * controladores y de la capa de persistencia.
 *
 * <h2>Componentes</h2>
 * <ul>
 * <li>{@code dto} — Objetos de transferencia de datos:
 * <ul>
 * <li>{@link org.adultofuncional.main.security.application.dto.PasswordRequest}</li>
 * <li>{@link org.adultofuncional.main.security.application.dto.PasswordUpdateRequest}</li>
 * <li>{@link org.adultofuncional.main.security.application.dto.PasswordResponse}</li>
 * </ul>
 * </li>
 * <li>{@code usecase} — Casos de uso:
 * <ul>
 * <li>{@link org.adultofuncional.main.security.application.usecase.CreatePasswordUseCase}</li>
 * <li>{@link org.adultofuncional.main.security.application.usecase.ListPasswordsUseCase}</li>
 * <li>{@link org.adultofuncional.main.security.application.usecase.GetPasswordUseCase}</li>
 * <li>{@link org.adultofuncional.main.security.application.usecase.UpdatePasswordUseCase}</li>
 * <li>{@link org.adultofuncional.main.security.application.usecase.DeletePasswordUseCase}</li>
 * </ul>
 * </li>
 * </ul>
 *
 * <h2>Seguridad</h2>
 * <p>
 * La validación anti‑XSS se aplica mediante la anotación personalizada
 * {@link org.adultofuncional.main.shared.security.NoHtml} en los campos de
 * texto libre de los DTOs de entrada.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 * @see org.adultofuncional.main.shared.security.NoHtml
 * @see org.adultofuncional.main.security.application.dto
 * @see org.adultofuncional.main.security.application.usecase
 */
package org.adultofuncional.main.security.application;
