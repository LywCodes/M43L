package ita.dto;

import ita.validator.UniqueOnUpdate;
import lombok.*;

import static ita.enumeration.EntityType.PERMISSION_TYPE;

@Setter
@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@AllArgsConstructor
@UniqueOnUpdate(value = PERMISSION_TYPE, field = "name")
public class PermissionUpdateDto extends BaseUpdateDto {

}
