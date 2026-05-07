package org.adultofuncional.main.finances.application.usecase.category;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.finances.domain.repository.CategoryRepository;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteCategoryUseCase {
    private final CategoryRepository categoryRepository;

    @Transactional
    public void execute(UUID categoryId) {
        if (!categoryRepository.findById(categoryId).isPresent()) {
            throw new NotFoundException("Categoría no encontrada con id: " + categoryId);
        }
        categoryRepository.deleteById(categoryId);
    }
}
