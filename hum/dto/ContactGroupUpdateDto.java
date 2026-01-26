package ita.dto;

import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class ContactGroupUpdateDto extends BaseUpdateDto {

    @Size(min = 1, message = "{mandatory.list}")
    private List<UUID> contactIds;

}
