/**
 * Capa de aplicación del módulo de autenticación.
 *
 * <p>Contiene los casos de uso y DTOs que orquestan la lógica de
 * autenticación. Los casos de uso coordinan el dominio y la
 * infraestructura sin exponer detalles de persistencia.</p>
 *
 * <p>Componentes:
 * <ul>
 *   <li>{@code usecase} — Casos de uso: {@code LoginUseCase}, {@code RegisterUseCase}</li>
 *   <li>{@code dto} — Objetos de transferencia: {@code AuthResponse}, {@code LoginRequest}, {@code RegisterRequest}</li>
 * </ul>
 *
 * @author Juan Sebastian Rios, Lydis Ester Jaraba, Miguel Angel Blandon Montes
 * @since 0.0.1
 */
package org.adultofuncional.main.auth.application;
