package com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.validator.annotation;

import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.validator.ExistingGameIdValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Constraint(validatedBy = ExistingGameIdValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(value = RUNTIME)
public @interface ExistingGameId {
    String message() default "There is no game with this id!";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
