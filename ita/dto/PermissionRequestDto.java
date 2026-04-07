package ita.dto;

import ita.validator.UniqueName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static ita.enumeration.EntityType.PERMISSION_TYPE;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PermissionRequestDto {

    @NotBlank(message = "{mandatory.string}")
    @UniqueName(message = "{unique}", value = {PERMISSION_TYPE}, field = "name")
    @Size(min = 3, max = 50, message = "{size.string}")
    private String name;

}
