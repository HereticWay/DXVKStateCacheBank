package com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.validator.annotation;

import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.validator.ExistingGameIdValidator;

import javax.validation.Constraint;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Constraint(validatedBy = ExistingGameIdValidator.class)
@Retention(value = RUNTIME)
@Target(value = FIELD)
public @interface ExistingGameId {
}
