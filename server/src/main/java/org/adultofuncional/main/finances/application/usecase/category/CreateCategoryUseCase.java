package org.adultofuncional.main.finances.application.usecase.category;

import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.finances.application.dto.category.CategoryResponse;
import org.adultofuncional.main.finances.application.dto.category.CreateCategoryRequest;
import org.adultofuncional.main.finances.domain.repository.CategoryRepository;
import org.adultofuncional.main.finances.infrastructure.persistence.entity.CategoryEntity;
import org.adultofuncional.main.shared.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: Crear una nueva categoría (solo administradores).
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 */
@Service
@RequiredArgsConstructor
public class CreateCategoryUseCase {

    private final CategoryRepository categoryRepository;

    // TODO: Agregar verificación de rol ADMIN

    @Transactional
    public CategoryResponse execute(CreateCategoryRequest request) {
        boolean exists = categoryRepository.existsByNameAndType(request.getName(), request.getType().name());
        if (exists) {
            throw new BusinessException("Ya existe una categoría con ese nombre y tipo");
        }

        CategoryEntity entity = new CategoryEntity();
        entity.setCategoryName(request.getName());
        entity.setCategoryType(request.getType().name());

        CategoryEntity saved = categoryRepository.save(entity);
        return CategoryResponse.builder()
                .id(saved.getCategoryId())
                .name(saved.getCategoryName())
                .type(org.adultofuncional.main.finances.domain.enums.CategoryType.valueOf(saved.getCategoryType()))
                .createdAt(saved.getCategoryCreatedAt())
                .deleted(false)
                .build();
    }
}
