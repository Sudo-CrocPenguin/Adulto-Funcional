package org.adultofuncional.main.finances.infrastructure.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.adultofuncional.main.finances.infrastructure.persistence.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

  @Query("""
      SELECT category
      FROM CategoryEntity category
      LEFT JOIN category.ownerAccount owner
      WHERE category.categoryId = :categoryId
        AND (category.categoryScope = 'SYSTEM' OR owner.accountId = :accountId)
      """)
  java.util.Optional<CategoryEntity> findAccessibleById(
      @Param("accountId") UUID accountId,
      @Param("categoryId") UUID categoryId);

  @Query("""
      SELECT category
      FROM CategoryEntity category
      LEFT JOIN category.ownerAccount owner
      WHERE category.categoryId = :categoryId
        AND category.categoryType = :categoryType
        AND (category.categoryScope = 'SYSTEM' OR owner.accountId = :accountId)
      """)
  java.util.Optional<CategoryEntity> findAccessibleByIdAndType(
      @Param("accountId") UUID accountId,
      @Param("categoryId") UUID categoryId,
      @Param("categoryType") String categoryType);

  @Query("""
      SELECT category
      FROM CategoryEntity category
      LEFT JOIN category.ownerAccount owner
      WHERE category.categoryScope = 'PERSONAL'
        AND category.categoryId = :categoryId
        AND owner.accountId = :accountId
      """)
  java.util.Optional<CategoryEntity> findPersonalByIdAndOwner(
      @Param("accountId") UUID accountId,
      @Param("categoryId") UUID categoryId);

  @Query("""
      SELECT category
      FROM CategoryEntity category
      LEFT JOIN category.ownerAccount owner
      WHERE (category.categoryScope = 'SYSTEM' OR owner.accountId = :accountId)
        AND (:categoryType IS NULL OR category.categoryType = :categoryType)
      ORDER BY category.categoryName ASC
      """)
  List<CategoryEntity> findAllAccessible(
      @Param("accountId") UUID accountId,
      @Param("categoryType") String categoryType);

  @Query("""
      SELECT category
      FROM CategoryEntity category
      LEFT JOIN category.ownerAccount owner
      WHERE category.categoryId IN :categoryIds
        AND (category.categoryScope = 'SYSTEM' OR owner.accountId = :accountId)
      """)
  List<CategoryEntity> findAllAccessibleById(
      @Param("accountId") UUID accountId,
      @Param("categoryIds") Iterable<UUID> categoryIds);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("""
      DELETE FROM CategoryEntity category
      WHERE category.categoryScope = 'PERSONAL'
        AND category.categoryId = :categoryId
        AND category.ownerAccount.accountId = :accountId
      """)
  int deletePersonalByIdAndOwner(
      @Param("accountId") UUID accountId,
      @Param("categoryId") UUID categoryId);

}
