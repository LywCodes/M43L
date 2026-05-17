package ita.controller;

import ita.aspect.GenerateAuditLog;
import ita.dto.ApprovalLogSearchCriteriaDto;
import ita.dto.ResponseDto;
import ita.entity.ApprovalAuditLog;
import ita.property.ResponseProperty;
import ita.service.ApprovalAuditService;
import ita.util.ResponseDtoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static ita.enumeration.EntityType.AUDIT_TYPE;
import static ita.enumeration.OperationType.READ_OPERATION;

@RestController
@RequestMapping("api/approval-log")
@RequiredArgsConstructor
public class ApprovalLogController {
    private final ApprovalAuditService auditLogService;
    private final ResponseProperty responseProperty;


    @GetMapping
    @PreAuthorize("hasAuthority('READ_APPROVAL_LOG')")
    @GenerateAuditLog(entityType = AUDIT_TYPE, operationType = READ_OPERATION)
    public ResponseEntity<ResponseDto<Object>> getAllLogs(ApprovalLogSearchCriteriaDto criteria) {
        Page<ApprovalAuditLog> logs = auditLogService.findAll(criteria);

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getSuccess().getCode().getAttachment(),
                responseProperty.getSuccess().getMessage().getAttachment(), logs);

        return ResponseEntity.status(200).body(responseDto);
    }

}
