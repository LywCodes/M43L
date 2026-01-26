package ita.controller;

import ita.aspect.GenerateAuditLog;
import ita.aspect.GenerateRequestLog;
import ita.dto.*;
import ita.entity.LocalRole;
import ita.property.ResponseProperty;
import ita.service.RoleService;
import ita.util.ResponseDtoUtil;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;


import java.util.Optional;
import java.util.UUID;

import static ita.enumeration.EntityType.AUTH_TYPE;
import static ita.enumeration.EntityType.ROLE_TYPE;
import static ita.enumeration.OperationType.*;

@RestController
@RequestMapping("api/role")
public class RoleController {

    private final RoleService roleService;
    private final ResponseProperty responseProperty;

    @Autowired
    public RoleController(RoleService roleService, ResponseProperty responseProperty) {
        this.roleService = roleService;
        this.responseProperty = responseProperty;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('READ_ROLE')")
    @GenerateAuditLog(entityType = ROLE_TYPE, operationType = READ_OPERATION)
    public ResponseEntity<ResponseDto<Object>> findBySort(BaseSearchCriteriaDto searchCriteria) {
        Page<RoleResponseDto> roles = roleService.findAllRole(searchCriteria);

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getSuccess().getCode().getRole(),
                responseProperty.getSuccess().getMessage().getRole(), roles);

        return ResponseEntity.status(200).body(responseDto);
    }

    @GetMapping("{id}")
    @PreAuthorize("hasAuthority('UPDATE_ROLE')")
    @GenerateAuditLog(entityType = ROLE_TYPE, operationType = READ_OPERATION)
    public ResponseEntity<ResponseDto<Object>> findById(@PathVariable String id) {
        LocalRole role = roleService.findById(UUID.fromString(id));

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getSuccess().getCode().getRole(),
                responseProperty.getSuccess().getMessage().getRole(), role);

        return ResponseEntity.status(200).body(responseDto);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_ROLE')")
    @GenerateRequestLog(entityType = ROLE_TYPE, operationType = ADD_OPERATION)
    @GenerateAuditLog(entityType = ROLE_TYPE, operationType = ADD_OPERATION)
    public ResponseEntity<ResponseDto<Object>> addRole(@Valid @RequestBody RoleRequestDto roleRequestDto) {
        LocalRole addedRole = roleService.addRole(roleRequestDto);

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getSuccess().getCode().getRole(),
                responseProperty.getSuccess().getMessage().getRole(), addedRole);

        return ResponseEntity.status(201).body(responseDto);
    }

    @PutMapping
    @PreAuthorize("hasAuthority('UPDATE_ROLE')")
    @GenerateRequestLog(entityType = ROLE_TYPE, operationType = UPDATE_OPERATION)
    @GenerateAuditLog(entityType = ROLE_TYPE, operationType = UPDATE_OPERATION)
    public ResponseEntity<ResponseDto<Object>> updateRole(@Valid @RequestBody RoleUpdateDto roleUpdateDto) {
        LocalRole updatedRole = roleService.updateRole(roleUpdateDto);

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getSuccess().getCode().getRole(),
                responseProperty.getSuccess().getMessage().getRole(), updatedRole);

        return ResponseEntity.status(200).body(responseDto);
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasAuthority('DELETE_ROLE')")
    @GenerateAuditLog(entityType = ROLE_TYPE, operationType = DELETE_OPERATION)
    public ResponseEntity<ResponseDto<Object>> deleteRole(@PathVariable(name = "id") String id) {
        roleService.deleteRole(id);

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getSuccess().getCode().getRole(),
                responseProperty.getSuccess().getMessage().getRole(), ResponseDtoUtil.generateDeleteMessage(id, ROLE_TYPE));

        return ResponseEntity.status(200).body(responseDto);
    }

}
