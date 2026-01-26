package ita.dto;

import ita.validator.UniqueName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.UUID;

import static ita.enumeration.EntityType.CONTACT_GROUP_TYPE;
import static ita.enumeration.EntityType.SENDER_TYPE;

@Data
@AllArgsConstructor
public class ContactGroupRequestDto {

    @NotBlank(message = "{mandatory.string}")
    @UniqueName(message = "{unique}", value = CONTACT_GROUP_TYPE, field = "name")
    private String name;

    @Size(min = 1, message = "{mandatory.list}")
    private List<UUID> contactIds;

}
