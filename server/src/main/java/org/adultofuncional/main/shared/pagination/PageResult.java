package org.adultofuncional.main.shared.pagination;

import java.util.List;
import java.util.function.Function;

/** Resultado inmutable de una consulta paginada en persistencia. */
public record PageResult<T>(
    List<T> content,
    int number,
    int size,
    long totalElements,
    int totalPages,
    boolean hasNext,
    boolean hasPrevious) {

  /** Protege el contenido frente a modificaciones posteriores. */
  public PageResult {
    content = List.copyOf(content);
  }

  /**
   * Transforma el contenido conservando intactos los metadatos de página.
   *
   * @param mapper conversión aplicada a cada elemento
   * @param <R> tipo de salida
   * @return página con el contenido convertido
   */
  public <R> PageResult<R> map(Function<T, R> mapper) {
    return new PageResult<>(
        content.stream().map(mapper).toList(),
        number,
        size,
        totalElements,
        totalPages,
        hasNext,
        hasPrevious);
  }
}
