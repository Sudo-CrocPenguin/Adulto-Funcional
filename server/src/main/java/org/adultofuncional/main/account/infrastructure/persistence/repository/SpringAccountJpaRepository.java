package org.adultofuncional.main.account.infrastructure.persistence.repository;

import org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA para la entidad {@link AccountEntity}.
 * <p>
 * Extiende {@link JpaRepository} y es escaneado automaticamente por Spring Data JPA,
 * que genera una implementacion en tiempo de ejecucion. Actua como adaptador para acceder
 * a la base de datos relacional (MariaDB) mediante JPA.
 * <p>
 * Proporciona:
 * <ul>
 *   <li>Metodos CRUD heredados: {@code save}, {@code findById}, {@code findAll}, {@code deleteById}.</li>
 *   <li>Consultas derivadas del nombre del metodo, como {@code findByAccount_email}.</li>
 * </ul>
 * Cuando se invoca un metodo como {@code save}, el proxy generado utiliza el
 * {@link jakarta.persistence.EntityManager} para traducir la operacion a SQL y ejecutarla
 * contra la base de datos.
 *
 * @author daniel
 * @since 0.0.1
 */
public interface SpringAccountJpaRepository extends JpaRepository<AccountEntity, UUID> {

    /**
     * Busca una cuenta por su direccion de email exacta.
     *
     * @param email el email a buscar
     * @return un {@link Optional} con la entidad encontrada, o {@code Optional.empty()} si no existe
     */
    Optional<AccountEntity> findByAccount_email(String email);
}