package org.adultofuncional.main.shared.validation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/** Limita el tamaño real UTF-8 que llegará a una columna binaria. */
@Documented
@Target({FIELD, PARAMETER})
@Retention(RUNTIME)
@Constraint(validatedBy = Utf8ByteLengthValidator.class)
public @interface Utf8ByteLength {

  String message() default "El valor excede el tamaño máximo permitido";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  int max();
}
