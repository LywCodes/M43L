package ita.service;

import ita.dto.CampaignHistoryResponseDto;
import ita.dto.PerformanceResponseDto;
import ita.dto.StatisticSearchCriteriaDto;
import ita.entity.CampaignDetail;
import ita.enumeration.CampaignDetailStatus;
import ita.repository.CampaignDetailRepository;
import ita.specification.StatisticSpecification;
import ita.util.CalendarUtil;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class StatisticService {

    private final CampaignDetailRepository campaignDetailRepository;

    public StatisticService(CampaignDetailRepository campaignDetailRepository) {
        this.campaignDetailRepository = campaignDetailRepository;
    }

    public PerformanceResponseDto getPerformanceStatistic(StatisticSearchCriteriaDto searchCriteria) {
        Specification<CampaignDetail> sentSpecification = Specification.where(StatisticSpecification.nameLike(searchCriteria.getCampaignName()))
                .and(StatisticSpecification.statusEqual(CampaignDetailStatus.SENT, searchCriteria.getStartDate(), searchCriteria.getEndDate()));

        Specification<CampaignDetail> openedSpecification = Specification.where(StatisticSpecification.nameLike(searchCriteria.getCampaignName()))
                .and(StatisticSpecification.statusEqual(CampaignDetailStatus.OPENED, searchCriteria.getStartDate(), searchCriteria.getEndDate()));

        Specification<CampaignDetail> clickedSpecification = Specification.where(StatisticSpecification.nameLike(searchCriteria.getCampaignName()))
                .and(StatisticSpecification.statusEqual(CampaignDetailStatus.CLICKED, searchCriteria.getStartDate(), searchCriteria.getEndDate()));

        Specification<CampaignDetail> softBouncedSpecification = Specification.where(StatisticSpecification.nameLike(searchCriteria.getCampaignName()))
                .and(StatisticSpecification.statusEqual(CampaignDetailStatus.SOFT_BOUNCED, searchCriteria.getStartDate(), searchCriteria.getEndDate()));

        Specification<CampaignDetail> hardBouncedSpecification = Specification.where(StatisticSpecification.nameLike(searchCriteria.getCampaignName()))
                .and(StatisticSpecification.statusEqual(CampaignDetailStatus.HARD_BOUNCED, searchCriteria.getStartDate(), searchCriteria.getEndDate()));

        Specification<CampaignDetail> unsubscribedSpecification = Specification.where(StatisticSpecification.nameLike(searchCriteria.getCampaignName()))
                .and(StatisticSpecification.statusEqual(CampaignDetailStatus.UNSUBSCRIBED, searchCriteria.getStartDate(), searchCriteria.getEndDate()));

        PerformanceResponseDto performanceResponseDto = new PerformanceResponseDto();

        performanceResponseDto.setSentCount(campaignDetailRepository.count(sentSpecification));
        performanceResponseDto.setOpenedCount(campaignDetailRepository.count(openedSpecification));
        performanceResponseDto.setClickedCount(campaignDetailRepository.count(clickedSpecification));
        performanceResponseDto.setUnsubscribedCount(campaignDetailRepository.count(unsubscribedSpecification));
        performanceResponseDto.setSoftBouncedCount(campaignDetailRepository.count(softBouncedSpecification));
        performanceResponseDto.setHardBouncedCount(campaignDetailRepository.count(hardBouncedSpecification));

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

            LocalDate startDateCurrentMonth = yearMonth.atDay(1);
            long startMillis = startDateCurrentMonth.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();

            LocalDate endDateCurrentMonth = yearMonth.atEndOfMonth();
            long endMillis = endDateCurrentMonth.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();

            labels.add(yearMonth.getMonth().name().substring(0, 3));

            Specification<CampaignDetail> sentSpecification = Specification.where(StatisticSpecification.nameLike(searchCriteria.getCampaignName()))
                    .and(StatisticSpecification.statusEqual(CampaignDetailStatus.SENT, startMillis, endMillis));

            datas.add(campaignDetailRepository.count(sentSpecification));
        }

        campaignHistoryResponseDto.setLabels(labels);
        campaignHistoryResponseDto.setDatas(datas);

        return campaignHistoryResponseDto;
    }

}
