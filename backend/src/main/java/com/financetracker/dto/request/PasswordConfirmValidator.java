package com.financetracker.dto.request;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Custom validator for password confirmation matching
 */
public class PasswordConfirmValidator implements ConstraintValidator<PasswordConfirm, UserRegistrationRequest> {

    @Override
    public boolean isValid(UserRegistrationRequest value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return value.getPassword() != null && 
               value.getPassword().equals(value.getConfirmPassword());
    }
}
