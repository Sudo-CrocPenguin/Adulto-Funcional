package org.adultofuncional.main.finances.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import org.adultofuncional.main.finances.domain.enums.CategoryType;

import com.fasterxml.uuid.Generators;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * Modelo de dominio que representa una categoría dentro del sistema.
 *
 * <p>
 * Las categorías permiten clasificar movimientos financieros
 * (por ejemplo: comida, transporte, salario, etc.).
 *
 * <p>
 * Este modelo encapsula únicamente las invariantes de negocio.
 * Las validaciones de formato pertenecen a los DTOs.
 *
 * @author Jeronimo Ospina Zapata
 * @since 0.0.1
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Category {

  @EqualsAndHashCode.Include
  final UUID id;

  String name;
  CategoryType type;

  final LocalDateTime createdAt;

  /**
   * Constructor privado. Usar métodos de fábrica.
   */
  private Category(UUID id, String name, CategoryType type, LocalDateTime createdAt) {

    if (id != null) {
      validateId(id);
    }

    validateName(name);
    validateType(type);
    validateCreatedAt(createdAt);

    this.id = id;
    this.name = name;
    this.type = type;
    this.createdAt = createdAt;
  }

  /**
   * Fábrica para crear una nueva categoría.
   *
   * <p>
   * Genera el UUID v7 y el {@code createdAt} en el dominio.
   *
   * @param name nombre de la categoría
   * @param type tipo de categoría
   * @return instancia lista para persistir
   */
  public static Category create(String name, CategoryType type) {

    UUID id = Generators.timeBasedEpochGenerator().generate();
    LocalDateTime now = LocalDateTime.now();

    return new Category(id, name, type, now);
  }

  /**
   * Fábrica para reconstituir una categoría desde persistencia.
   *
   * @param id        identificador
   * @param name      nombre
   * @param type      tipo
   * @param createdAt fecha de creación
   * @return instancia reconstituida
   */
  public static Category reconstitute(UUID id, String name,
      CategoryType type, LocalDateTime createdAt) {

    return new Category(id, name, type, createdAt);
  }

  /**
   * Actualiza el nombre de la categoría.
   *
   * @param name nuevo nombre
   */
  public void updateName(String name) {
    validateName(name);
    this.name = name;
  }

  /**
   * Actualiza el tipo de la categoría.
   *
   * @param type nuevo tipo
   */
  public void updateType(CategoryType type) {
    validateType(type);
    this.type = type;
  }

  // ── Invariantes de negocio ────────────────────────────────────────────────

  private static void validateId(UUID id) {
    if (id == null) {
      throw new IllegalArgumentException("Id cannot be null");
    }
  }

  private static void validateName(String name) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Name cannot be null or empty");
    }
  }

  private static void validateType(CategoryType type) {
    if (type == null) {
      throw new IllegalArgumentException("Type cannot be null");
    }
  }

  private static void validateCreatedAt(LocalDateTime createdAt) {
    if (createdAt == null) {
      throw new IllegalArgumentException("CreatedAt cannot be null");
    }
    if (createdAt.isAfter(LocalDateTime.now())) {
      throw new IllegalArgumentException("CreatedAt cannot be in the future");
    }
  }
}
