package org.adultofuncional.main.finances.infrastructure.persistence.mapper;

import org.adultofuncional.main.finances.domain.enums.CategoryType;
import org.adultofuncional.main.finances.domain.model.Category;
import org.adultofuncional.main.finances.infrastructure.persistence.entity.CategoryEntity;
import org.springframework.stereotype.Component;

/**
 * Componente que convierte entre las diferentes representaciones de una
 * categoria.
 * 
 * <p>
 * Traduce entre:
 * <ul>
 * <li>{@link CategoryEntity} (JPA) ↔ {@link Category} (dominio)</li>
 * </ul>
 *
 * <p>
 * La conversión del tipo de categoría entre {@code String} y
 * {@link CategoryType}
 * asume que los valores almacenados en base de datos coinciden exactamente con
 * los nombres de las constantes del enum (sensible a mayúsculas).
 * 
 * <p>
 * El método {@code toResponse()} se implementará cuando los DTOs
 * de respuesta estén definidos por el equipo.
 * 
 * @author Lidys Jaraba
 * @since 0.0.1
 * @see Category
 * @see CategoryEntity
 */

@Component
public class CategoryMapper {

  /**
   * Convierte una {@link CategoryEntity} al modelo de dominio {@link Category}.
   *
   * <p>
   * Usa el método de fábrica {@code Category.reconstitute()} para respetar
   * el constructor privado del modelo de dominio.
   *
   * @param entity entidad JPA; si es {@code null} retorna {@code null}
   * @return modelo de dominio reconstituido o {@code null}
   */

  public Category toDomain(CategoryEntity entity) {
    if (entity == null)
      return null;

    return Category.reconstitute(
        entity.getCategoryId(),
        entity.getCategoryName(),
        CategoryType.valueOf(entity.getCategoryType()));
  }

  /**
   * Convierte el modelo de dominio {@link Category} a {@link CategoryEntity}.
   *
   * <p>
   * El campo {@code categoryCreatedAt} no se asigna aquí porque la entidad
   * lo establece automáticamente mediante {@code @PrePersist}.
   *
   * @param category modelo de dominio; si es {@code null} retorna {@code null}
   * @return entidad JPA lista para persistir
   */

  public CategoryEntity toEntity(Category category) {
    if (category == null)
      return null;

    CategoryEntity entity = new CategoryEntity();
    entity.setCategoryId(category.getId());
    entity.setCategoryName(category.getName());
    entity.setCategoryType(category.getType().name());

    return entity;
  }
}
