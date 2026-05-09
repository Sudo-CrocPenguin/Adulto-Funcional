package org.adultofuncional.main.finances.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.adultofuncional.main.finances.domain.model.Category;
import org.adultofuncional.main.finances.domain.repository.CategoryRepository;
import org.adultofuncional.main.finances.infrastructure.persistence.entity.CategoryEntity;
import org.adultofuncional.main.finances.infrastructure.persistence.mapper.CategoryMapper;
import org.adultofuncional.main.finances.infrastructure.persistence.repository.SpringCategoryJpaRepository;
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
 * <p>
 * <strong>Métodos implementados:</strong>
 * <ul>
 * <li>{@link #findById(UUID)} — busca una categoría por ID y la convierte
 * a dominio.</li>
 * <li>{@link #findAll()} — retorna todas las categorías como lista de
 * dominio.</li>
 * <li>{@link #findAllById(Iterable)} — consulta por lote de IDs y convierte
 * a dominio (evita N+1 en listados de otras entidades).</li>
 * <li>{@link #save(Category)} — persiste una categoría nueva o actualizada,
 * devolviendo el modelo de dominio resultante.</li>
 * <li>{@link #deleteById(UUID)} — elimina una categoría por su ID.</li>
 * </ul>
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

  private final SpringCategoryJpaRepository categoryJpaRepository;
  private final CategoryMapper categoryMapper;

  /**
   * Busca una categoría por su identificador único.
   *
   * <p>
   * Consulta el repositorio Spring Data JPA y convierte la entidad resultante
   * al modelo de dominio mediante
   * {@link CategoryMapper#toDomain(CategoryEntity)}.
   *
   * @param id UUID de la categoría. No debe ser {@code null}.
   * @return {@link Optional} con la categoría si existe;
   *         {@code Optional.empty()} en caso contrario.
   */
  @Override
  public Optional<Category> findById(UUID id) {
    return categoryJpaRepository.findById(id).map(categoryMapper::toDomain);
  }

  /**
   * Retorna todas las categorías registradas en el sistema.
   *
   * <p>
   * Recupera todas las entidades {@link CategoryEntity} y las convierte una a una
   * al modelo de dominio {@link Category} usando el mapper.
   *
   * @return lista de categorías (vacía si no hay registros).
   */
  @Override
  public List<Category> findAll() {
    return categoryJpaRepository.findAll()
        .stream()
        .map(categoryMapper::toDomain)
        .toList();
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
    CategoryEntity saved = categoryJpaRepository.save(entity);
    return categoryMapper.toDomain(saved);
  }

  /**
   * Elimina una categoría por su identificador único.
   *
   * <p>
   * Si no existe ninguna categoría con el ID dado, la operación no tiene efecto
   * (comportamiento silencioso de Spring Data JPA).
   *
   * @param id UUID de la categoría a eliminar. No debe ser {@code null}.
   */
  @Override
  public void deleteById(UUID id) {
    categoryJpaRepository.deleteById(id);
  }

  /**
   * Busca múltiples categorías por sus identificadores en un solo lote.
   *
   * <p>
   * Utiliza {@link SpringCategoryJpaRepository#findAllById(Iterable)} para
   * recuperar las entidades en una única consulta y luego las convierte a
   * dominio con el mapper. Este método es usado por los casos de uso que
   * necesitan cargar categorías asociadas a otras entidades (gastos fijos,
   * movimientos) para evitar el problema N+1.
   *
   * @param ids colección de UUIDs de las categorías a buscar.
   * @return lista de modelos de dominio {@link Category} encontrados.
   *         Puede ser de menor tamaño que la entrada si algunos IDs no existen.
   */
  @Override
  public List<Category> findAllById(Iterable<UUID> ids) {
    List<CategoryEntity> entities = categoryJpaRepository.findAllById(ids);
    return entities.stream()
        .map(categoryMapper::toDomain)
        .toList();
  }
}
