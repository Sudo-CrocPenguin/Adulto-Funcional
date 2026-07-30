/**
 * Puertos de repositorio (interfaces) del dominio de cuentas de usuario.
 *
 * <p>
 * Define los contratos de persistencia que los casos de uso requieren para
 * interactuar con las cuentas sin conocer los detalles de almacenamiento.
 * La interfaz expone las operaciones mínimas necesarias (guardado, búsqueda
 * por ID, búsqueda por email, listado, verificación de existencia y
 * eliminación) y es implementada en la capa de infraestructura mediante un
 * adaptador JPA.
 *
 * <h2>Puerto incluido</h2>
 * <ul>
 * <li>{@link org.adultofuncional.main.account.domain.repository.AccountRepository}
 * —
 * Acceso a {@link org.adultofuncional.main.account.domain.model.Account}.
 * Incluye búsqueda por email para la autenticación y validación de unicidad,
 * y verificación de existencia para el caso de uso de eliminación.</li>
 * </ul>
 *
 * <h2>Consideraciones</h2>
 * <ul>
 * <li>El método {@code deleteById} tiene comportamiento silencioso si el
 * identificador no existe; la validación de existencia previa es
 * responsabilidad de la capa de aplicación.</li>
 * <li>El método {@code findByEmail} es utilizado tanto por
 * {@link org.adultofuncional.main.config.security.DatabaseUserDetailsService}
 * durante el login como por los casos de uso para validar unicidad del
 * email.</li>
 * <li>La implementación concreta se encuentra en el paquete
 * {@code org.adultofuncional.main.account.infrastructure.repository}.</li>
 * </ul>
 *
 * @author Daniel Salazar, Miguel Angel Blandon Montes
 * @since 0.0.1
 * @see org.adultofuncional.main.account.domain.model.Account
 * @see org.adultofuncional.main.account.infrastructure.repository.AccountRepositoryImpl
 */
package org.adultofuncional.main.account.domain.repository;
