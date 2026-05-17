package ita.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

@Slf4j
public class HtmlSizeValidator implements ConstraintValidator<HtmlSize, String> {

    private long maxKB;
    private static final long BYTES_PER_KB = 1024L;

    @Override
    public void initialize(HtmlSize constraintAnnotation) {
        this.maxKB = constraintAnnotation.maxKB();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        if (value == null) {
            return true;
        }

        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        long sizeInBytes = bytes.length;
        double sizeInKbDecimal = (double) sizeInBytes / BYTES_PER_KB;
        long maxBytes = maxKB * BYTES_PER_KB;

        log.info("HTML size: {} bytes ({} KB), Max allowed: {} KB",
                sizeInBytes, sizeInKbDecimal, maxKB);

        if (sizeInBytes > maxBytes) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    String.format(
                            "HTML content size %.2f KB exceeds maximum allowed size of %d KB",
                            sizeInKbDecimal,
                            maxKB
                    )
            ).addConstraintViolation();
            return false;
        }

        return true;
    }

}
