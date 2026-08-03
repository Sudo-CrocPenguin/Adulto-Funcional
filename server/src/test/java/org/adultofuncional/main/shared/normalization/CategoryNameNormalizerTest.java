package org.adultofuncional.main.shared.normalization;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CategoryNameNormalizerTest {

  @Test
  void appliesNfkcWhitespaceAndLocaleNeutralCase() {
    String decomposed = "  Cafe\u0301\u00A0\t HOGAR  ";

    assertThat(CategoryNameNormalizer.normalize(decomposed)).isEqualTo("café hogar");
  }
}
