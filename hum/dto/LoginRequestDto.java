package ita.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

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
