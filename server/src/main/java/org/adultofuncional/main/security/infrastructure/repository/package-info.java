/**
 * Adaptadores de repositorio del módulo de seguridad (gestor de contraseñas).
 *
 * <p>
 * Contiene la implementación concreta del puerto
 * {@link org.adultofuncional.main.security.domain.repository.PasswordRepository},
 * que conecta el dominio con Spring Data JPA y MariaDB. Utiliza
 * {@link org.adultofuncional.main.security.infrastructure.persistence.repository.PasswordJpaRepository}
 * y el
 * {@link org.adultofuncional.main.security.infrastructure.persistence.mapper.PasswordMapper}
 * para traducir entre entidades JPA y el modelo de dominio.
 *
 * <h2>Adaptador incluido</h2>
 * <ul>
 * <li>{@link org.adultofuncional.main.security.infrastructure.repository.PasswordRepositoryImpl}
 * —
 * Implementa todas las operaciones definidas en el puerto de dominio:
 * buscar por ID, listar por cuenta, guardar y eliminar credenciales.</li>
 * </ul>
 *
 * @author Jeronimo Ospina Zapata
 * @since 0.0.1
 * @see org.adultofuncional.main.security.domain.repository.PasswordRepository
 * @see org.adultofuncional.main.security.infrastructure.persistence.repository.PasswordJpaRepository
 * @see org.adultofuncional.main.security.infrastructure.persistence.mapper.PasswordMapper
 */
package org.adultofuncional.main.security.infrastructure.repository;
