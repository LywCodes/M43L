package ita.dto;

import ita.enumeration.CampaignDetailStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class CampaignDetailResponseDto {

    private UUID id;
    private String name;
    private CampaignDetailStatus status;
    private String sender;
    private String contact;
    private Long sentAt;
    private Long clickedAt;
    private Long openedAt;
    private Long softBouncedAt;
    private Long hardBouncedAt;
    private Long unsubscribedAt;

}
