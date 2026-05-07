package org.adultofuncional.main.finances.application.usecase.category;

import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.finances.application.dto.category.CategoryFilterRequest;
import org.adultofuncional.main.finances.application.dto.category.CategoryResponse;
import org.adultofuncional.main.finances.domain.repository.CategoryRepository;
import org.adultofuncional.main.finances.infrastructure.persistence.entity.CategoryEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Caso de uso: Listar categorías con filtros opcionales.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 */
@Service
@RequiredArgsConstructor
public class ListCategoriesUseCase {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponse> execute(CategoryFilterRequest filter) {
        List<CategoryEntity> categories;
        if (filter != null && filter.isIncludeDeleted()) {
            categories = categoryRepository.findAllWithDeleted();
        } else {
            categories = categoryRepository.findAll();
        }

        if (filter != null && filter.getType() != null) {
            categories = categories.stream()
                    .filter(c -> c.getCategoryType().equals(filter.getType().name()))
                    .collect(Collectors.toList());
        }

        return categories.stream()
                .map(entity -> CategoryResponse.builder()
                        .id(entity.getCategoryId())
                        .name(entity.getCategoryName())
                        .type(org.adultofuncional.main.finances.domain.enums.CategoryType.valueOf(entity.getCategoryType()))
                        .createdAt(entity.getCategoryCreatedAt())
                        .deleted(entity.getCategoryDeletedAt() != null)
                        .build())
                .collect(Collectors.toList());
    }
}
