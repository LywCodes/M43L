package ita.exception;

import ita.dto.ResponseDto;
import ita.property.ResponseProperty;
import ita.util.ResponseDtoUtil;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.sql.SQLException;

import static ita.util.ResponseDtoUtil.generatePayload;

@ControllerAdvice(basePackages = "ita.controller")
public class DatabaseExceptionHandler {

    private final ResponseProperty responseProperty;

    public DatabaseExceptionHandler(ResponseProperty responseProperty) {
        this.responseProperty = responseProperty;
    }

    @ExceptionHandler(IncorrectResultSizeDataAccessException.class)
    public ResponseEntity<ResponseDto<Object>> handleIncorrectResultSizeDataAccessException(IncorrectResultSizeDataAccessException exception) {
        ResponseDto<Object> response = ResponseDtoUtil.generateResponse(responseProperty.getInvalidInput().getCode(),
                responseProperty.getInvalidInput().getMessage(), generatePayload(exception.getMessage()));

        return ResponseEntity.status(401).body(response);
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<ResponseDto<Object>> handleSQLException(SQLException exception) {
        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getInvalidInput().getCode(),
                responseProperty.getInvalidInput().getMessage(), generatePayload(exception.getMessage()));

        return ResponseEntity.status(404).body(responseDto);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ResponseDto<Object>> handleDataIntegrityViolationException(DataIntegrityViolationException exception) {
        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getInvalidInput().getCode(),
                responseProperty.getInvalidInput().getMessage(), generatePayload("The value must be unique"));

        return ResponseEntity.status(401).body(responseDto);
    }

}
