package ita.dto;

import ita.entity.LocalRole;
import ita.validator.UniqueName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static ita.enumeration.EntityType.USER_TYPE;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequestDto {

    @NotBlank(message = "{mandatory.string}")
    @Size(min = 3, max = 50, message = "{size.string}")
    private String name;

    @NotBlank(message = "{mandatory.string}")
    @Size(min = 3, max = 50, message = "{size.string}")
    @UniqueName(message = "{unique}", value = USER_TYPE, field = "username")
    private String username;

    @Size(min = 1, message = "{mandatory.list}")
    private List<UUID> roleIds;

}
