package ita.controller;

import ita.aspect.GenerateAuditLog;
import ita.aspect.GenerateRequestLog;
import ita.dto.*;
import ita.entity.Permission;
import ita.property.ResponseProperty;
import ita.service.PermissionService;
import ita.util.ResponseDtoUtil;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static ita.enumeration.EntityType.PERMISSION_TYPE;
import static ita.enumeration.OperationType.*;

@RestController
@Slf4j
@RequestMapping("api/permission")
public class PermissionController {

    private final PermissionService permissionService;
    private final ResponseProperty responseProperty;

    public PermissionController(PermissionService permissionService, ResponseProperty responseProperty) {
        this.permissionService = permissionService;
        this.responseProperty = responseProperty;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('READ_PERMISSION')")
    @GenerateAuditLog(entityType = PERMISSION_TYPE, operationType = READ_OPERATION)
    public ResponseEntity<ResponseDto<Object>> findBySort(BaseSearchCriteriaDto searchCriteria) {
        Page<PermissionResponseDto> permissions = permissionService.findAll(searchCriteria);

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getSuccess().getCode().getPermission(),
                responseProperty.getSuccess().getMessage().getPermission(), permissions);

        return ResponseEntity.status(200).body(responseDto);
    }

    @GetMapping("{id}")
    @PreAuthorize("hasAuthority('UPDATE_PERMISSION')")
    @GenerateAuditLog(entityType = PERMISSION_TYPE, operationType = READ_OPERATION)
    public ResponseEntity<ResponseDto<Object>> findById(@PathVariable String id) {
        PermissionResponseDto permission = permissionService.findDtoById(UUID.fromString(id));

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getSuccess().getCode().getPermission(),
                responseProperty.getSuccess().getMessage().getPermission(), permission);

        return ResponseEntity.status(200).body(responseDto);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_PERMISSION')")
    @GenerateRequestLog(entityType = PERMISSION_TYPE, operationType = ADD_OPERATION)
    @GenerateAuditLog(entityType = PERMISSION_TYPE, operationType = ADD_OPERATION)
    public ResponseEntity<ResponseDto<Object>> addPermission(@Valid @RequestBody PermissionRequestDto permissionRequestDto) {
        Permission permission = permissionService.addPermission(permissionRequestDto);

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getSuccess().getCode().getPermission(),
                responseProperty.getSuccess().getMessage().getPermission(), permission);

        return ResponseEntity.status(201).body(responseDto);
    }

    @PutMapping
    @PreAuthorize("hasAuthority('UPDATE_PERMISSION')")
    @GenerateRequestLog(entityType = PERMISSION_TYPE, operationType = UPDATE_OPERATION)
    @GenerateAuditLog(entityType = PERMISSION_TYPE, operationType = UPDATE_OPERATION)
    public ResponseEntity<ResponseDto<Object>> updatePermission(@Valid @RequestBody PermissionUpdateDto permissionUpdateDto) {
        Permission permission = permissionService.updatePermission(permissionUpdateDto);

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getSuccess().getCode().getPermission(),
                responseProperty.getSuccess().getMessage().getPermission(), permission);

        return ResponseEntity.status(200).body(responseDto);
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasAuthority('DELETE_PERMISSION')")
    @GenerateAuditLog(entityType = PERMISSION_TYPE, operationType = DELETE_OPERATION)
    public ResponseEntity<ResponseDto<Object>> deletePermission(@PathVariable String id) {
        permissionService.deletePermission(UUID.fromString(id));

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getSuccess().getCode().getPermission(),
                responseProperty.getSuccess().getMessage().getPermission(), ResponseDtoUtil.generatePayload("Permission has been deleted"));

        return ResponseEntity.status(200).body(responseDto);
    }

}
