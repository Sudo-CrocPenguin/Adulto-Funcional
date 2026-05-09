/**
 * Puertos de repositorio (interfaces) del dominio de seguridad (gestor de
 * contraseñas).
 *
 * <p>
 * Define los contratos de persistencia que los casos de uso requieren para
 * interactuar con las credenciales almacenadas sin conocer los detalles de
 * almacenamiento. La interfaz expone las operaciones mínimas necesarias
 * (búsqueda, listado por cuenta, guardado y eliminación) y es implementada en
 * la capa de infraestructura mediante un adaptador JPA.
 *
 * <h2>Puerto incluido</h2>
 * <ul>
 * <li>{@link org.adultofuncional.main.security.domain.repository.PasswordRepository}
 * —
 * Acceso a {@link org.adultofuncional.main.security.domain.model.Password}.
 * Incluye búsqueda por ID, listado de todas las credenciales de una cuenta,
 * persistencia y eliminación.</li>
 * </ul>
 *
 * <h2>Consideraciones</h2>
 * <ul>
 * <li>El método {@code deleteById} tiene comportamiento silencioso si el
 * identificador no existe; la validación de existencia previa es
 * responsabilidad de la capa de aplicación.</li>
 * <li>El método {@code findAllByAccountId} retorna todas las credenciales de
 * una cuenta; los datos sensibles (contraseña cifrada) se mantienen en el
 * modelo de dominio y nunca se exponen sin control.</li>
 * <li>La implementación concreta se encuentra en el paquete
 * {@code org.adultofuncional.main.security.infrastructure.repository}.</li>
 * </ul>
 *
 * @author Daniel Salazar, Juan Sebastian Rios
 * @since 1.0
 * @see org.adultofuncional.main.security.domain.model.Password
 * @see org.adultofuncional.main.security.infrastructure.repository.PasswordRepositoryImpl
 */
package org.adultofuncional.main.security.domain.repository;
