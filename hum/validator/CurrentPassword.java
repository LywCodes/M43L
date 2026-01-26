package ita.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = CurrentPasswordValidator.class)
public @interface CurrentPassword {

    String message() default "";
    Class<?>[] groups() default{};
    Class<? extends Payload>[] payload() default {};

}
