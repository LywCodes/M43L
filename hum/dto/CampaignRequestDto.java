package ita.dto;

import ita.entity.CampaignHeader;
import ita.validator.UniqueName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

import static ita.enumeration.EntityType.CAMPAIGN_DETAIL_TYPE;

@Data
@AllArgsConstructor
public class CampaignRequestDto {

    @NotBlank(message = "{mandatory.string}")
    @UniqueName(message = "{unique}", value = {CAMPAIGN_DETAIL_TYPE}, field = "name")
    @Size(min = 3, max = 50, message = "{size.string}")
    private String name;

    @NotBlank(message = "{mandatory.string}")
    @Size(min = 3, max = 50, message = "{size.string}")
    private String subject;

    @NotBlank(message = "{mandatory.string}")
    private String senderId;

    @NotBlank(message = "{mandatory.string}")
    private String contentId;
    private String attachmentId;

    @NotBlank(message = "{mandatory.string}")
    private String contactGroupId;

}
