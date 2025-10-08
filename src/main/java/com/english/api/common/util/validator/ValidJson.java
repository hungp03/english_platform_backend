package com.english.api.common.util.validator;

import com.nimbusds.jose.Payload;
import jakarta.validation.Constraint;

import java.lang.annotation.*;

/**
 * Created by hungpham on 10/8/2025
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = JsonValidator.class)
@Documented
public @interface ValidJson {
    String message() default "Invalid JSON format";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

