package ita.service;

import ita.dto.CampaignHistoryResponseDto;
import ita.dto.PerformanceResponseDto;
import ita.dto.StatisticSearchCriteriaDto;
import ita.entity.CampaignDetail;
import ita.enumeration.CampaignDetailStatus;
import ita.repository.CampaignDetailRepository;
import ita.specification.StatisticSpecification;
import ita.util.CalendarUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class StatisticService {

    private final CampaignDetailRepository campaignDetailRepository;

    public StatisticService(CampaignDetailRepository campaignDetailRepository) {
        this.campaignDetailRepository = campaignDetailRepository;
    }

    public PerformanceResponseDto getPerformanceStatistic(StatisticSearchCriteriaDto searchCriteria) {
        Specification<CampaignDetail> baseSpecification = StatisticSpecification.nameLike(searchCriteria.getCampaignName());

        CampaignDetailStatus[] statuses = {
                CampaignDetailStatus.SENT,
                CampaignDetailStatus.OPENED,
                CampaignDetailStatus.CLICKED,
                CampaignDetailStatus.UNSUBSCRIBED,
                CampaignDetailStatus.SOFT_BOUNCED,
                CampaignDetailStatus.HARD_BOUNCED
        };

        PerformanceResponseDto performanceResponseDto = new PerformanceResponseDto();

        for (CampaignDetailStatus status : statuses) {
            Specification<CampaignDetail> specification = baseSpecification.and(StatisticSpecification.statusEqual(status, searchCriteria.getStartDate(), searchCriteria.getEndDate()));
            switch (status) {
                case SENT:
                    performanceResponseDto.setSentCount(campaignDetailRepository.count(specification));
                    break;
                case OPENED:
                    performanceResponseDto.setOpenedCount(campaignDetailRepository.count(specification));
                    break;
                case CLICKED:
                    performanceResponseDto.setClickedCount(campaignDetailRepository.count(specification));
                    break;
                case UNSUBSCRIBED:
                    performanceResponseDto.setUnsubscribedCount(campaignDetailRepository.count(specification));
                    break;
                case SOFT_BOUNCED:
                    performanceResponseDto.setSoftBouncedCount(campaignDetailRepository.count(specification));
                    break;
                case HARD_BOUNCED:
                    performanceResponseDto.setHardBouncedCount(campaignDetailRepository.count(specification));
                    break;
                default:
                    log.warn("unhandled CampaignDetailStatus found: {}. No count will be performed.", status);
            }
        }

        return performanceResponseDto;
    }

    public CampaignHistoryResponseDto getCampaignHistory(StatisticSearchCriteriaDto searchCriteria) {
        Calendar startDateCalendar = CalendarUtil.getCalendarInstance(searchCriteria.getStartDate());
        Calendar endDateCalendar = CalendarUtil.getCalendarInstance(searchCriteria.getEndDate());

        int startMonth = startDateCalendar.get(Calendar.MONTH);
        int endMonth = endDateCalendar.get(Calendar.MONTH);
        int endYear = endDateCalendar.get(Calendar.YEAR);

        CampaignHistoryResponseDto campaignHistoryResponseDto = new CampaignHistoryResponseDto();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

        campaignHistoryResponseDto.setLabel(String.format("Campaign History from %s to %s", simpleDateFormat.format(new Date(searchCriteria.getStartDate())), simpleDateFormat.format(new Date(searchCriteria.getEndDate()))));

        List<String> labels = new ArrayList<>();
        List<Long> datas = new ArrayList<>();

        for (int i = startMonth; i <= endMonth; i++) {
            YearMonth yearMonth = YearMonth.of(endYear, i+1);

            long startMillis = yearMonth.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
            long endMillis = yearMonth.atEndOfMonth().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();

            labels.add(yearMonth.getMonth().name().substring(0, 3));

            Specification<CampaignDetail> sentSpecification = Specification.allOf(StatisticSpecification.nameLike(searchCriteria.getCampaignName())
                    .and(StatisticSpecification.statusEqual(CampaignDetailStatus.SENT, startMillis, endMillis))
            );

            datas.add(campaignDetailRepository.count(sentSpecification));
        }

        campaignHistoryResponseDto.setLabels(labels);
        campaignHistoryResponseDto.setDatas(datas);

        return campaignHistoryResponseDto;
    }

}
