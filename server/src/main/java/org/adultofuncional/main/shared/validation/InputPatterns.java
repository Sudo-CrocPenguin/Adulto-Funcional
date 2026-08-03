package org.adultofuncional.main.shared.validation;

/** Expresiones compartidas por los contratos HTTP de datos personales. */
public final class InputPatterns {

  /** Letras Unicode con marcas, espacios, apóstrofes y guiones internos. */
  public static final String PERSON_NAME =
      "^[\\p{L}\\p{M}]+(?:[ '\u2019-][\\p{L}\\p{M}]+)*$";

  /** Número internacional E.164: signo más y entre 8 y 15 dígitos. */
  public static final String E164_PHONE = "^\\+[1-9][0-9]{7,14}$";

  /** Campo opcional que, cuando se envía, no puede estar vacío. */
  public static final String NON_BLANK = "(?s).*\\S.*";

  private InputPatterns() {
  }
}
