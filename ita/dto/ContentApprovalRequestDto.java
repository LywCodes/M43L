package ita.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ContentApprovalRequestDto {
    @NotNull(message = "isApprove field must be filled")
    private Boolean isApproved;

    private String reason;
}
