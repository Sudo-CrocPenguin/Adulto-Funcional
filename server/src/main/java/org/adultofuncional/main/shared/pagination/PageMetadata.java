package org.adultofuncional.main.shared.pagination;

/** Metadatos expuestos junto a la lista paginada en el sobre HTTP. */
public record PageMetadata(
    int number,
    int size,
    long totalElements,
    int totalPages,
    boolean hasNext,
    boolean hasPrevious) {

  /** Construye los metadatos públicos a partir del resultado interno. */
  public static PageMetadata from(PageResult<?> result) {
    return new PageMetadata(
        result.number(),
        result.size(),
        result.totalElements(),
        result.totalPages(),
        result.hasNext(),
        result.hasPrevious());
  }
}
