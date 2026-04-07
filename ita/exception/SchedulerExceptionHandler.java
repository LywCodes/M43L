package ita.exception;

import ita.dto.ResponseDto;
import ita.property.ResponseProperty;
import ita.util.ResponseDtoUtil;
import org.quartz.SchedulerException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static ita.util.ResponseDtoUtil.generatePayload;

@ControllerAdvice(basePackages = "ita.controller")
public class SchedulerExceptionHandler {

    private final ResponseProperty responseProperty;

    public SchedulerExceptionHandler(ResponseProperty responseProperty) {
        this.responseProperty = responseProperty;
    }

    @ExceptionHandler(SchedulerException.class)
    public ResponseEntity<ResponseDto<Object>> handleSchedulerException(SchedulerException exception) {
        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getServerError().getCode(),
                responseProperty.getServerError().getMessage(), generatePayload(exception.getMessage()));

        return ResponseEntity.status(500).body(responseDto);
    }

}
