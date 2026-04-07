package ita.dto;

import ita.validator.UniqueOnUpdate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;
import java.util.UUID;

import static ita.enumeration.EntityType.USER_TYPE;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@AllArgsConstructor
@UniqueOnUpdate(value = USER_TYPE, field = "username")
public class UserUpdateDto extends BaseUpdateDto {

    @NotBlank(message = "{mandatory.string}")
    @Size(min = 3, max = 50, message = "{size.string}")
    private String username;

    @Size(min = 1, message = "{mandatory.list}")
    private List<UUID> roleIds;

}
