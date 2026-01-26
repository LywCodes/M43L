package ita.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@AllArgsConstructor
public class RoleUpdateDto extends BaseUpdateDto {
    private String description;

    @Size(min = 1, message = "{mandatory.list}")
    private List<UUID> permissionIds;

}
