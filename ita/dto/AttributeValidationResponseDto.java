package ita.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AttributeValidationResponseDto {
    private boolean isValid;
    private String message;
    private List<String> missingAttributes;
}
