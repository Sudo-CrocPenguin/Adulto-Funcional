package org.adultofuncional.main.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.adultofuncional.main.account.application.dto.UpdateAccountRequest;
import org.adultofuncional.main.auth.application.dto.LoginRequest;
import org.adultofuncional.main.auth.application.dto.RegisterRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class IdentityRequestValidationTest {

  private static final ValidatorFactory FACTORY =
      Validation.buildDefaultValidatorFactory();
  private static final Validator VALIDATOR = FACTORY.getValidator();

  @AfterAll
  static void closeValidatorFactory() {
    FACTORY.close();
  }

  @Test
  void acceptsUnicodeNamesE164PhoneAndLongPassphrases() {
    RegisterRequest request = validRegistration()
        .names("Łukasz Ángel")
        .lastnames("D’Ávila-Soto")
        .phone("+573001234567")
        .password("frase extensa y memorable")
        .build();

    assertThat(VALIDATOR.validate(request)).isEmpty();
  }

  @Test
  void rejectsShortNewPasswordAndNonE164Phone() {
    RegisterRequest request = validRegistration()
        .phone("300 123 4567")
        .password("muy-corta")
        .build();

    assertThat(fields(VALIDATOR.validate(request)))
        .containsExactlyInAnyOrder("password", "phone");
  }

  @Test
  void keepsHistoricalPasswordsCompatibleDuringLogin() {
    LoginRequest request = LoginRequest.builder()
        .email("legacy@example.com")
        .password("old-pass")
        .build();

    assertThat(VALIDATOR.validate(request)).isEmpty();
  }

  @Test
  void rejectsEmptyValuesWhenPatchFieldsArePresent() {
    UpdateAccountRequest request = UpdateAccountRequest.builder()
        .names("")
        .lastnames("   ")
        .phone("")
        .email("")
        .build();

    assertThat(fields(VALIDATOR.validate(request)))
        .containsExactlyInAnyOrder("names", "lastnames", "phone", "email");
  }

  private RegisterRequest.RegisterRequestBuilder validRegistration() {
    return RegisterRequest.builder()
        .names("María")
        .lastnames("Pérez")
        .phone("+573001234567")
        .email("maria@example.com")
        .password("correct horse battery");
  }

  private Set<String> fields(Set<? extends ConstraintViolation<?>> violations) {
    return violations.stream()
        .map(violation -> violation.getPropertyPath().toString())
        .collect(java.util.stream.Collectors.toSet());
  }
}
