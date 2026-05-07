package org.adultofuncional.main.finances.application.usecase.category;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.finances.application.dto.category.CategoryFilterRequest;
import org.adultofuncional.main.finances.application.dto.category.CategoryResponse;
import org.adultofuncional.main.finances.domain.model.Category;
import org.adultofuncional.main.finances.domain.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListCategoriesUseCase {
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponse> execute(CategoryFilterRequest filter) {
        List<Category> categories = categoryRepository.findAll();
        if (filter != null && filter.getType() != null) {
            categories = categories.stream()
                .filter(c -> c.getType() == filter.getType())
                .collect(Collectors.toList());
        }
        return categories.stream()
            .map(c -> CategoryResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .type(c.getType())
                .createdAt(c.getCreatedAt())
                .deleted(false)
                .build())
            .collect(Collectors.toList());
    }
}
