package org.adultofuncional.main.finances.application.usecase.category;

import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.finances.application.dto.category.CategoryResponse;
import org.adultofuncional.main.finances.application.dto.category.CreateCategoryRequest;
import org.adultofuncional.main.finances.domain.model.Category;
import org.adultofuncional.main.finances.domain.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateCategoryUseCase {
    private final CategoryRepository categoryRepository;

    @Transactional
    public CategoryResponse execute(CreateCategoryRequest request) {
        Category category = Category.create(request.getName(), request.getType());
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
