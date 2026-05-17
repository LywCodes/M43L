package ita.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class CampaignApprovalRequestDto {

    @NotNull(message = "{mandatory.uuid}")
    private UUID campaignHeaderId;

    @NotBlank(message = "{mandatory.string}")
    @Size(min = 1, max = 50, message = "{size.string}")
    private String reason;
}

