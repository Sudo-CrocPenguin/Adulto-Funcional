package org.adultofuncional.main.finances.infrastructure.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.adultofuncional.main.finances.infrastructure.persistence.entity.FixedExpensesEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio Spring Data JPA para la entidad {@link FixedExpensesEntity}.
 * <p>
 * Proporciona métodos de acceso a la tabla {@code fixed_expenses} sin exponer
 * la implementación concreta al dominio. Este repositorio es utilizado
 * por el adaptador correspondiente en la capa de infraestructura.
 * </p>
 *
 * @author Daniel Salazar
 * @see FixedExpensesEntity
 * @since 1.0
 */
public interface SpringFixedExpenseJpaRepository extends JpaRepository<FixedExpensesEntity, UUID> {

    /**
     * Busca todos los gastos fijos asociados a una cuenta específica.
     *
     * @param accountId el identificador de la cuenta (UUID)
     * @return lista de entidades {@code FixedExpensesEntity} de esa cuenta,
     *         puede estar vacía si no hay gastos fijos registrados
     */
    List<FixedExpensesEntity> findByAccount_AccountId(UUID accountId);

    /**
     * Busca un gasto fijo por su identificador único.
     * <p>
     * Método explícito similar a {@link #findById(Object)} pero con nombre
     * más semántico.
     * </p>
     *
     * @param fixedExpenseId el UUID del gasto fijo
     * @return la entidad si existe, o {@code null} si no se encuentra
     */
    FixedExpensesEntity findByFixedExpenseId(UUID fixedExpenseId);
}