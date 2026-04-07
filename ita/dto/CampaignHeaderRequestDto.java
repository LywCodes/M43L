package ita.dto;

import ita.enumeration.CampaignType;
import ita.validator.ScheduledTime;
import ita.validator.UniqueName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static ita.enumeration.EntityType.CAMPAIGN_HEADER_TYPE;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CampaignHeaderRequestDto {

    @NotNull(message = "{mandatory.string}")
    @NotBlank(message = "{mandatory.string}")
    @UniqueName(message = "{unique}", value = {CAMPAIGN_HEADER_TYPE}, field = "name")
    @Size(min = 3, max = 50, message = "{size.string}")
    private String name;

    @NotBlank(message = "{mandatory.string}")
    @Size(min = 3, max = 50, message = "{size.string}")
    private String subject;

    @NotNull(message = "{mandatory.string}")
    private CampaignType type;

    @NotBlank(message = "{mandatory.string}")
    private String senderId;

    @NotBlank(message = "{mandatory.string}")
    private String contentId;

    private String attachmentId;

    @NotBlank(message = "{mandatory.string}")
    private String contactGroupId;

    @NotNull(message = "{mandatory.string}")
    @ScheduledTime(message = "{campaign.time}")
    private Long scheduledTime;

    @NotNull(message = "{mandatory.string}")
    private Boolean isDraft;
}