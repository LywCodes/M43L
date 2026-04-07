package ita.job;

import ita.entity.CampaignHeader;
import ita.service.CampaignHeaderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class StartUpCampaignRecovery {

    private final CampaignHeaderService campaignHeaderService;

    public StartUpCampaignRecovery(CampaignHeaderService campaignHeaderService) {
        this.campaignHeaderService = campaignHeaderService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void recoverMissedCampaigns() {
        try {
            List<CampaignHeader> missedCampaigns = campaignHeaderService.getScheduledCampaignsToExecute();

            if (missedCampaigns.isEmpty()) {
                return;
            }

            for (CampaignHeader campaign : missedCampaigns) {
                executeSingleCampaignSafely(campaign);
            }

        } catch (Exception e) {
            log.error("Critical error during startup campaign recovery: {}", e.getMessage(), e);
        }
    }

    private void executeSingleCampaignSafely(CampaignHeader campaign) {
        try {
            campaignHeaderService.executeCampaign(campaign.getId().toString());
        } catch (Exception e) {
            log.error("Failed to recover campaign {}: {}", campaign.getId(), e.getMessage(), e);
        }
    }
}
