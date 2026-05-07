package org.adultofuncional.main.finances.infrastructure.persistence.mapper;

import org.adultofuncional.main.finances.domain.enums.CategoryType;
import org.adultofuncional.main.finances.domain.model.Category;
import org.adultofuncional.main.finances.infrastructure.persistence.entity.CategoryEntity;
import org.springframework.stereotype.Component;


@Component
public class CategoryMapper {
    
    public Category toDomain(CategoryEntity entity) {
        if (entity == null) return null;

        return Category.reconstitute(
            entity.getCategoryId(),
            entity.getCategoryName(),
            CategoryType.valueOf(entity.getCategoryType()),
            entity.getCategoryCreatedAt()
        );
    }

    public CategoryEntity toEntity(Category category) {
        if (category == null) return null;

        CategoryEntity entity = new CategoryEntity();
        entity.setCategoryId(category.getId());
        entity.setCategoryName(category.getName());
        entity.setCategoryType(category.getType().name());

        return entity;
    }
}
