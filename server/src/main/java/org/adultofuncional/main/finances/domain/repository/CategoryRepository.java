package org.adultofuncional.main.finances.domain.repository;

import org.adultofuncional.main.finances.domain.model.Category;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto del repositorio para categorías.
 * Define métodos básicos que los casos de uso necesitan.
 *
 * @author Daniel Salazar
 * @since 1.0
 */
public interface CategoryRepository {

  /**
   * Busca una categoría por su identificador.
   * 
   * @param id el UUID de la categoría
   * @return Optional con la categoría si existe
   */
  Optional<Category> findById(UUID id);

  /**
   * Lista todas las categorías disponibles.
   * 
   * @return lista de categorías
   */
  List<Category> findAll();

  List<Category> findAllById(Iterable<UUID> ids);

  /**
   * Guarda una categoría (nueva o actualizada).
   * 
   * @param category la categoría a guardar
   * @return la categoría guardada
   */
  Category save(Category category);

  /**
   * Elimina una categoría por su ID.
   * 
   * @param id el UUID de la categoría
   */
  void deleteById(UUID id);
}
