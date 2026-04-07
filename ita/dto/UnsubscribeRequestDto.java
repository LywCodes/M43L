package ita.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UnsubscribeRequestDto {

    @NotBlank(message = "{mandatory.string}")
    private String trackerId;

    @NotBlank(message = "{mandatory.string}")
    private String reason;

}
