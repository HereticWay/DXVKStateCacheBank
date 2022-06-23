package com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.validator.annotation;

import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.validator.ExistingUserIdValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Constraint(validatedBy = ExistingUserIdValidator.class)
@Target(ElementType.FIELD)
@Retention(value = RUNTIME)
public @interface ExistingUserId {
    String message() default "There is no user with this id!";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default{};
}
