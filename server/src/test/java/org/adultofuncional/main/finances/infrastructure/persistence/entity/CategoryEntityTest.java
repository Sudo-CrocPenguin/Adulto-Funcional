package org.adultofuncional.main.finances.infrastructure.persistence.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CategoryEntityTest {

  private CategoryEntity category;

  @BeforeEach
  void setUp() {
    category = new CategoryEntity();
  }

  @Test
  void testPrePersistSetsCreatedAt() {
    category.onCreate();
    assertNotNull(category.getCategory_created_at());
  }

  @Test
  void testSoftDelete() {
    assertNull(category.getCategory_deleted_at());

    category.softDelete();

    assertNotNull(category.getCategory_deleted_at());
  }

  @Test
  void testCollectionsInitialization() {
    assertNotNull(category.getMovements());
    assertNotNull(category.getFixed_expenses());
    assertNotNull(category.getEvents());

    assertTrue(category.getMovements().isEmpty());
    assertTrue(category.getFixed_expenses().isEmpty());
    assertTrue(category.getEvents().isEmpty());
  }

  @Test
  void testSettersAndGetters() {
    UUID id = UUID.randomUUID();
    LocalDateTime now = LocalDateTime.now();

    category.setCategory_id(id);
    category.setCategory_name("Alimentos");
    category.setCategory_type("Gasto");
    category.setCategory_created_at(now);
    category.setCategory_deleted_at(now);

    assertEquals(id, category.getCategory_id());
    assertEquals("Alimentos", category.getCategory_name());
    assertEquals("Gasto", category.getCategory_type());
    assertEquals(now, category.getCategory_created_at());
    assertEquals(now, category.getCategory_deleted_at());
  }
}
