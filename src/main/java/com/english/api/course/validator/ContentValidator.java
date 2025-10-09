package com.english.api.course.validator;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Created by hungpham on 10/10/2025
 */
public class ContentValidator implements ConstraintValidator<ValidJsonContent, JsonNode> {
    @Override
    public boolean isValid(JsonNode node, ConstraintValidatorContext context) {
        if (node == null || !node.isObject()) return false;
        return node.has("type") && node.has("body");
    }
}

