package ita.exception;

import ita.aspect.GenerateAuditLog;
import ita.dto.ResponseDto;
import ita.property.ResponseProperty;
import ita.util.ResponseDtoUtil;
import jakarta.validation.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static ita.enumeration.EntityType.ERROR_TYPE;
import static ita.enumeration.EntityType.NULL_TYPE;
import static ita.enumeration.OperationType.ADD_OPERATION;
import static ita.util.ResponseDtoUtil.generatePayload;

@ControllerAdvice(basePackages = "ita.controller")
public class InputExceptionHandler {

    private final ResponseProperty responseProperty;

    @Autowired
    public InputExceptionHandler(ResponseProperty responseProperty) {
        this.responseProperty = responseProperty;
    }

    @ExceptionHandler
    @GenerateAuditLog(entityType = ERROR_TYPE, operationType = ADD_OPERATION)
    public ResponseEntity<ResponseDto<Object>> handleValidationException(ValidationException exception) {
        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getInvalidInput().getCode(),
                responseProperty.getInvalidInput().getMessage(), generatePayload(exception.getMessage()));

        return ResponseEntity.status(400).body(responseDto);
    }

}
