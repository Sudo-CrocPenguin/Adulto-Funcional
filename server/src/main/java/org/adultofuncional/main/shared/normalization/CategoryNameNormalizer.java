package org.adultofuncional.main.shared.normalization;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/** Normalización canónica compartida para la unicidad de categorías. */
public final class CategoryNameNormalizer {

  private static final Pattern SEPARATORS = Pattern.compile("[\\p{Z}\\s]+");

  private CategoryNameNormalizer() {
  }

  /** Aplica NFKC, unifica separadores, recorta y usa minúsculas neutrales. */
  public static String normalize(String name) {
    if (name == null) {
      throw new IllegalArgumentException("Category name cannot be null");
    }
    String normalized = Normalizer.normalize(name, Normalizer.Form.NFKC);
    normalized = SEPARATORS.matcher(normalized).replaceAll(" ").strip();
    return normalized.toLowerCase(Locale.ROOT);
  }
}
