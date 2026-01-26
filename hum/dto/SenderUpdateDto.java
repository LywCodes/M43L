package ita.dto;

import ita.validator.UniqueName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import static ita.enumeration.EntityType.SENDER_TYPE;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@AllArgsConstructor
public class SenderUpdateDto extends BaseUpdateDto {

    @Email
    @NotBlank(message = "{mandatory.string}")
    private String email;

}
