package ita.exception;

import ita.dto.ResponseDto;
import ita.property.ResponseProperty;
import ita.util.ResponseDtoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.IOException;
import java.util.NoSuchElementException;

import static ita.util.ResponseDtoUtil.generatePayload;

@ControllerAdvice(basePackages = "ita.controller")
public class GeneralExceptionHandler {

    private final ResponseProperty responseProperty;

    @Autowired
    public GeneralExceptionHandler(ResponseProperty responseProperty) {
        this.responseProperty = responseProperty;
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ResponseDto<Object>> handleResourceNotFound(NoSuchElementException exception) {
        ResponseDto<Object> response = ResponseDtoUtil.generateResponse(responseProperty.getNotFound().getCode(),
                responseProperty.getNotFound().getMessage(), generatePayload(exception.getMessage()));

        return ResponseEntity.status(404).body(response);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ResponseDto<Object>> handleNullException(NullPointerException exception) {

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getNotFound().getCode(),
                responseProperty.getNotFound().getMessage(), generatePayload(exception.getMessage()));

        return ResponseEntity.status(404).body(responseDto);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<ResponseDto<Object>> handleIOException(IOException exception) {

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getNotFound().getCode(),
                responseProperty.getNotFound().getMessage(), generatePayload(exception.getMessage()));

        return ResponseEntity.status(404).body(responseDto);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ResponseDto<Object>> handleNotFoundException(NotFoundException exception) {

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getNotFound().getCode(),
                responseProperty.getNotFound().getMessage(), generatePayload(exception.getMessage()));

        return ResponseEntity.status(404).body(responseDto);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ResponseDto<Object>> handleNotFoundException(RuntimeException exception) {

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getServerError().getCode(),
                responseProperty.getServerError().getMessage(), generatePayload(exception.getMessage()));

        return ResponseEntity.status(500).body(responseDto);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseDto<Object>> handleNullException(IllegalArgumentException exception) {

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getInvalidInput().getCode(),
                responseProperty.getInvalidInput().getMessage(), generatePayload(exception.getMessage()));

        return ResponseEntity.status(400).body(responseDto);
    }
}
