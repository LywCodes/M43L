package ita.dto;

import ita.validator.CurrentPassword;
import ita.validator.PasswordMatch;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
@PasswordMatch(message = "{not-match.password}")
@CurrentPassword(message = "{not-match.current}")
public class ChangePasswordRequestDto {

    @NotNull(message = "{mandatory.uuid}")
    private UUID id;

    @Size(min = 3, max = 50, message = "{size.string}")
    @NotBlank(message = "{mandatory.string}")
    private String currentPassword;

    @Size(min = 8, max = 50, message = "{size.string}")
    @NotBlank(message = "{mandatory.string}")
    private String newPassword;

    @Size(min = 8, max = 50, message = "{size.string}")
    @NotBlank(message = "{mandatory.string}")
    private String confirmPassword;

}
