package ita.exception;

import ita.aspect.GenerateAuditLog;
import ita.dto.ResponseDto;
import ita.property.ResponseProperty;
import ita.util.ResponseDtoUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static ita.enumeration.EntityType.ERROR_TYPE;
import static ita.enumeration.OperationType.LOGIN_OPERATION;
import static ita.util.ResponseDtoUtil.generatePayload;

@ControllerAdvice(basePackages = "ita.controller")
public class SecurityExceptionHandler {

    private final ResponseProperty responseProperty;

    public SecurityExceptionHandler(ResponseProperty responseProperty) {
        this.responseProperty = responseProperty;
    }

    @ExceptionHandler(BadCredentialsException.class)
    @GenerateAuditLog(entityType = ERROR_TYPE, operationType = LOGIN_OPERATION)
    public ResponseEntity<ResponseDto<Object>> handleBadCredential(BadCredentialsException exception) {
        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getAccessDenied().getCode().getInvalid(),
                responseProperty.getAccessDenied().getMessage().getInvalid(), generatePayload("Incorrect username and/or password"));

        return ResponseEntity.status(400).body(responseDto);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ResponseDto<Object>> handleAccessDenied(AccessDeniedException exception) {
        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getAccessDenied().getCode().getInvalid(),
                responseProperty.getAccessDenied().getMessage().getInvalid(), generatePayload("Access denied for this feature"));

        return ResponseEntity.status(403).body(responseDto);
    }

}
