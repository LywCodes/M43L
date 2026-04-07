package ita.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginRequestDto {

    @NotBlank(message = "{mandatory.string}")
    @Size(min = 3, max = 50, message = "{size.string}")
    private String username;

    @NotBlank(message = "{mandatory.string}")
    private String password;

    @Override
    public String toString() {
        return "User(" + "username=" + username + ')';
    }

}
