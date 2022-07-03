package com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.validator;

import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.validator.annotation.ExistingUserId;
import com.dxvkstatecachebank.dxvkstatecachebank.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Component
public class ExistingUserIdValidator implements ConstraintValidator<ExistingUserId, Long> {
    private final UserService userService;

    @Autowired
    public ExistingUserIdValidator(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean isValid(Long value, ConstraintValidatorContext context) {
        if(value == null)
            return false;

        return userService.existsById(value);
    }
}
