package ita.controller;

import ita.aspect.GenerateAuditLog;
import ita.dto.BaseSearchCriteriaDto;
import ita.dto.ResponseDto;
import ita.dto.UnsubscribeRequestDto;
import ita.dto.UnsubscribeResponseDto;
import ita.property.ResponseProperty;
import ita.service.UnsubscribeService;
import ita.util.ResponseDtoUtil;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static ita.enumeration.EntityType.UNSUBSCRIBE_TYPE;
import static ita.enumeration.OperationType.DELETE_OPERATION;
import static ita.enumeration.OperationType.UPDATE_OPERATION;

@RestController
@RequestMapping("api/unsubscribe")
public class UnsubscribeController {

    private final UnsubscribeService unsubscribeService;
    private final ResponseProperty responseProperty;

    public UnsubscribeController(UnsubscribeService unsubscribeService, ResponseProperty responseProperty) {
        this.unsubscribeService = unsubscribeService;
        this.responseProperty = responseProperty;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('READ_UNSUBSCRIBE')")
    public ResponseEntity<ResponseDto<Object>> findBySort(BaseSearchCriteriaDto searchCriteria) {
        Page<UnsubscribeResponseDto> unsubscribeList = unsubscribeService.findAllContact(searchCriteria);

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getSuccess().getCode().getUnsubscribe(),
                responseProperty.getSuccess().getMessage().getUnsubscribe(), unsubscribeList);

        return ResponseEntity.status(200).body(responseDto);
    }

    @DeleteMapping("whitelist/{id}")
    @PreAuthorize("hasAuthority('WHITELIST_UNSUBSCRIBE')")
    @GenerateAuditLog(entityType = UNSUBSCRIBE_TYPE, operationType = UPDATE_OPERATION)
    public ResponseEntity<ResponseDto<Object>> whitelistContact(@PathVariable String id) {
        unsubscribeService.whitelistContact(UUID.fromString(id));

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getSuccess().getCode().getUnsubscribe(),
                responseProperty.getSuccess().getMessage().getUnsubscribe(), ResponseDtoUtil.generatePayload("Whitelist contact success"));

        return ResponseEntity.status(200).body(responseDto);
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasAuthority('DELETE_UNSUBSCRIBE')")
    @GenerateAuditLog(entityType = UNSUBSCRIBE_TYPE, operationType = DELETE_OPERATION)
    public ResponseEntity<ResponseDto<Object>> deleteUnsubscribeHistory(@PathVariable String id) {
        unsubscribeService.deleteUnsubscribeHistory(UUID.fromString(id));

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getSuccess().getCode().getUnsubscribe(),
                responseProperty.getSuccess().getMessage().getUnsubscribe(), ResponseDtoUtil.generatePayload("Delete unsubscribe history success"));

        return ResponseEntity.status(200).body(responseDto);
    }

}
