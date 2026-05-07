package org.adultofuncional.main.finances.application.usecase.category;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.finances.application.dto.category.CategoryResponse;
import org.adultofuncional.main.finances.application.dto.category.UpdateCategoryRequest;
import org.adultofuncional.main.finances.domain.repository.CategoryRepository;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UpdateCategoryUseCase {
    private final CategoryRepository categoryRepository;

    @Transactional
    public CategoryResponse execute(UUID categoryId, UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new NotFoundException("Categoría no encontrada con id: " + categoryId));
        if (StringUtils.hasText(request.getName())) {
            category.updateName(request.getName());
        }
        if (request.getType() != null) {
            category.updateType(request.getType());
        }
        Category saved = categoryRepository.save(category);
        return CategoryResponse.builder()
            .id(saved.getId())
            .name(saved.getName())
            .type(saved.getType())
            .createdAt(saved.getCreatedAt())
            .deleted(false)
            .build();
    }
}
