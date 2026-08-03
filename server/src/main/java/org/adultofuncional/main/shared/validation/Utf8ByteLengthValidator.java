package org.adultofuncional.main.shared.validation;

import java.nio.charset.StandardCharsets;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/** Valida bytes UTF-8, no unidades UTF-16 de {@link String#length()}. */
public class Utf8ByteLengthValidator
    implements ConstraintValidator<Utf8ByteLength, String> {

  private int maximum;

  @Override
  public void initialize(Utf8ByteLength annotation) {
    maximum = annotation.max();
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    return value == null || value.getBytes(StandardCharsets.UTF_8).length <= maximum;
  }
}
