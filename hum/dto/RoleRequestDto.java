package ita.dto;

import ita.validator.UniqueName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.UUID;

import static ita.enumeration.EntityType.ROLE_TYPE;

@Data
@AllArgsConstructor
public class RoleRequestDto {

    @NotBlank(message = "{mandatory.string}")
    @UniqueName(message = "{unique}", value = {ROLE_TYPE}, field = "name")
    @Size(min = 3, max = 10, message = "{size.string}")
    private String name;
    private String description;

    @Size(min = 1, message = "{mandatory.list}")
    private List<UUID> permissionIds;

}
