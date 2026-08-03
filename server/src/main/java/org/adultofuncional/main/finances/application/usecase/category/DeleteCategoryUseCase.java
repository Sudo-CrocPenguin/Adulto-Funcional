package org.adultofuncional.main.finances.application.usecase.category;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.finances.domain.repository.CategoryRepository;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: Eliminar una categoría financiera existente.
 *
 * <p>
 * Elimina en una sola sentencia una categoría PERSONAL de la cuenta. Las
 * categorías SYSTEM y las pertenecientes a terceros se ocultan como 404.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 * @see CategoryRepository
 * @see NotFoundException
 */
@Service
@RequiredArgsConstructor
public class DeleteCategoryUseCase {

  /**
   * Puerto de dominio para la persistencia de categorías.
   * Se resuelve en tiempo de ejecución con la implementación concreta
   * del módulo de infraestructura.
   */
  private final CategoryRepository categoryRepository;

  /**
   * Ejecuta la eliminación de una categoría por su identificador.
   *
   * @param categoryId Identificador único de la categoría a eliminar.
   *                   No puede ser {@code null}.
   * @throws NotFoundException si no existe ninguna categoría con el ID
   *                           proporcionado.
   */
  @Transactional
  public void execute(UUID accountId, UUID categoryId) {
    if (!categoryRepository.deletePersonalByIdAndOwner(accountId, categoryId)) {
      throw new NotFoundException("Categoría no encontrada con id: " + categoryId);
    }
  }
}
