package ita.controller;

import ita.aspect.GenerateAuditLog;
import ita.aspect.GenerateRequestLog;
import ita.dto.*;
import ita.entity.Sender;
import ita.enumeration.ApprovalStatus;
import ita.property.ResponseProperty;
import ita.service.SenderService;
import ita.util.ResponseDtoUtil;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static ita.enumeration.EntityType.SENDER_TYPE;
import static ita.enumeration.OperationType.*;

@RestController
@RequestMapping("api/sender")
public class SenderController {

    private final SenderService senderService;
    private final ResponseProperty responseProperty;

    public SenderController(SenderService senderService, ResponseProperty responseProperty) {
        this.senderService = senderService;
        this.responseProperty = responseProperty;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('READ_SENDER')")
    @GenerateAuditLog(entityType = SENDER_TYPE, operationType = READ_OPERATION)
    public ResponseEntity<ResponseDto<Object>> findBySort(BaseSearchCriteriaDto searchCriteria,
                                                          @RequestParam (required = false) ApprovalStatus status) {
        Page<Sender> senders = senderService.findAllSender(searchCriteria, status);

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getSuccess().getCode().getSender(),
                responseProperty.getSuccess().getMessage().getSender(), senders);

        return ResponseEntity.status(200).body(responseDto);
    }

    @GetMapping("projection")
    @PreAuthorize("hasAuthority('READ_SENDER')")
    @GenerateAuditLog(entityType = SENDER_TYPE, operationType = READ_OPERATION)
    public ResponseEntity<ResponseDto<Object>> findProjection(BaseSearchCriteriaDto searchCriteria) {
        Page<SenderResponseDto> senders = senderService.findProjection(searchCriteria);

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getSuccess().getCode().getSender(),
                responseProperty.getSuccess().getMessage().getSender(), senders);

        return ResponseEntity.status(200).body(responseDto);
    }

    @GetMapping("{id}")
    @PreAuthorize("hasAuthority('UPDATE_SENDER')")
    @GenerateAuditLog(entityType = SENDER_TYPE, operationType = READ_OPERATION)
    public ResponseEntity<ResponseDto<Object>> findById(@PathVariable String id) {
        Sender sender = senderService.findById(UUID.fromString(id));

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getSuccess().getCode().getSender(),
                responseProperty.getSuccess().getMessage().getSender(), sender);

        return ResponseEntity.status(200).body(responseDto);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_SENDER')")
    @GenerateRequestLog(entityType = SENDER_TYPE, operationType = ADD_OPERATION)
    @GenerateAuditLog(entityType = SENDER_TYPE, operationType = ADD_OPERATION)
    public ResponseEntity<ResponseDto<Object>> addSender(@Valid @RequestBody SenderRequestDto senderRequestDto) {
        Sender sender = senderService.addSender(senderRequestDto);

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getSuccess().getCode().getSender(),
                responseProperty.getSuccess().getMessage().getSender(), sender);

        return ResponseEntity.status(201).body(responseDto);
    }

    @PutMapping
    @PreAuthorize("hasAuthority('UPDATE_SENDER')")
    @GenerateRequestLog(entityType = SENDER_TYPE, operationType = UPDATE_OPERATION)
    @GenerateAuditLog(entityType = SENDER_TYPE, operationType = UPDATE_OPERATION)
    public ResponseEntity<ResponseDto<Object>> updateSender(@Valid @RequestBody SenderUpdateDto senderUpdateDto) {
        Sender updatedSender = senderService.updateSender(senderUpdateDto);

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getSuccess().getCode().getSender(),
                responseProperty.getSuccess().getMessage().getSender(), updatedSender);

        return ResponseEntity.status(200).body(responseDto);
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasAuthority('DELETE_SENDER')")
    @GenerateAuditLog(entityType = SENDER_TYPE, operationType = DELETE_OPERATION)
    public ResponseEntity<ResponseDto<Object>> deleteSender(@PathVariable String id) {
        senderService.deleteSender(id);

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getSuccess().getCode().getSender(),
                responseProperty.getSuccess().getMessage().getSender(), ResponseDtoUtil.generateDeleteMessage(id, SENDER_TYPE));

        return ResponseEntity.status(200).body(responseDto);
    }

    @PostMapping("{id}/approvals")
    @PreAuthorize("hasAuthority('APPROVE_SENDER')")
    @GenerateRequestLog(entityType = SENDER_TYPE, operationType = APPROVE_OPERATION)
    @GenerateAuditLog(entityType = SENDER_TYPE, operationType = APPROVE_OPERATION)
    public ResponseEntity<ResponseDto<Object>> approveSender(@PathVariable String id,
                                                             @Valid @RequestBody ApprovalRequestDto request) {

        Sender updatedSender = senderService.processApproval(UUID.fromString(id), request);

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(
                responseProperty.getSuccess().getCode().getSender(),
                responseProperty.getSuccess().getMessage().getSender(), updatedSender);

        return ResponseEntity.status(200).body(responseDto);
    }

}
