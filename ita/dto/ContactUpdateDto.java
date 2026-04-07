package ita.dto;

import ita.validator.UniqueOnUpdate;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import static ita.enumeration.EntityType.CONTACT_TYPE;


@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@UniqueOnUpdate(value = CONTACT_TYPE, field = "email")
public class ContactUpdateDto extends BaseUpdateDto {

    @Email
    @NotBlank(message = "{mandatory.string}")
    private String email;
}
