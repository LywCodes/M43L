package ita.dto;

import ita.validator.UniqueOnUpdate;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;
import java.util.UUID;

import static ita.enumeration.EntityType.ROLE_TYPE;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@AllArgsConstructor
@UniqueOnUpdate(value = ROLE_TYPE, field = "name")
public class RoleUpdateDto extends BaseUpdateDto {
    private String description;

    @Size(min = 1, message = "{mandatory.list}")
    private List<UUID> permissionIds;

}
