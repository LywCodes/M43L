package ita.controller;

import ita.aspect.GenerateAuditLog;
import ita.aspect.GenerateRequestLog;
import ita.dto.*;
import ita.entity.CampaignHeader;
import ita.property.ResponseProperty;
import ita.service.CampaignHeaderService;
import ita.util.ResponseDtoUtil;
import jakarta.validation.Valid;
import org.quartz.SchedulerException;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static ita.enumeration.EntityType.*;
import static ita.enumeration.OperationType.*;

@RestController
@RequestMapping("/api/campaign/header")
public class CampaignHeaderController {

    private final CampaignHeaderService campaignHeaderService;
    private final ResponseProperty responseProperty;

    public CampaignHeaderController(CampaignHeaderService campaignHeaderService, ResponseProperty responseProperty) {
        this.campaignHeaderService = campaignHeaderService;
        this.responseProperty = responseProperty;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_CAMPAIGN')")
    @GenerateRequestLog(entityType = CAMPAIGN_HEADER_TYPE, operationType = ADD_OPERATION)
    @GenerateAuditLog(entityType = CAMPAIGN_HEADER_TYPE, operationType = ADD_OPERATION)
    public ResponseEntity<ResponseDto<Object>> createScheduledCampaign(@Valid @RequestBody CampaignHeaderRequestDto request) {

        campaignHeaderService.createScheduledCampaign(request);

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(
                responseProperty.getSuccess().getCode().getCampaign(),
                responseProperty.getSuccess().getMessage().getCampaign(), "success");

        return ResponseEntity.status(201).body(responseDto);
    }

    @PutMapping
    @PreAuthorize("hasAuthority('UPDATE_CAMPAIGN')")
    @GenerateRequestLog(entityType = CAMPAIGN_HEADER_TYPE, operationType = UPDATE_OPERATION)
    @GenerateAuditLog(entityType = CAMPAIGN_HEADER_TYPE, operationType = UPDATE_OPERATION)
    public ResponseEntity<ResponseDto<Object>> updateCampaignHeader(@Valid @RequestBody CampaignHeaderUpdateDto request) throws SchedulerException, MethodArgumentNotValidException, NoSuchMethodException {
        CampaignHeaderResponseDto campaignHeader = campaignHeaderService.updateCampaignHeader(request);

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getSuccess().getCode().getCampaign(),
                responseProperty.getSuccess().getMessage().getCampaign(), campaignHeader);

        return ResponseEntity.status(201).body(responseDto);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('READ_CAMPAIGN')")
    @GenerateAuditLog(entityType = CAMPAIGN_HEADER_TYPE, operationType = READ_OPERATION)
    public ResponseEntity<ResponseDto<Object>> getAllCampaigns(CampaignHeaderSearchCriteria searchCriteria) {
        Page<CampaignHeader> campaigns = campaignHeaderService.getAllCampaigns(searchCriteria);

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse
                (responseProperty.getSuccess().getCode().getCampaign(),
                responseProperty.getSuccess().getMessage().getCampaign(), campaigns);

        return ResponseEntity.status(200).body(responseDto);
    }

    @GetMapping("{id}")
    @PreAuthorize("hasAuthority('READ_CAMPAIGN')")
    @GenerateAuditLog(entityType = CAMPAIGN_HEADER_TYPE, operationType = READ_OPERATION)
    public ResponseEntity<ResponseDto<Object>> findById(@PathVariable String id) {
        CampaignHeaderResponseDto campaignHeader = campaignHeaderService.findDtoById(UUID.fromString(id));

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getSuccess().getCode().getCampaign(),
                responseProperty.getSuccess().getMessage().getCampaign(), campaignHeader);

        return ResponseEntity.status(200).body(responseDto);
    }

    @DeleteMapping("/cancel/{campaignId}")
    @PreAuthorize("hasAuthority('UPDATE_CAMPAIGN')")
    @GenerateAuditLog(entityType = CAMPAIGN_HEADER_TYPE, operationType = DELETE_OPERATION)
    public ResponseEntity<ResponseDto<Object>> cancelScheduledCampaign(@PathVariable String campaignId) throws SchedulerException {
        CampaignHeader campaignHeader = campaignHeaderService.cancelScheduledCampaign(campaignId);

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getSuccess().getCode().getCampaign(),
                responseProperty.getSuccess().getMessage().getCampaign(), campaignHeader);

        return ResponseEntity.status(200).body(responseDto);
    }

    @PutMapping("/reschedule")
    @PreAuthorize("hasAuthority('UPDATE_CAMPAIGN')")
    @GenerateRequestLog(entityType = CAMPAIGN_HEADER_TYPE, operationType = UPDATE_OPERATION)
    @GenerateAuditLog(entityType = CAMPAIGN_HEADER_TYPE, operationType = UPDATE_OPERATION)
    public ResponseEntity<ResponseDto<Object>> rescheduleCampaign(@Valid @RequestBody RescheduleCampaignDto rescheduleCampaignDto) throws SchedulerException {
        CampaignHeader campaignHeader = campaignHeaderService.rescheduleCampaign(rescheduleCampaignDto);

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getSuccess().getCode().getCampaign(),
                responseProperty.getSuccess().getMessage().getCampaign(), campaignHeader);

        return ResponseEntity.status(200).body(responseDto);
    }

    @DeleteMapping("/delete/{campaignId}")
    @PreAuthorize("hasAuthority('DELETE_CAMPAIGN')")
    public ResponseEntity<ResponseDto<Object>> deleteCampaign(@PathVariable String campaignId) throws SchedulerException {
        campaignHeaderService.deleteCampaign(campaignId);

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getSuccess().getCode().getCampaign(),
                responseProperty.getSuccess().getMessage().getCampaign(), ResponseDtoUtil.generatePayload("Campaign deleted successfully"));

        return ResponseEntity.status(200).body(responseDto);
    }
}