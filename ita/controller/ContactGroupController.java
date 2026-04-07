package ita.controller;

import ita.aspect.GenerateAuditLog;
import ita.aspect.GenerateRequestLog;
import ita.dto.*;
import ita.entity.ContactGroup;
import ita.property.ResponseProperty;
import ita.service.ContactGroupService;
import ita.util.ResponseDtoUtil;
import jakarta.validation.Valid;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;

import static ita.enumeration.EntityType.CONTACT_GROUP_TYPE;
import static ita.enumeration.OperationType.*;

@RestController
@RequestMapping("api/contact/group")
public class ContactGroupController {

    private final ContactGroupService contactGroupService;
    private final ResponseProperty responseProperty;

    public ContactGroupController(ContactGroupService contactGroupService, ResponseProperty responseProperty) {
        this.contactGroupService = contactGroupService;
        this.responseProperty = responseProperty;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('READ_CONTACT_GROUP')")
    @GenerateAuditLog(entityType = CONTACT_GROUP_TYPE, operationType = READ_OPERATION)
    public ResponseEntity<ResponseDto<Object>> findBySort(BaseSearchCriteriaDto searchCriteria) {
        Page<ContactGroupResponseDto> contactGroups = contactGroupService.findAll(searchCriteria);

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getSuccess().getCode().getContactGroup(),
                responseProperty.getSuccess().getMessage().getContactGroup(), contactGroups);

        return ResponseEntity.status(200).body(responseDto);
    }

    @GetMapping("{id}")
    @PreAuthorize("hasAuthority('UPDATE_CONTACT_GROUP')")
    @GenerateAuditLog(entityType = CONTACT_GROUP_TYPE, operationType = READ_OPERATION)
    public ResponseEntity<ResponseDto<Object>> findById(@PathVariable String id) {
        ContactGroup contactGroup = contactGroupService.findById(UUID.fromString(id));

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getSuccess().getCode().getContactGroup(),
                responseProperty.getSuccess().getMessage().getContactGroup(), contactGroup);

        return ResponseEntity.status(200).body(responseDto);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_CONTACT_GROUP')")
    @GenerateRequestLog(entityType = CONTACT_GROUP_TYPE, operationType = ADD_OPERATION)
    @GenerateAuditLog(entityType = CONTACT_GROUP_TYPE, operationType = ADD_OPERATION)
    public ResponseEntity<ResponseDto<Object>> addContactGroup(@Valid @RequestBody ContactGroupRequestDto contactGroupRequestDto) {
        ContactGroup contactGroup = contactGroupService.addContactGroup(contactGroupRequestDto);

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getSuccess().getCode().getContact(),
                responseProperty.getSuccess().getMessage().getContact(), contactGroup);

        return ResponseEntity.status(201).body(responseDto);
    }

    @PutMapping
    @PreAuthorize("hasAuthority('UPDATE_CONTACT_GROUP')")
    @GenerateRequestLog(entityType = CONTACT_GROUP_TYPE, operationType = UPDATE_OPERATION)
    @GenerateAuditLog(entityType = CONTACT_GROUP_TYPE, operationType = UPDATE_OPERATION)
    public ResponseEntity<ResponseDto<Object>> updateContactGroup(@Valid @RequestBody ContactGroupUpdateDto contactGroupUpdateDto) {
        ContactGroup updatedContactGroup = contactGroupService.updateContactGroup(contactGroupUpdateDto);

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getSuccess().getCode().getContactGroup(),
                responseProperty.getSuccess().getMessage().getContactGroup(), updatedContactGroup);

        return ResponseEntity.status(200).body(responseDto);
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasAuthority('DELETE_CONTACT_GROUP')")
    @GenerateAuditLog(entityType = CONTACT_GROUP_TYPE, operationType = DELETE_OPERATION)
    public ResponseEntity<ResponseDto<Object>> deleteContactGroup(@PathVariable String id) {
        contactGroupService.deleteContactGroup(id);

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getSuccess().getCode().getContactGroup(),
                responseProperty.getSuccess().getMessage().getContactGroup(), ResponseDtoUtil.generateDeleteMessage(id, CONTACT_GROUP_TYPE));

        return ResponseEntity.status(200).body(responseDto);
    }

    @GetMapping("export/{id}")
    @PreAuthorize("hasAuthority('READ_CONTACT_GROUP')")
    @GenerateAuditLog(entityType = CONTACT_GROUP_TYPE, operationType = DOWNLOAD_OPERATION)
    public ResponseEntity<Resource> exportContactGroup(@PathVariable String id) {
        ByteArrayResource resource = contactGroupService.export(UUID.fromString(id));

        HttpHeaders httpHeaders = new HttpHeaders();

        httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        httpHeaders.setContentDispositionFormData("attachment", "Contact Group " + id + ".xlsx");
        httpHeaders.setContentLength(resource.getByteArray().length);

        return ResponseEntity.status(200).headers(httpHeaders).contentLength(resource.getByteArray().length)
                .contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);
    }

    @PostMapping("bulk")
    @PreAuthorize("hasAuthority('CREATE_CONTACT_GROUP')")
    @GenerateRequestLog(entityType = CONTACT_GROUP_TYPE, operationType = ADD_OPERATION)
    @GenerateAuditLog(entityType = CONTACT_GROUP_TYPE, operationType = ADD_OPERATION)
    public ResponseEntity<ResponseDto<Object>> addContactBulk(@Valid @ModelAttribute ContactBulkRequestDto contactBulkRequestDto) throws IOException, MethodArgumentNotValidException, NoSuchMethodException {
        ContactGroup contactGroup = contactGroupService.addContactBulk(contactBulkRequestDto);

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getSuccess().getCode().getContactGroup(),
                responseProperty.getSuccess().getMessage().getContactGroup(), contactGroup);

        return ResponseEntity.status(201).body(responseDto);
    }

}
