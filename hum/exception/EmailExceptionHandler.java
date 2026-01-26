package ita.exception;

import ita.dto.ResponseDto;
import ita.property.ResponseProperty;
import ita.util.ResponseDtoUtil;
import jakarta.mail.MessagingException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static ita.util.ResponseDtoUtil.generatePayload;

@ControllerAdvice(basePackages = "ita.controller")
public class EmailExceptionHandler {

    private final ResponseProperty responseProperty;

    public EmailExceptionHandler(ResponseProperty responseProperty) {
        this.responseProperty = responseProperty;
    }

    @ExceptionHandler(MessagingException.class)
    public ResponseEntity<ResponseDto<Object>> handleMessagingException(MessagingException exception) {
        ResponseDto<Object> response = ResponseDtoUtil.generateResponse(responseProperty.getInvalidInput().getCode(),
                responseProperty.getInvalidInput().getMessage(), generatePayload(exception.getMessage()));

        return ResponseEntity.status(401).body(response);
    }
}
