package org.adultofuncional.main.finances.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.adultofuncional.main.finances.domain.model.Category;
import org.adultofuncional.main.finances.domain.repository.CategoryRepository;
import org.adultofuncional.main.finances.infrastructure.persistence.entity.CategoryEntity;
import org.adultofuncional.main.finances.infrastructure.persistence.mapper.CategoryMapper;
import org.adultofuncional.main.finances.infrastructure.persistence.repository.SpringCategoryJpaRepository;

public class CategoryRepositoryImpl implements CategoryRepository {

  private final SpringCategoryJpaRepository categoryJpaRepository;
  private final CategoryMapper categoryMapper;

  @Override
  public Optional<Category> findById(UUID id) {
    return categoryJpaRepository.findById(id).map(categoryMapper::toDomain);
  }

  @Override
  public List<Category> findAll() {
    return categoryJpaRepository.findAll()
        .stream()
        .map(categoryMapper::toDomain)
        .toList();
  }

  @Override
  public Category save(Category category) {
    CategoryEntity entity = categoryMapper.toEntity(category);
    CategoryEntity saved = categoryJpaRepository.save(entity);
    return categoryMapper.toDomain(saved);
  }

  @Override
  public void deleteById(UUID id) {
    categoryJpaRepository.deleteById(id);
  }

}
