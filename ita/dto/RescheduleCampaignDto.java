package ita.dto;

import ita.validator.ScheduledTime;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class RescheduleCampaignDto {

    @NotNull(message = "{mandatory.uuid}")
    private UUID campaignId;

    @NotNull(message = "{mandatory.string}")
    @ScheduledTime(message = "{campaign.time}")
    private Long scheduledTime;

}
