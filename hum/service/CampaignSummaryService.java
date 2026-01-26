package ita.service;

import ita.dto.CampaignGrowthResponseDto;
import ita.dto.GranularDateDto;
import ita.entity.CampaignSummary;
import ita.exception.NotFoundException;
import ita.repository.CampaignSummaryRepository;
import ita.util.CalendarUtil;
import org.apache.commons.lang3.time.CalendarUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

import static ita.enumeration.EntityType.STATISTIC_TYPE;

@Service
public class CampaignSummaryService {

    @Value("${label.text}")
    private String labelTemplate;

    private final CampaignSummaryRepository campaignSummaryRepository;

    public CampaignSummaryService(CampaignSummaryRepository campaignSummaryRepository) {
        this.campaignSummaryRepository = campaignSummaryRepository;
    }

//    public CampaignGrowthResponseDto getCampaignGrowth(String granularity, Long startDate, Long endDate) {
//        List<CampaignSummary> campaignSummaries = findByDate(startDate, endDate);
//
//        if (granularity.compareTo("DAY") == 0) {
//            return campaignGrowthEachDay(campaignSummaries);
//        } else if (granularity.compareTo("MONTH") == 0) {
//            return campaignGrowthEachMonth(campaignSummaries);
//        } else {
//            return campaignGrowthEachYear(campaignSummaries);
//        }
//    }
//
//    private CampaignGrowthResponseDto campaignGrowthEachDay(List<CampaignSummary> campaignSummaries) {
//
//    }
//
//    private CampaignGrowthResponseDto campaignGrowthEachMonth(List<CampaignSummary> campaignSummaries) {
//
//    }
//
//    private CampaignGrowthResponseDto campaignGrowthEachYear(List<CampaignSummary> campaignSummaries) {
//        Long tempSentCount = 0L;
//        Long tempBouncedCount = 0L;
//        Long tempOpenedCount = 0L;
//        Long tempUnsubscribedCount = 0L;
//
//
//        CampaignGrowthResponseDto campaignGrowthResponseDto = new CampaignGrowthResponseDto();
//
//        campaignGrowthResponseDto.setLabel(labelTemplate);
//
//        for (CampaignSummary campaignSummary : campaignSummaries) {
//            tempSentCount += campaignSummary.getSentCount();
//            tempBouncedCount += campaignSummary.getBouncedCount();
//            tempOpenedCount += campaignSummary.getOpenedCount();
//            tempUnsubscribedCount += campaignSummary.getUnsubscribedCount();
//        }
//
//        campaignGrowthResponseDto.set
//    }

    private List<CampaignSummary> findByDate(Long startDate, Long endDate) {
        GranularDateDto granularStartDate = getGranularDate(startDate);
        GranularDateDto granularEndDate = getGranularDate(endDate);

        return campaignSummaryRepository.findByDate(granularStartDate.getDay(), granularEndDate.getDay(),
                granularStartDate.getMonth(), granularEndDate.getMonth(), granularStartDate.getYear(), granularEndDate.getYear());
    }

    private GranularDateDto getGranularDate(Long date) {
        Calendar calendar = CalendarUtil.getCalendarInstance(date);

        GranularDateDto dateDto = new GranularDateDto();

        dateDto.setDay(calendar.get(Calendar.DAY_OF_WEEK));
        dateDto.setMonth(calendar.get(Calendar.MONTH));
        dateDto.setYear(calendar.get(Calendar.YEAR));

        return dateDto;
    }

}
