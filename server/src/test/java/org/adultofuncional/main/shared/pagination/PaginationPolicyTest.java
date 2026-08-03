package org.adultofuncional.main.shared.pagination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;

import org.adultofuncional.main.shared.exception.BusinessException;
import org.adultofuncional.main.shared.response.ApiErrorCode;
import org.junit.jupiter.api.Test;

/** Verifica límites, valores predeterminados y lista blanca de ordenamiento. */
class PaginationPolicyTest {

  private static final Set<String> ALLOWED_SORTS = Set.of("date", "amount");

  @Test
  void appliesDefaultsAndNormalizesDirection() {
    PageQuery defaults = PaginationPolicy.resolve(
        null, null, null, null, "date", false, ALLOWED_SORTS);
    PageQuery explicit = PaginationPolicy.resolve(
        2, 50, "amount", "asc", "date", false, ALLOWED_SORTS);

    assertThat(defaults).isEqualTo(new PageQuery(0, 20, "date", false));
    assertThat(explicit).isEqualTo(new PageQuery(2, 50, "amount", true));
  }

  @Test
  void rejectsOutOfRangeAndUnknownParameters() {
    assertInvalid(-1, 20, "date", "ASC");
    assertInvalid(0, 0, "date", "ASC");
    assertInvalid(0, 101, "date", "ASC");
    assertInvalid(0, 20, "unknown", "ASC");
    assertInvalid(0, 20, "date", "SIDEWAYS");
  }

  private void assertInvalid(Integer page, Integer size, String sortBy, String direction) {
    assertThatThrownBy(() -> PaginationPolicy.resolve(
        page, size, sortBy, direction, "date", false, ALLOWED_SORTS))
        .isInstanceOfSatisfying(BusinessException.class, exception -> {
          assertThat(exception.getStatus()).isEqualTo(400);
          assertThat(exception.getCode()).isEqualTo(ApiErrorCode.PARAMETER_INVALID);
        });
  }
}
