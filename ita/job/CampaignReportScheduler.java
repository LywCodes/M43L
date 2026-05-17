package ita.job;

import ita.service.CampaignReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

//@Component
@Slf4j
@RequiredArgsConstructor
public class CampaignReportScheduler {
    private final CampaignReportService campaignReportService;

    @Value("${report.folder.path:./reports/campaigns}")
    private String reportFolderPath;

//    @Scheduled(cron = "0 0 18 * * ?")
//   @Scheduled(initialDelay = 5000, fixedDelay = 3600000) // for testing

    public void generateReport(){

        try{
//          --  for testing --
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime oneMonthAgo = now.minusMonths(3);

            long startTime = oneMonthAgo.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            long endTime = now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

            Path directoryPath = Paths.get(reportFolderPath);

            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
                log.info("Created directory for reports at: {}", directoryPath.toAbsolutePath());
            }

            String dateStr = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"));
            String filePath = Paths.get(reportFolderPath, "TEST_report_campaign_" + dateStr + ".xlsx").toString();

            log.info("Generating report from {} to {} | Target file: {}",
                    oneMonthAgo.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    now.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    filePath);


            campaignReportService.generateDailyReport(startTime, endTime, filePath);

            log.info("TESTING CRON JOB COMPLETED: Report generated successfully.");

//      -- default --
//            LocalDate yesterday = LocalDate.now().minusDays(1);
//
//            long startOfDay = yesterday.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
//            long endOfDay = yesterday.atTime(23, 59, 59, 999_000_000).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
//
//            Path directoryPath = Paths.get(reportFolderPath);
//            if (!Files.exists(directoryPath)) {
//                Files.createDirectories(directoryPath);
//                log.info("Created directory for reports at: {}", directoryPath.toAbsolutePath());
//            }
//
//            String dateStr = yesterday.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
//            String filePath = Paths.get(reportFolderPath, "report_campaign_" + dateStr + ".xlsx").toString();
//
//            log.info("Generating report for date: {} | Target file: {}", dateStr, filePath);
//
//            // (pass parameter time dan path)
//            campaignReportService.generateDailyReport(startOfDay, endOfDay, filePath);
//
//            log.info("CRON JOB COMPLETED: Daily Excel Report generated successfully.");
        } catch (Exception e) {
            log.error("CRON JOB FAILED: Unexpected error during report generation", e);
        }

    }

}
