package ita.controller;

import ita.aspect.GenerateAuditLog;
import ita.aspect.GenerateRequestLog;
import ita.dto.BaseSearchCriteriaDto;
import ita.dto.ContactRequestDto;
import ita.dto.ContactUpdateDto;
import ita.dto.ResponseDto;
import ita.entity.Contact;
import ita.property.ResponseProperty;
import ita.service.ContactService;
import ita.util.ResponseDtoUtil;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static ita.enumeration.EntityType.CONTACT_TYPE;
import static ita.enumeration.OperationType.*;

@RestController
@RequestMapping("api/contact")
public class ContactController {

    private final ContactService contactService;
    private final ResponseProperty responseProperty;

    public ContactController(ContactService contactService, ResponseProperty responseProperty) {
        this.contactService = contactService;
        this.responseProperty = responseProperty;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('READ_CONTACT')")
    @GenerateAuditLog(entityType = CONTACT_TYPE, operationType = READ_OPERATION)
    public ResponseEntity<ResponseDto<Object>> findBySort(BaseSearchCriteriaDto searchCriteria) {
        Page<Contact> contacts = contactService.findAllContact(searchCriteria);

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getSuccess().getCode().getContact(),
                responseProperty.getSuccess().getMessage().getContact(), contacts);

        return ResponseEntity.status(200).body(responseDto);
    }

    @GetMapping("{id}")
    @PreAuthorize("hasAuthority('UPDATE_CONTACT')")
    @GenerateAuditLog(entityType = CONTACT_TYPE, operationType = READ_OPERATION)
    public ResponseEntity<ResponseDto<Object>> findById(@PathVariable String id) {
        Contact contact = contactService.findById(UUID.fromString(id));

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getSuccess().getCode().getContact(),
                responseProperty.getSuccess().getMessage().getContact(), contact);

        return ResponseEntity.status(200).body(responseDto);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_CONTACT')")
    @GenerateRequestLog(entityType = CONTACT_TYPE, operationType = ADD_OPERATION)
    @GenerateAuditLog(entityType = CONTACT_TYPE, operationType = ADD_OPERATION)
    public ResponseEntity<ResponseDto<Object>> addContact(@Valid @RequestBody ContactRequestDto contactRequestDto) {
        Contact contact = contactService.addContact(contactRequestDto);

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getSuccess().getCode().getContact(),
                responseProperty.getSuccess().getMessage().getContact(), contact);

        return ResponseEntity.status(201).body(responseDto);
    }

    @PutMapping
    @PreAuthorize("hasAuthority('UPDATE_CONTACT')")
    @GenerateRequestLog(entityType = CONTACT_TYPE, operationType = UPDATE_OPERATION)
    @GenerateAuditLog(entityType = CONTACT_TYPE, operationType = UPDATE_OPERATION)
    public ResponseEntity<ResponseDto<Object>> updateContact(@Valid @RequestBody ContactUpdateDto contactUpdateDto) {
        Contact updatedContact = contactService.updateContact(contactUpdateDto);

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getSuccess().getCode().getContact(),
                responseProperty.getSuccess().getMessage().getContact(), updatedContact);

        return ResponseEntity.status(200).body(responseDto);
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasAuthority('DELETE_CONTACT')")
    @GenerateAuditLog(entityType = CONTACT_TYPE, operationType = DELETE_OPERATION)
    public ResponseEntity<ResponseDto<Object>> deleteContact(@PathVariable String id) {
        contactService.deleteContact(id);

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getSuccess().getCode().getContact(),
                responseProperty.getSuccess().getMessage().getContact(), ResponseDtoUtil.generateDeleteMessage(id, CONTACT_TYPE));

        return ResponseEntity.status(200).body(responseDto);
    }

}
