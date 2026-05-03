package ita.dto;

import ita.enumeration.CampaignType;
import ita.validator.ScheduledTime;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampaignHeaderUpdateDto {

    @NotNull(message = "{mandatory.uuid}")
    private UUID id;

    @NotBlank(message = "{mandatory.string}")
    @NotNull(message = "{mandatory.string}")
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

    @NotNull(message = "{mandatory.string}")
    private UUID approverId;
}
