package ita.controller;

import ita.aspect.GenerateAuditLog;
import ita.aspect.GenerateRequestLog;
import ita.dto.*;
import ita.entity.Attachment;
import ita.enumeration.ApprovalStatus;
import ita.projection.BaseProjection;
import ita.property.ResponseProperty;
import ita.service.AttachmentService;
import ita.util.ResponseDtoUtil;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;

import static ita.enumeration.EntityType.*;
import static ita.enumeration.OperationType.*;

@RestController
@Slf4j
@RequestMapping("api/attachment")
public class AttachmentController {

    private final AttachmentService attachmentService;
    private final ResponseProperty responseProperty;

    public AttachmentController(AttachmentService attachmentService, ResponseProperty responseProperty) {
        this.attachmentService = attachmentService;
        this.responseProperty = responseProperty;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('READ_ATTACHMENT')")
    @GenerateAuditLog(entityType = ATTACHMENT_TYPE, operationType = READ_OPERATION)
    public ResponseEntity<ResponseDto<Object>> findBySort(BaseSearchCriteriaDto searchCriteria,
                                                          @RequestParam (required = false) ApprovalStatus status
                                                          ) {
        Page<Attachment> attachments = attachmentService.findByFilter(searchCriteria, status);

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getSuccess().getCode().getAttachment(),
                responseProperty.getSuccess().getMessage().getAttachment(), attachments);

        return ResponseEntity.status(200).body(responseDto);
    }

    @GetMapping("{id}")
    @PreAuthorize("hasAuthority('UPDATE_ATTACHMENT')")
    @GenerateAuditLog(entityType = ATTACHMENT_TYPE, operationType = READ_OPERATION)
    public ResponseEntity<ResponseDto<Object>> findById(@PathVariable String id) {
        Attachment attachment = attachmentService.findById(UUID.fromString(id));

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getSuccess().getCode().getAttachment(),
                responseProperty.getSuccess().getMessage().getAttachment(), attachment);

        return ResponseEntity.status(200).body(responseDto);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_ATTACHMENT')")
    @GenerateRequestLog(entityType = ATTACHMENT_TYPE, operationType = ADD_OPERATION)
    @GenerateAuditLog(entityType = ATTACHMENT_TYPE, operationType = ADD_OPERATION)
    public ResponseEntity<ResponseDto<Object>> uploadAttachment(@Valid @ModelAttribute AttachmentRequestDto attachmentRequestDto) throws IOException, MethodArgumentNotValidException, NoSuchMethodException {
        attachmentService.uploadAttachment(attachmentRequestDto);

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getSuccess().getCode().getAttachment(),
                responseProperty.getSuccess().getMessage().getAttachment(), ResponseDtoUtil.generatePayload("Attachment uploaded successfully"));

        return ResponseEntity.status(201).body(responseDto);
    }

    @PutMapping
    @PreAuthorize("hasAuthority('UPDATE_ATTACHMENT')")
    @GenerateRequestLog(entityType = ATTACHMENT_TYPE, operationType = UPDATE_OPERATION)
    @GenerateAuditLog(entityType = ATTACHMENT_TYPE, operationType = UPDATE_OPERATION)
    public ResponseEntity<ResponseDto<Object>> updateAttachment(@Valid @RequestBody AttachmentUpdateDto attachmentUpdateDto) {
        attachmentService.updateAttachment(attachmentUpdateDto);

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getSuccess().getCode().getContact(),
                responseProperty.getSuccess().getMessage().getContact(), ResponseDtoUtil.generatePayload("Attachment updated successfully"));

        return ResponseEntity.status(200).body(responseDto);
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasAuthority('DELETE_ATTACHMENT')")
    @GenerateAuditLog(entityType = ATTACHMENT_TYPE, operationType = DELETE_OPERATION)
    public ResponseEntity<ResponseDto<Object>> deleteAttachment(@PathVariable String id) {
        attachmentService.deleteAttachment(UUID.fromString(id));

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getSuccess().getCode().getAttachment(),
                responseProperty.getSuccess().getMessage().getAttachment(), ResponseDtoUtil.generatePayload("Attachment deleted successfully"));

        return ResponseEntity.status(200).body(responseDto);
    }

    @PostMapping("{id}/approvals")
    @PreAuthorize("hasAuthority('APPROVE_SENDER')")
    @GenerateRequestLog(entityType = SENDER_TYPE, operationType = APPROVE_OPERATION)
    @GenerateAuditLog(entityType = SENDER_TYPE, operationType = APPROVE_OPERATION)
    public ResponseEntity<ResponseDto<Object>> approveSender(@PathVariable String id,
                                                             @Valid @RequestBody ApprovalRequestDto request) {

        Attachment attachment = attachmentService.processAttachment(UUID.fromString(id), request);

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(
                responseProperty.getSuccess().getCode().getSender(),
                responseProperty.getSuccess().getMessage().getSender(), attachment);

        return ResponseEntity.status(200).body(responseDto);
    }
}
