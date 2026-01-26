package ita.controller;

import ita.aspect.GenerateAuditLog;
import ita.aspect.GenerateRequestLog;
import ita.dto.BaseSearchCriteriaDto;
import ita.dto.ContentRequestDto;
import ita.dto.ResponseDto;
import ita.entity.Contact;
import ita.entity.Content;
import ita.projection.BaseProjection;
import ita.property.ResponseProperty;
import ita.service.ContentService;
import ita.util.ResponseDtoUtil;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

import static ita.enumeration.EntityType.AUTH_TYPE;
import static ita.enumeration.EntityType.CONTENT_TYPE;
import static ita.enumeration.OperationType.*;

@RestController
@RequestMapping("api/content")
public class ContentController {

    private final ContentService contentService;
    private final ResponseProperty responseProperty;

    public ContentController(ContentService contentService, ResponseProperty responseProperty) {
        this.contentService = contentService;
        this.responseProperty = responseProperty;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('READ_CONTENT')")
    @GenerateAuditLog(entityType = CONTENT_TYPE, operationType = READ_OPERATION)
    public ResponseEntity<ResponseDto<Object>> findBySort(BaseSearchCriteriaDto searchCriteria) {
        Page<BaseProjection> contents = contentService.findAllContent(searchCriteria);

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getSuccess().getCode().getContent(),
                responseProperty.getSuccess().getMessage().getContent(), contents);

        return ResponseEntity.status(200).body(responseDto);
    }

    @GetMapping("{id}")
    @PreAuthorize("hasAuthority('UPDATE_CONTENT')")
    public ResponseEntity<ResponseDto<Object>> findById(@PathVariable String id) {
        Content content = contentService.findById(UUID.fromString(id));

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getSuccess().getCode().getContent(),
                responseProperty.getSuccess().getMessage().getContent(), content);

        return ResponseEntity.status(200).body(responseDto);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_CONTENT')")
    @GenerateRequestLog(entityType = CONTENT_TYPE, operationType = ADD_OPERATION)
    @GenerateAuditLog(entityType = CONTENT_TYPE, operationType = ADD_OPERATION)
    public ResponseEntity<ResponseDto<Object>> addContent(@Valid @RequestBody ContentRequestDto contentRequestDto) {
        Content content = contentService.addContent(contentRequestDto);

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getSuccess().getCode().getContent(),
                responseProperty.getSuccess().getMessage().getContent(), content);

        return ResponseEntity.status(201).body(responseDto);
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasAuthority('DELETE_CONTENT')")
    @GenerateAuditLog(entityType = CONTENT_TYPE, operationType = DELETE_OPERATION)
    public ResponseEntity<ResponseDto<Object>> deleteContent(@PathVariable String id) {
        contentService.deleteContent(id);

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getSuccess().getCode().getContent(),
                responseProperty.getSuccess().getMessage().getContent(), ResponseDtoUtil.generateDeleteMessage(id, CONTENT_TYPE));

        return ResponseEntity.status(200).body(responseDto);
    }

}
