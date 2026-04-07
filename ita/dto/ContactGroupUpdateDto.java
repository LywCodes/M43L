package ita.dto;

import ita.validator.UniqueOnUpdate;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;
import java.util.UUID;

import static ita.enumeration.EntityType.CONTACT_GROUP_TYPE;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@UniqueOnUpdate(value = CONTACT_GROUP_TYPE, field = "name")
public class ContactGroupUpdateDto extends BaseUpdateDto {

    @Size(min = 1, message = "{mandatory.list}")
    private List<UUID> contactIds;

}
