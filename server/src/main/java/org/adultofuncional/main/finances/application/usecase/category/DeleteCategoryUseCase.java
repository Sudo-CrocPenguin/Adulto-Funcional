package org.adultofuncional.main.finances.application.usecase.category;

import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.finances.domain.repository.CategoryRepository;
import org.adultofuncional.main.finances.infrastructure.persistence.entity.CategoryEntity;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Caso de uso: Eliminar lógicamente una categoría (soft delete).
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 */
@Service
@RequiredArgsConstructor
public class DeleteCategoryUseCase {

    private final CategoryRepository categoryRepository;

    @Transactional
    public void execute(UUID categoryId) {
        CategoryEntity entity = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Categoría no encontrada: " + categoryId));
        entity.softDelete();
        categoryRepository.save(entity);
    }
}
