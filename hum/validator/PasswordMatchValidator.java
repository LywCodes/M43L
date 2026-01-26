package ita.validator;

import ita.dto.ChangePasswordRequestDto;
import ita.entity.LocalUser;
import ita.service.UserService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordMatchValidator implements ConstraintValidator<PasswordMatch, ChangePasswordRequestDto> {

    private final PasswordEncoder passwordEncoder;

    public PasswordMatchValidator(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public boolean isValid(ChangePasswordRequestDto changePasswordRequestDto, ConstraintValidatorContext constraintValidatorContext) {
        if (!changePasswordRequestDto.getNewPassword().equals(changePasswordRequestDto.getConfirmPassword())) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate(constraintValidatorContext.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("confirmPassword").addConstraintViolation();

            return false;
        }

        return true;
    }
}
