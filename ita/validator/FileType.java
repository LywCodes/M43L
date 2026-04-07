package ita.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = FileTypeValidator.class)
public @interface FileType {

    String message() default "Wrong file type";
    Class<?>[] groups() default{};
    Class<? extends Payload>[] payload() default {};

    ita.enumeration.FileType[] fileTypes();

}
