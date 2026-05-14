/**
 * Capa de dominio del módulo de seguridad (gestor de contraseñas).
 *
 * <p>
 * Contiene el modelo de dominio que representa una credencial almacenada de
 * forma segura y el puerto de repositorio que desacopla el acceso a datos.
 * Esta capa es completamente independiente de la infraestructura externa
 * (JPA, REST, controladores) y encapsula las reglas de negocio fundamentales
 * del cifrado y almacenamiento de contraseñas.
 *
 * <h2>Componentes</h2>
 * <ul>
 * <li>{@code model} — Entidad del dominio:
 * <ul>
 * <li>{@link org.adultofuncional.main.security.domain.model.Password} —
 * Credencial de un servicio externo con cifrado AES‑256.</li>
 * </ul>
 * </li>
 * <li>{@code repository} — Puerto de persistencia:
 * <ul>
 * <li>{@link org.adultofuncional.main.security.domain.repository.PasswordRepository}</li>
 * </ul>
 * </li>
 * </ul>
 *
 * <h2>Consideraciones</h2>
 * <ul>
 * <li><strong>Identidad:</strong> El modelo genera su propio UUID v7 a través
 * del método de fábrica {@code create}.</li>
 * <li><strong>Invariantes de negocio:</strong> Valida estrictamente sus campos
 * obligatorios y la coherencia temporal.</li>
 * <li><strong>Separación de operaciones:</strong> El método
 * {@code reconstitute} permite reconstruir instancias desde persistencia.</li>
 * </ul>
 *
 * @author Jeronimo Ospina Zapata, Daniel Salazar
 * @since 0.0.1
 * @see org.adultofuncional.main.security.domain.model
 * @see org.adultofuncional.main.security.domain.repository
 */
package org.adultofuncional.main.security.domain;
