package ita.job;

import ita.service.CampaignHeaderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;


//v1
//@Component
//@DisallowConcurrentExecution
//@RequiredArgsConstructor
//@Slf4j
//public class CampaignExecutionJob implements Job {
//
//
//    private final CampaignHeaderService campaignHeaderService;
//
//    @Override
//    public void execute(JobExecutionContext context) throws JobExecutionException {
//        String hostname = "unknown";
//        try { hostname = getLocalHost().getHostName();
//            log.info("job executed on instance: {}", hostname);
//        } catch (UnknownHostException e) {
//            //throw new RuntimeException(e);
//            log.warn("job executed on unknown host");
//        }
//
//        String campaignId = context.getJobDetail().getJobDataMap().getString("campaignId");
//
//        log.info("START JOB campaignId: {}" , campaignId);
//
//        try {
//            campaignHeaderService.executeCampaign(campaignId);
//            log.info("SUCCESS JOB campaignId: {}" , campaignId);
//        } catch (Exception e) {
//            log.error("ERROR JOB campaignId: {}" , campaignId, e);
//            throw new JobExecutionException("Failed to execute campaign", e);
//        }
//    }
//}

//v2
@Slf4j
@Component
@RequiredArgsConstructor
@DisallowConcurrentExecution
public class CampaignExecutionJob implements Job {

    private final CampaignHeaderService campaignHeaderService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        logExecutionHost();

        JobDataMap jobDataMap = context.getMergedJobDataMap();
        String campaignId = jobDataMap.getString("campaignId");

        if (campaignId == null || campaignId.isBlank()) {
            log.error("campaignId is missing in JobDataMap");
            throw new JobExecutionException("campaignId is missing in JobDataMap");
        }

        log.info("START JOB campaignId: {}", campaignId);

        try {
            campaignHeaderService.executeCampaign(campaignId);
            log.info("SUCCESS JOB campaignId: {}", campaignId);
        } catch (Exception e) {
            log.error("ERROR JOB campaignId: {}", campaignId, e);
            throw new JobExecutionException("Failed to execute campaign for campaignId=" + campaignId, e);
        }
    }

    private void logExecutionHost() {
        try {
            String hostname = InetAddress.getLocalHost().getHostName();
            log.info("job executed on instance: {}", hostname);
        } catch (UnknownHostException e) {
            log.warn("job executed on unknown host");
        }
    }
}

