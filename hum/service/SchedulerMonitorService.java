package ita.service;

import ita.entity.CampaignHeader;
import ita.enumeration.CampaignDetailStatus;
import ita.enumeration.CampaignStatus;
import ita.repository.CampaignHeaderRepository;
import ita.specification.CampaignHeaderSpecification;
import jakarta.transaction.Transactional;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

import static ita.enumeration.CampaignStatus.IN_PROGRESS;

@Service
public class SchedulerMonitorService {

    private final CampaignHeaderRepository campaignHeaderRepository;
    private final CampaignDetailService campaignDetailService;
    private final Scheduler quartzScheduler;

    public SchedulerMonitorService(CampaignHeaderRepository campaignHeaderRepository,
                                   CampaignDetailService campaignDetailService,
                                   Scheduler quartzScheduler) {
        this.campaignHeaderRepository = campaignHeaderRepository;
        this.campaignDetailService = campaignDetailService;
        this.quartzScheduler = quartzScheduler;
    }

    @Scheduled(fixedRate = 60000)
    public void monitorInProgressCampaigns() throws SchedulerException {
        Specification<CampaignHeader> campaignHeaderSpecification = Specification.where(CampaignHeaderSpecification.statusEqual(IN_PROGRESS));

        List<CampaignHeader> inProgressCampaigns = campaignHeaderRepository.findAll(campaignHeaderSpecification);

        for (CampaignHeader campaign : inProgressCampaigns) {
            checkCampaignCompletion(campaign);
        }
    }

    @Transactional
    public void checkCampaignCompletion(CampaignHeader campaign) throws SchedulerException {
        long sentCount = campaignDetailService.countByStatusAndName(CampaignDetailStatus.SENT, campaign.getName());
        long softBouncedCount = campaignDetailService.countByStatusAndName(CampaignDetailStatus.SOFT_BOUNCED, campaign.getName());

        int expectedEmailCount = campaign.getContactGroup().getContacts().size();

        long processedCount = sentCount + softBouncedCount;

        if (sentCount == expectedEmailCount || processedCount == expectedEmailCount) {
            markCampaignAsCompleted(campaign);

            return;
        }

        if (isOlderThan(campaign.getStartedAt(), 60)) {
            markCampaignAsFailed(campaign, String.format("Email processed %s from %s", processedCount, expectedEmailCount));
        }
    }

    private void markCampaignAsCompleted(CampaignHeader campaign) throws SchedulerException {
        campaign.setStatus(CampaignStatus.SENT);
        campaign.setCompletedAt(System.currentTimeMillis());

        campaignHeaderRepository.save(campaign);

        deleteQuartzJob(campaign);
    }

    private void markCampaignAsFailed(CampaignHeader campaign, String reason) throws SchedulerException {
        campaign.setStatus(CampaignStatus.FAILED);
        campaign.setFailedAt(System.currentTimeMillis());
        campaign.setFailureReason(reason);
        campaignHeaderRepository.save(campaign);

        deleteQuartzJob(campaign);
    }

    private void deleteQuartzJob(CampaignHeader campaign) throws SchedulerException {
        if (campaign.getQuartzJobId() != null) {
            JobKey jobKey = JobKey.jobKey(campaign.getQuartzJobId(), "campaign-jobs");
            if (quartzScheduler.checkExists(jobKey)) {
                quartzScheduler.deleteJob(jobKey);
            }
        }
    }

    private boolean isOlderThan(Long timestamp, int minutes) {
        if (timestamp == null) return false;

        long cutoffTime = System.currentTimeMillis() - (minutes * 60 * 1000L);

        return timestamp < cutoffTime;
    }


    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupOldCampaigns() {

    }


    @Scheduled(cron = "0 0 6 * * *")
    public void generateDailyReport() {

    }


    @Scheduled(fixedRate = 1800000) // 30 minutes
    public void schedulerHealthCheck() {

    }

}