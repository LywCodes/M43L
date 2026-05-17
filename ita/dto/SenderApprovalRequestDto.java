package ita.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class SenderApprovalRequestDto {

    @NotNull(message = "isApprove field must be filled")
    private Boolean isApprove;

    private String reason;
}
