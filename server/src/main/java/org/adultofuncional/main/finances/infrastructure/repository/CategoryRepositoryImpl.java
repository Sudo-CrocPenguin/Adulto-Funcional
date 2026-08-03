package org.adultofuncional.main.finances.infrastructure.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.adultofuncional.main.finances.domain.model.Category;
import org.adultofuncional.main.finances.domain.enums.CategoryType;
import org.adultofuncional.main.finances.domain.repository.CategoryRepository;
import org.adultofuncional.main.finances.infrastructure.persistence.entity.CategoryEntity;
import org.adultofuncional.main.finances.infrastructure.persistence.mapper.CategoryMapper;
import org.adultofuncional.main.finances.infrastructure.persistence.repository.SpringCategoryJpaRepository;
import org.adultofuncional.main.shared.pagination.PageQuery;
import org.adultofuncional.main.shared.pagination.PageResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

/**
 * Adaptador concreto del puerto {@link CategoryRepository}.
 *
 * <p>
 * Implementa las operaciones de persistencia de categorías delegando en
 * {@link SpringCategoryJpaRepository} (Spring Data JPA) y utilizando el
 * {@link CategoryMapper} para convertir entre las entidades JPA
 * ({@link CategoryEntity}) y el modelo de dominio ({@link Category}).
 *
 * Las consultas aplican siempre alcance y ownership antes de mapear el dato al
 * dominio; el adaptador no expone accesos globales que puedan omitir esa regla.
 *
 * @author Juan Sebastian Rios
 * @since 1.0
 * @see CategoryRepository
 * @see SpringCategoryJpaRepository
 * @see CategoryMapper
 */
@Repository
@RequiredArgsConstructor
public class CategoryRepositoryImpl implements CategoryRepository {

  private static final Map<String, String> SORT_FIELDS = Map.of(
      "name", "categoryName",
      "type", "categoryType",
      "scope", "categoryScope",
      "id", "categoryId");

  private final SpringCategoryJpaRepository categoryJpaRepository;
  private final CategoryMapper categoryMapper;

  @Override
  public Optional<Category> findAccessibleById(UUID accountId, UUID categoryId) {
    return categoryJpaRepository.findAccessibleById(accountId, categoryId)
        .map(categoryMapper::toDomain);
  }

  @Override
  public Optional<Category> findAccessibleByIdAndType(
      UUID accountId,
      UUID categoryId,
      CategoryType type) {
    return categoryJpaRepository.findAccessibleByIdAndType(accountId, categoryId, type.name())
        .map(categoryMapper::toDomain);
  }

  @Override
  public Optional<Category> findPersonalByIdAndOwner(UUID accountId, UUID categoryId) {
    return categoryJpaRepository.findPersonalByIdAndOwner(accountId, categoryId)
        .map(categoryMapper::toDomain);
  }

  @Override
  public PageResult<Category> findPageAccessible(
      UUID accountId,
      CategoryType type,
      String searchTerm,
      PageQuery pageQuery) {
    String persistedType = type == null ? null : type.name();
    String entitySortField = SORT_FIELDS.get(pageQuery.sortBy());
    Sort.Direction direction = pageQuery.ascending() ? Sort.Direction.ASC : Sort.Direction.DESC;
    Sort sort = Sort.by(direction, entitySortField);
    if (!entitySortField.equals("categoryId")) {
      sort = sort.and(Sort.by(direction, "categoryId"));
    }
    PageRequest pageable = PageRequest.of(pageQuery.number(), pageQuery.size(), sort);
    Page<CategoryEntity> page = categoryJpaRepository.findPageAccessible(
        accountId,
        persistedType,
        searchTerm,
        pageable);
    return new PageResult<>(
        page.getContent().stream().map(categoryMapper::toDomain).toList(),
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages(),
        page.hasNext(),
        page.hasPrevious());
  }

  @Override
  public List<Category> findAllAccessibleById(UUID accountId, Iterable<UUID> ids) {
    return categoryJpaRepository.findAllAccessibleById(accountId, ids).stream()
        .map(categoryMapper::toDomain)
        .toList();
  }

  @Override
  public boolean deletePersonalByIdAndOwner(UUID accountId, UUID categoryId) {
    return categoryJpaRepository.deletePersonalByIdAndOwner(accountId, categoryId) > 0;
  }

  /**
   * Persiste una categoría nueva o actualiza una existente.
   *
   * <p>
   * Convierte el modelo de dominio a entidad JPA con
   * {@link CategoryMapper#toEntity(Category)}, la guarda mediante Spring Data JPA
   * y vuelve a convertir el resultado a dominio para retornar la versión
   * persistida (incluyendo el ID si fue generado).
   *
   * @param category la categoría a guardar. No debe ser {@code null}.
   * @return la categoría persistida como modelo de dominio.
   */
  @Override
  public Category save(Category category) {
    CategoryEntity entity = categoryMapper.toEntity(category);
    categoryJpaRepository.findById(category.getId())
        .ifPresent(existing -> entity.setVersion(existing.getVersion()));
    CategoryEntity saved = categoryJpaRepository.saveAndFlush(entity);
    return categoryMapper.toDomain(saved);
  }

}
