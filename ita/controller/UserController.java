package ita.controller;

import ita.aspect.GenerateAuditLog;
import ita.aspect.GenerateRequestLog;
import ita.dto.*;
import ita.entity.LocalUser;
import ita.property.ResponseProperty;
import ita.service.UserService;
import ita.util.ResponseDtoUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static ita.enumeration.EntityType.USER_TYPE;
import static ita.enumeration.OperationType.*;

@RestController
@RequestMapping("api/user")
public class UserController {

    private final UserService userService;
    private final ResponseProperty responseProperty;

    @Autowired
    public UserController(UserService userService, ResponseProperty responseProperty) {
        this.userService = userService;
        this.responseProperty = responseProperty;
    }

    @GetMapping
    @GenerateAuditLog(entityType = USER_TYPE, operationType = READ_OPERATION)
    @PreAuthorize("hasAuthority('READ_USER')")
    public ResponseEntity<ResponseDto<Object>> findBySort(BaseSearchCriteriaDto searchCriteria) {
        Page<UserResponseDto> users = userService.findAllUser(searchCriteria);

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getSuccess().getCode().getUser(),
                responseProperty.getSuccess().getMessage().getUser(), users);

        return ResponseEntity.status(200).body(responseDto);
    }

    @GetMapping("{id}")
    @GenerateAuditLog(entityType = USER_TYPE, operationType = READ_OPERATION)
    @PreAuthorize("hasAuthority('UPDATE_USER')")
    public ResponseEntity<ResponseDto<Object>> findById(@PathVariable String id) {
        LocalUser user = userService.findById(UUID.fromString(id));

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getSuccess().getCode().getUser(),
                responseProperty.getSuccess().getMessage().getUser(), user);

        return ResponseEntity.status(200).body(responseDto);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_USER')")
    @GenerateRequestLog(entityType = USER_TYPE, operationType = ADD_OPERATION)
    @GenerateAuditLog(entityType = USER_TYPE, operationType = ADD_OPERATION)
    public ResponseEntity<ResponseDto<Object>> registerUser(@Valid @RequestBody UserRequestDto userRequestDto) {
        LocalUser user = userService.addUser(userRequestDto);

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getSuccess().getCode().getUser(),
                responseProperty.getSuccess().getMessage().getUser(), user);

        return ResponseEntity.status(201).body(responseDto);
    }

    @PutMapping
    @PreAuthorize("hasAuthority('UPDATE_USER')")
    @GenerateAuditLog(entityType = USER_TYPE, operationType = UPDATE_OPERATION)
    @GenerateRequestLog(entityType = USER_TYPE, operationType = UPDATE_OPERATION)
    public ResponseEntity<ResponseDto<Object>> updateUser(@Valid @RequestBody UserUpdateDto userUpdateDto) {
        LocalUser user = userService.updateUser(userUpdateDto);

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getSuccess().getCode().getUser(),
                responseProperty.getSuccess().getMessage().getUser(), user);

        return ResponseEntity.status(200).body(responseDto);
    }

    @DeleteMapping("{id}")
    @GenerateAuditLog(entityType = USER_TYPE, operationType = DELETE_OPERATION)
    @PreAuthorize("hasAuthority('DELETE_USER')")
    public ResponseEntity<ResponseDto<Object>> deleteRole(@PathVariable(name = "id") String id) {
        userService.deleteUser(id);

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getSuccess().getCode().getUser(),
                responseProperty.getSuccess().getMessage().getUser(), ResponseDtoUtil.generateDeleteMessage(id, USER_TYPE));

        return ResponseEntity.status(200).body(responseDto);
    }

}
