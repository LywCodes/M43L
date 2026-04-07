package ita.controller;

import ita.aspect.GenerateAuditLog;
import ita.dto.CampaignHistoryResponseDto;
import ita.dto.PerformanceResponseDto;
import ita.dto.ResponseDto;
import ita.dto.StatisticSearchCriteriaDto;
import ita.property.ResponseProperty;
import ita.service.StatisticService;
import ita.util.ResponseDtoUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static ita.enumeration.EntityType.STATISTIC_TYPE;
import static ita.enumeration.OperationType.READ_OPERATION;

@RestController
@RequestMapping("api/statistic")
public class StatisticController {

    private final StatisticService statisticService;
    private final ResponseProperty responseProperty;

    public StatisticController(StatisticService statisticService, ResponseProperty responseProperty) {
        this.statisticService = statisticService;
        this.responseProperty = responseProperty;
    }

    @GetMapping("performance")
    @GenerateAuditLog(entityType = STATISTIC_TYPE, operationType = READ_OPERATION)
    public ResponseEntity<ResponseDto<Object>> getPerformance(StatisticSearchCriteriaDto searchCriteria) {
        PerformanceResponseDto performanceResponseDto = statisticService.getPerformanceStatistic(searchCriteria);

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getSuccess().getCode().getStatistic(),
                responseProperty.getSuccess().getMessage().getStatistic(), performanceResponseDto);

        return ResponseEntity.status(200).body(responseDto);
    }

    @GetMapping("history")
    @GenerateAuditLog(entityType = STATISTIC_TYPE, operationType = READ_OPERATION)
    public ResponseEntity<ResponseDto<Object>> getCampaignHistory(StatisticSearchCriteriaDto searchCriteria) {
        CampaignHistoryResponseDto campaignHistoryResponseDto = statisticService.getCampaignHistory(searchCriteria);

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getSuccess().getCode().getStatistic(),
                responseProperty.getSuccess().getMessage().getStatistic(), campaignHistoryResponseDto);

        return ResponseEntity.status(200).body(responseDto);
    }

}
