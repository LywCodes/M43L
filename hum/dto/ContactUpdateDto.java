package ita.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;


@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class ContactUpdateDto extends BaseUpdateDto{

    @Email
    @NotBlank(message = "{mandatory.string}")
    private String email;
}
