package ita.controller;

import com.opencsv.exceptions.CsvValidationException;
import ita.aspect.GenerateAuditLog;
import ita.aspect.GenerateRequestLog;
import ita.dto.ContactAttributeResponseDto;
import ita.dto.ContactBulkRequestDto;
import ita.dto.ResponseDto;
import ita.property.ResponseProperty;
import ita.service.ContactAttributeService;
import ita.util.ResponseDtoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static ita.enumeration.EntityType.CONTACT_GROUP_TYPE;
import static ita.enumeration.OperationType.ADD_OPERATION;
import static ita.enumeration.OperationType.READ_OPERATION;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/contact/attribute")
public class ContactAttributeController {
    private final ContactAttributeService contactAttributeService;
    private final ResponseProperty responseProperty;

    @PostMapping(value = "import", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize("hasAuthority('CREATE_CONTACT_GROUP')")
    @GenerateRequestLog(entityType = CONTACT_GROUP_TYPE, operationType = ADD_OPERATION)
    @GenerateAuditLog(entityType = CONTACT_GROUP_TYPE, operationType = ADD_OPERATION)
    public ResponseEntity<ResponseDto<Object>> importContactGroupAttribute(@ModelAttribute ContactBulkRequestDto contactBulkRequestDto) throws CsvValidationException, IOException {
        Map<String, Object> result = contactAttributeService.importContactAttribute(contactBulkRequestDto);

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(
                responseProperty.getSuccess().getCode().getContactGroup(),
                "Contact Group with Attributes imported successfully",
                result
        );

        return ResponseEntity.status(201).body(responseDto);

    }

    @GetMapping("{groupId}")
    @PreAuthorize("hasAuthority('READ_CONTACT_GROUP')")
    @GenerateAuditLog(entityType = CONTACT_GROUP_TYPE, operationType = READ_OPERATION)
    public ResponseEntity<ResponseDto<Object>> getGroupAttribute(@PathVariable String groupId){
        List<ContactAttributeResponseDto> attributes = contactAttributeService.getAttributesByGroupId(UUID.fromString(groupId));

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(
                responseProperty.getSuccess().getCode().getContactGroup(),
                responseProperty.getSuccess().getMessage().getContactGroup(),
                attributes
        );
        return ResponseEntity.status(200).body(responseDto);
    }


}
