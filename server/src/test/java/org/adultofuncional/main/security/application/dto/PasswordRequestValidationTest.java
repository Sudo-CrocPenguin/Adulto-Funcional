package org.adultofuncional.main.security.application.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class PasswordRequestValidationTest {

  private static final ValidatorFactory FACTORY =
      Validation.buildDefaultValidatorFactory();
  private static final Validator VALIDATOR = FACTORY.getValidator();

  @AfterAll
  static void closeFactory() {
    FACTORY.close();
  }

  @Test
  void acceptsSecretAtCiphertextCapacity() {
    PasswordRequest request = PasswordRequest.builder()
        .applicationName("Aplicación")
        .password("a".repeat(2032))
        .build();

    assertThat(VALIDATOR.validate(request)).isEmpty();
  }

  @Test
  void rejectsUnicodeSecretThatExceedsCiphertextCapacity() {
    PasswordRequest request = PasswordRequest.builder()
        .applicationName("Aplicación")
        .password("🔐".repeat(509))
        .build();

    assertThat(VALIDATOR.validate(request))
        .anyMatch(violation -> violation.getPropertyPath().toString().equals("password"));
  }

  @Test
  void rejectsBlankValuesInCredentialPatch() {
    PasswordUpdateRequest request = PasswordUpdateRequest.builder()
        .applicationName(" ")
        .password("")
        .build();

    assertThat(VALIDATOR.validate(request))
        .extracting(violation -> violation.getPropertyPath().toString())
        .containsExactlyInAnyOrder("applicationName", "password");
  }
}
