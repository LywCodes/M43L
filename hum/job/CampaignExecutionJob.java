package ita.job;

import ita.service.CampaignHeaderService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@DisallowConcurrentExecution
public class CampaignExecutionJob implements Job {

    @Autowired
    private CampaignHeaderService campaignHeaderService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String campaignId = context.getJobDetail().getJobDataMap().getString("campaignId");

        try {
            campaignHeaderService.executeCampaign(campaignId);
        } catch (Exception e) {
            throw new JobExecutionException("Failed to execute campaign", e);
        }
    }
}

