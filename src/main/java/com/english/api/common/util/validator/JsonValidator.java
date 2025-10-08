package com.english.api.common.util.validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Created by hungpham on 10/8/2025
 */
public class JsonValidator implements ConstraintValidator<ValidJson, String> {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) return true;
        try {
            mapper.readTree(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

