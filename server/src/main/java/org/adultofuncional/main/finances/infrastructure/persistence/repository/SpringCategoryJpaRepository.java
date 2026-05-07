package org.adultofuncional.main.finances.infrastructure.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.adultofuncional.main.finances.infrastructure.persistence.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio Spring Data JPA para la entidad {@link CategoryEntity}.
 * <p>
 * Proporciona métodos de acceso a la tabla {@code categories} sin exponer
 * la implementación concreta al dominio. Este repositorio es utilizado
 * por el adaptador correspondiente en la capa de infraestructura.
 * </p>
 *
 * @author Daniel Salazar
 * @see CategoryEntity
 * @since 1.0
 */
public interface SpringCategoryJpaRepository extends JpaRepository<CategoryEntity, UUID> {

  /**
   * Busca categorías por su tipo (Ingreso o Egreso).
   *
   * @param categoryType el tipo de categoría (ej: "Ingreso", "Egreso")
   * @return lista de categorías que coinciden con el tipo (puede estar vacía)
   */
  List<CategoryEntity> findByCategoryType(String categoryType);
}
