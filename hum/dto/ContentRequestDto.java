package ita.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ContentRequestDto {

    @Size(min = 3, max = 50, message = "{size.string}")
    @NotBlank(message = "{mandatory.string}")
    private String name;

    private String html;

    @NotNull(message = "{mandatory.integer}")
    private Integer numberOfParam;

}
