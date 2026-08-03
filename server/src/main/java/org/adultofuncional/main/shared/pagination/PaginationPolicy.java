package org.adultofuncional.main.shared.pagination;

import java.util.Locale;
import java.util.Set;

import org.adultofuncional.main.shared.exception.BusinessException;
import org.adultofuncional.main.shared.response.ApiErrorCode;

/**
 * Aplica el contrato común de paginación definido por el ADR 0005.
 *
 * <p>La primera página es cero, el tamaño predeterminado es 20 y nunca puede
 * superar 100. Cada endpoint aporta su campo y dirección predeterminados, así
 * como la lista blanca de campos ordenables.</p>
 */
public final class PaginationPolicy {

  public static final int DEFAULT_PAGE = 0;
  public static final int DEFAULT_SIZE = 20;
  public static final int MAX_SIZE = 100;

  private PaginationPolicy() {
  }

  /** Resuelve valores opcionales y rechaza cualquier parámetro inseguro. */
  public static PageQuery resolve(
      Integer page,
      Integer size,
      String sortBy,
      String sortDirection,
      String defaultSort,
      boolean defaultAscending,
      Set<String> allowedSorts) {
    int resolvedPage = page == null ? DEFAULT_PAGE : page;
    int resolvedSize = size == null ? DEFAULT_SIZE : size;
    String resolvedSort = sortBy == null || sortBy.isBlank() ? defaultSort : sortBy;

    if (resolvedPage < 0) {
      throw invalid("La página no puede ser negativa");
    }
    if (resolvedSize < 1 || resolvedSize > MAX_SIZE) {
      throw invalid("El tamaño de página debe estar entre 1 y 100");
    }
    if (!allowedSorts.contains(resolvedSort)) {
      throw invalid("El campo de ordenamiento no es válido");
    }

    boolean ascending = defaultAscending;
    if (sortDirection != null && !sortDirection.isBlank()) {
      String direction = sortDirection.toUpperCase(Locale.ROOT);
      if (!direction.equals("ASC") && !direction.equals("DESC")) {
        throw invalid("La dirección de ordenamiento debe ser ASC o DESC");
      }
      ascending = direction.equals("ASC");
    }

    return new PageQuery(resolvedPage, resolvedSize, resolvedSort, ascending);
  }

  private static BusinessException invalid(String message) {
    return new BusinessException(message, 400, ApiErrorCode.PARAMETER_INVALID);
  }
}
