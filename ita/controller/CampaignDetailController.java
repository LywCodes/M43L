package ita.controller;

import ita.aspect.GenerateAuditLog;
import ita.dto.CampaignDetailResponseDto;
import ita.dto.CampaignHistorySearchCriteria;
import ita.dto.ResponseDto;
import ita.property.ResponseProperty;
import ita.service.CampaignDetailService;
import ita.service.CampaignReportService;
import ita.util.ResponseDtoUtil;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import static ita.enumeration.EntityType.CAMPAIGN_DETAIL_TYPE;
import static ita.enumeration.OperationType.DOWNLOAD_OPERATION;
import static ita.enumeration.OperationType.READ_OPERATION;

@RestController
@RequestMapping("api/campaign/detail")
public class CampaignDetailController {

    private final CampaignDetailService campaignDetailService;
    private final CampaignReportService campaignReportService;
    private final ResponseProperty responseProperty;

    public CampaignDetailController(CampaignDetailService campaignDetailService, CampaignReportService campaignReportService, ResponseProperty responseProperty) {
        this.campaignDetailService = campaignDetailService;
        this.campaignReportService = campaignReportService;
        this.responseProperty = responseProperty;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('READ_CAMPAIGN_HISTORY')")
    @GenerateAuditLog(entityType = CAMPAIGN_DETAIL_TYPE, operationType = READ_OPERATION)
    public ResponseEntity<ResponseDto<Object>> findBySort(CampaignHistorySearchCriteria searchCriteria) {
        Page<CampaignDetailResponseDto> campaignDetails = campaignDetailService.findAllCampaignDetail(searchCriteria);

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getSuccess().getCode().getCampaign(),
                responseProperty.getSuccess().getMessage().getCampaign(), campaignDetails);

        return ResponseEntity.status(200).body(responseDto);
    }

    @GetMapping("download")
    @PreAuthorize("hasAuthority('DOWNLOAD_CAMPAIGN_HISTORY')")
    @GenerateAuditLog(entityType = CAMPAIGN_DETAIL_TYPE, operationType = DOWNLOAD_OPERATION)
    public ResponseEntity<StreamingResponseBody> downloadCampaignHistoryV2(
            @RequestParam(name = "startDate") Long startDate,
            @RequestParam(name = "endDate") Long endDate) {
        StreamingResponseBody stream = campaignReportService.generateCampaignHistory(startDate,endDate);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        httpHeaders.setContentDispositionFormData("attachment", "EMMA Report.xlsx");

        return ResponseEntity.status(200)
                .headers(httpHeaders)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(stream);
    }
}
