package ita.dto;

import ita.validator.UniqueName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

import static ita.enumeration.EntityType.CONTACT_TYPE;

@Data
@AllArgsConstructor
public class ContactRequestDto {

    @NotBlank(message = "{mandatory.string}")
    @Size(min = 3, max = 50, message = "{mandatory.string}")
    private String name;

    @Email(message = "{email}")
    @NotBlank(message = "{mandatory.string}")
    @UniqueName(message = "{unique}", value = CONTACT_TYPE, field = "email")
    private String email;

}
