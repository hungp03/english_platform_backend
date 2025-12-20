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
        if (node == null || !node.isObject()) {
            return false;
        }

        if (!node.has("type") || !node.has("body")) {
            return false;
        }

        String type = node.get("type").asText();
        JsonNode body = node.get("body");

        if (!body.isObject()) {
            buildErrorMessage(context, "Body must be an object");
            return false;
        }

        return switch (type.toLowerCase()) {
            case "quiz" -> validateQuizContent(body, context);
            case "html" -> validateHtmlContent(body, context);
            default -> {
                buildErrorMessage(context, "Invalid type. Only 'quiz' and 'html' are allowed");
                yield false;
            }
        };
    }

    private boolean validateQuizContent(JsonNode body, ConstraintValidatorContext context) {
        if (!validateAllowedFields(body, context, new String[]{"intro", "questions", "quizzes_content"}, "Quiz body")) {
            return false;
        }

        if (!body.has("questions")) {
            buildErrorMessage(context, "Quiz content must contain 'questions' field");
            return false;
        }

        JsonNode questions = body.get("questions");
        if (!questions.isArray()) {
            buildErrorMessage(context, "Questions must be an array");
            return false;
        }

        for (int i = 0; i < questions.size(); i++) {
            JsonNode question = questions.get(i);
            if (!question.isObject()) {
                buildErrorMessage(context, "Question at index " + i + " must be an object");
                return false;
            }

            if (!validateAllowedFields(question, context, new String[]{"question", "options", "answer"}, "Question at index " + i)) {
                return false;
            }

            if (!question.has("question")) {
                buildErrorMessage(context, "Question at index " + i + " must have 'question' field");
                return false;
            }

            if (!question.has("options")) {
                buildErrorMessage(context, "Question at index " + i + " must have 'options' field");
                return false;
            }

            JsonNode options = question.get("options");
            if (!options.isArray()) {
                buildErrorMessage(context, "Options at question index " + i + " must be an array");
                return false;
            }

            if (!question.has("answer")) {
                buildErrorMessage(context, "Question at index " + i + " must have 'answer' field");
                return false;
            }
        }

        return true;
    }

    private boolean validateHtmlContent(JsonNode body, ConstraintValidatorContext context) {
        if (!validateAllowedFields(body, context, new String[]{"intro", "sections"}, "HTML body")) {
            return false;
        }

        if (!body.has("sections")) {
            buildErrorMessage(context, "HTML content must contain 'sections' field");
            return false;
        }

        JsonNode sections = body.get("sections");
        if (!sections.isArray()) {
            buildErrorMessage(context, "Sections must be an array");
            return false;
        }

        for (int i = 0; i < sections.size(); i++) {
            JsonNode section = sections.get(i);
            if (!section.isObject()) {
                buildErrorMessage(context, "Section at index " + i + " must be an object");
                return false;
            }

            if (!validateAllowedFields(section, context, new String[]{"html"}, "Section at index " + i)) {
                return false;
            }

            if (!section.has("html")) {
                buildErrorMessage(context, "Section at index " + i + " must have 'html' field");
                return false;
            }
        }

        return true;
    }

    private boolean validateAllowedFields(JsonNode node, ConstraintValidatorContext context, String[] allowedFields, String location) {
        var allowedSet = java.util.Set.of(allowedFields);
        var fieldNames = new java.util.ArrayList<String>();
        node.fieldNames().forEachRemaining(fieldNames::add);

        for (String fieldName : fieldNames) {
            if (!allowedSet.contains(fieldName)) {
                buildErrorMessage(context, location + " contains unexpected field: '" + fieldName + "'. Allowed fields: " + String.join(", ", allowedFields));
                return false;
            }
        }
        return true;
    }

    private void buildErrorMessage(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }
}

