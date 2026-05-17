package ita.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = HtmlSizeValidator.class)
public @interface HtmlSize {
    String message() default "HTML content exceeds maximum allowed size";

    long maxKB() default 500;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
