package org.adultofuncional.main.finances.infrastructure.persistence.mapper;

import org.adultofuncional.main.finances.domain.enums.CategoryType;
import org.adultofuncional.main.finances.domain.enums.CategoryScope;
import org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity;
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
        entity.getNormalizedName(),
        CategoryType.valueOf(entity.getCategoryType()),
        CategoryScope.valueOf(entity.getCategoryScope()),
        entity.getOwnerAccount() == null ? null : entity.getOwnerAccount().getAccountId());
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
    entity.setNormalizedName(category.getNormalizedName());
    entity.setCategoryType(category.getType().name());
    entity.setCategoryScope(category.getScope().name());
    if (category.getOwnerAccountId() != null) {
      AccountEntity owner = new AccountEntity();
      owner.setAccountId(category.getOwnerAccountId());
      entity.setOwnerAccount(owner);
    }

    return entity;
  }
}
