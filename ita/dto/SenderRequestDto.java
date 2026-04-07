package ita.dto;

import ita.validator.UniqueName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

import static ita.enumeration.EntityType.SENDER_TYPE;

@Data
@AllArgsConstructor
public class SenderRequestDto {

    @NotBlank(message = "{mandatory.string}")
    private String name;

    @Email(message = "{email}")
    @NotBlank(message = "{mandatory.string}")
    @UniqueName(message = "{unique}", value = SENDER_TYPE, field = "email")
    private String email;

}
