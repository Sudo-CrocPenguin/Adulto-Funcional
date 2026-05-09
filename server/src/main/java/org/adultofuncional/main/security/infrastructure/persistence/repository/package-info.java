/**
 * Repositorios Spring Data JPA del módulo de seguridad (gestor de contraseñas).
 *
 * <p>
 * Contiene la interfaz
 * {@link org.adultofuncional.main.security.infrastructure.persistence.repository.PasswordJpaRepository},
 * que extiende {@link org.springframework.data.jpa.repository.JpaRepository} y
 * define consultas específicas para la entidad
 * {@link org.adultofuncional.main.security.infrastructure.persistence.entity.PasswordEntity}.
 * Esta interfaz es utilizada por el adaptador de repositorio en la capa de
 * infraestructura.
 *
 * @author Daniel Salazar
 * @since 1.0
 * @see org.adultofuncional.main.security.infrastructure.persistence.entity.PasswordEntity
 * @see org.adultofuncional.main.security.infrastructure.repository.PasswordRepositoryImpl
 */
package org.adultofuncional.main.security.infrastructure.persistence.repository;
