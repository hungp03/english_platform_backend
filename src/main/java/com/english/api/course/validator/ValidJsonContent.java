package com.english.api.course.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by hungpham on 10/10/2025
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ContentValidator.class)
public @interface ValidJsonContent {
    String message() default "Invalid content: must contain 'type' and 'body'";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

