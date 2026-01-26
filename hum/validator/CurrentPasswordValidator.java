package ita.validator;

import ita.dto.ChangePasswordRequestDto;
import ita.entity.LocalUser;
import ita.service.UserService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
public class CurrentPasswordValidator implements ConstraintValidator<CurrentPassword, ChangePasswordRequestDto> {

    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    public CurrentPasswordValidator(PasswordEncoder passwordEncoder, UserService userService) {
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
    }

    @Override
    public boolean isValid(ChangePasswordRequestDto changePasswordRequestDto, ConstraintValidatorContext constraintValidatorContext) {
        LocalUser user = userService.findById(changePasswordRequestDto.getId());

        if (!passwordEncoder.matches(changePasswordRequestDto.getCurrentPassword(), user.getPassword())) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate(constraintValidatorContext.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("currentPassword").addConstraintViolation();

            return false;
        }

        return true;
    }
}
