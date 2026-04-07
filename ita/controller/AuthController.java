package ita.controller;

import ita.aspect.GenerateAuditLog;
import ita.aspect.GenerateRequestLog;
import ita.dto.JwtResponseDto;
import ita.dto.LoginRequestDto;
import ita.dto.ResponseDto;
import ita.property.ResponseProperty;
import ita.service.UserService;
import ita.util.ResponseDtoUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static ita.enumeration.EntityType.AUTH_TYPE;
import static ita.enumeration.OperationType.LOGIN_OPERATION;

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
    public ResponseEntity<ResponseDto<Object>> login(@Valid @RequestBody LoginRequestDto loginRequestDto, HttpServletResponse response) {
        JwtResponseDto jwtResponseDto = userService.authenticateUser(loginRequestDto);

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getSuccess().getCode().getAuth(),
                responseProperty.getSuccess().getMessage().getAuth(), jwtResponseDto);

        return ResponseEntity.status(200).body(responseDto);
    }
}
