package ita.dto;

import ita.validator.UniqueOnUpdate;
import lombok.*;

import static ita.enumeration.EntityType.ATTACHMENT_TYPE;

@Setter
@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@AllArgsConstructor
@UniqueOnUpdate(value = ATTACHMENT_TYPE, field = "name")
public class AttachmentUpdateDto extends BaseUpdateDto {

}
