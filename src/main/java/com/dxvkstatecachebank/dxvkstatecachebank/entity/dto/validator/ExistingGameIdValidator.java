package com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.validator;

import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.validator.annotation.ExistingGameId;
import com.dxvkstatecachebank.dxvkstatecachebank.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Component
public class ExistingGameIdValidator implements ConstraintValidator<ExistingGameId, Long> {
    private final GameService gameService;

    @Autowired
    public ExistingGameIdValidator(GameService gameService) {
        this.gameService = gameService;
    }

    @Override
    public boolean isValid(Long value, ConstraintValidatorContext context) {
        if (value == null)
            return false;

        return gameService.existsById(value);
    }
}
