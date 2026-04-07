package ita.validator;

import ita.enumeration.EntityType;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = UniqueOnUpdateValidator.class)
public @interface UniqueOnUpdate {
    String message() default "Field already exists";
    Class<?>[] groups() default{};
    Class<? extends Payload>[] payload() default {};
    EntityType value();
    String field();
}
