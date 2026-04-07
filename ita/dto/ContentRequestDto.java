package ita.dto;

import ita.validator.UniqueName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

import static ita.enumeration.EntityType.CONTENT_TYPE;

@Data
@AllArgsConstructor
public class ContentRequestDto {

    @Size(min = 3, max = 50, message = "{size.string}")
    @NotBlank(message = "{mandatory.string}")
    @UniqueName(message = "{unique}", value = CONTENT_TYPE, field = "name")
    private String name;

    private String html;

    @NotNull(message = "{mandatory.integer}")
    private Integer numberOfParam;

}
