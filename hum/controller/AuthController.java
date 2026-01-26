package ita.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import ita.aspect.GenerateAuditLog;
import ita.aspect.GenerateRequestLog;
import ita.dto.*;
import ita.entity.LocalUser;
import ita.property.ResponseProperty;
import ita.service.UserService;
import ita.util.ResponseDtoUtil;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import static ita.enumeration.EntityType.*;
import static ita.enumeration.OperationType.*;

@RestController
@RequestMapping("api/auth")
public class AuthController {

    private final UserService userService;
    private final ResponseProperty responseProperty;

    @Autowired
    public AuthController(UserService userService, ResponseProperty responseProperty) {
        this.userService = userService;
        this.responseProperty = responseProperty;
    }

    @PostMapping("login")
    @GenerateRequestLog(entityType = AUTH_TYPE, operationType = LOGIN_OPERATION)
    @GenerateAuditLog(entityType = AUTH_TYPE, operationType = LOGIN_OPERATION)
    public ResponseEntity<ResponseDto<Object>> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        JwtResponseDto jwtResponseDto = userService.authenticateUser(loginRequestDto);

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getSuccess().getCode().getAuth(),
                responseProperty.getSuccess().getMessage().getAuth(), jwtResponseDto);

        return ResponseEntity.status(200).body(responseDto);
    }

    @PutMapping("change-password")
    @GenerateAuditLog(entityType = AUTH_TYPE, operationType = UPDATE_OPERATION)
    public ResponseEntity<ResponseDto<Object>> changePassword(@Valid @RequestBody ChangePasswordRequestDto changePasswordRequestDto) {
        userService.changePassword(changePasswordRequestDto);

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getSuccess().getCode().getAuth(),
                responseProperty.getSuccess().getMessage().getAuth(), ResponseDtoUtil.generatePayload("Password has been changed."));

        return ResponseEntity.status(200).body(responseDto);
    }

}
