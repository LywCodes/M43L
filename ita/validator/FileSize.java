package ita.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = FileSizeValidator.class)
public @interface FileSize {

    String message() default "File size can not be more than 10 MB";
    Class<?>[] groups() default{};
    Class<? extends Payload>[] payload() default {};
    long maxSizeInMB() default 10;

}
