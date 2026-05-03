package ita.controller;

import ita.aspect.GenerateAuditLog;
import ita.aspect.GenerateRequestLog;
import ita.dto.*;
import ita.property.ResponseProperty;
import ita.service.CampaignApprovalService;
import ita.service.CampaignHeaderService;
import ita.util.ResponseDtoUtil;
import jakarta.validation.Valid;
import org.quartz.SchedulerException;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.UUID;

import static ita.enumeration.EntityType.CAMPAIGN_HEADER_TYPE;
import static ita.enumeration.OperationType.*;

@RestController
@RequestMapping("/api/campaign/approval")
public class CampaignApprovalController {
    private final CampaignApprovalService campaignApprovalService;
    private final CampaignHeaderService campaignHeaderService;
    private final ResponseProperty responseProperty;

    public CampaignApprovalController(
            CampaignApprovalService campaignApprovalService,
            CampaignHeaderService campaignHeaderService,
            ResponseProperty responseProperty) {

        this.campaignApprovalService = campaignApprovalService;
        this.campaignHeaderService = campaignHeaderService;
        this.responseProperty = responseProperty;
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('APPROVE_CAMPAIGN')")
    @GenerateRequestLog(entityType = CAMPAIGN_HEADER_TYPE, operationType = APPROVE_OPERATION)
    @GenerateAuditLog(entityType = CAMPAIGN_HEADER_TYPE, operationType = APPROVE_OPERATION)
    public ResponseEntity<ResponseDto<Object>>approve(
            @PathVariable UUID id,
            @Valid @RequestBody ApprovalRequestDto request
            ) throws SchedulerException {

        campaignApprovalService.approve(id, request);
        ResponseDto<Object>responseDto = ResponseDtoUtil.generateResponse(
                responseProperty.getSuccess().getCode().getCampaign(),
                responseProperty.getSuccess().getMessage().getCampaign(), "campaign approved"
        );

        return ResponseEntity.status(200).body(responseDto);
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('APPROVE_CAMPAIGN')")
    @GenerateRequestLog(entityType = CAMPAIGN_HEADER_TYPE, operationType = REJECT_OPERATION)
    @GenerateAuditLog(entityType = CAMPAIGN_HEADER_TYPE, operationType = REJECT_OPERATION)
    public ResponseEntity<ResponseDto<Object>>reject(
            @PathVariable UUID id,
            @Valid @RequestBody ApprovalRequestDto request
            )  {
        campaignApprovalService.reject(id, request);

        ResponseDto<Object>responseDto = ResponseDtoUtil.generateResponse(
                responseProperty.getSuccess().getCode().getCampaign(),
                responseProperty.getSuccess().getMessage().getCampaign(), "campaign rejected"
        );

        return ResponseEntity.status(200).body(responseDto);
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('CREATE_CAMPAIGN')")
    @GenerateRequestLog(entityType = CAMPAIGN_HEADER_TYPE, operationType = UPDATE_OPERATION)
    @GenerateAuditLog(entityType = CAMPAIGN_HEADER_TYPE, operationType = UPDATE_OPERATION)
    public ResponseEntity<ResponseDto<Object>>cancel(
            @PathVariable UUID id,
            @Valid @RequestBody ApprovalRequestDto request
            )  {
        campaignApprovalService.cancelRequest(id, request);

        ResponseDto<Object>responseDto =  ResponseDtoUtil.generateResponse(
                responseProperty.getSuccess().getCode().getCampaign(),
                responseProperty.getSuccess().getMessage().getCampaign(),"Campaign successfully cancelled"
        );

        return ResponseEntity.status(200).body(responseDto);
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('APPROVE_CAMPAIGN')")
    @GenerateAuditLog(entityType = CAMPAIGN_HEADER_TYPE, operationType = READ_OPERATION)
    public ResponseEntity<ResponseDto<Object>> getPending() {

        List<CampaignHeaderResponseDto> campaignHeader = campaignHeaderService.getPendingApprovals();

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(
                responseProperty.getSuccess().getCode().getCampaign(),
                responseProperty.getSuccess().getMessage().getCampaign(),
                campaignHeader
        );

        return ResponseEntity.status(200).body(responseDto);
    }

    @GetMapping("/history")
    @PreAuthorize("hasAnyAuthority('APPROVE_CAMPAIGN', 'READ_CAMPAIGN')")
    @GenerateAuditLog(entityType = CAMPAIGN_HEADER_TYPE, operationType = READ_OPERATION)
    public ResponseEntity<ResponseDto<Object>> getApprovalHistoryLogs(ApprovalHistorySearchCriteria criteria) {
        Page<ApprovalLogDto> history = campaignApprovalService.getHistoryLogs(criteria);


        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(
        responseProperty.getSuccess().getCode().getCampaign(),
        responseProperty.getSuccess().getMessage().getCampaign(), history
        );

        return ResponseEntity.status(200).body(responseDto);
    }
}
