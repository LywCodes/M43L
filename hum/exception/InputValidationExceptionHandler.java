package ita.exception;

import ita.dto.ResponseDto;
import ita.property.ResponseProperty;
import ita.util.ResponseDtoUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

import static ita.util.ResponseDtoUtil.generatePayload;

@RestControllerAdvice
@Slf4j
public class InputValidationExceptionHandler {

    private final ResponseProperty responseProperty;

    public InputValidationExceptionHandler(ResponseProperty responseProperty) {
        this.responseProperty = responseProperty;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDto<Object>> handleInputValidationException(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new HashMap<>();
        exception.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getInvalidInput().getCode(),
                responseProperty.getInvalidInput().getMessage(), errors);

        return ResponseEntity.status(400).body(responseDto);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ResponseDto<Object>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException exception) {
        Map<String, String> errors = new HashMap<>();

        errors.put("file", "File upload size exceeded the limit.");

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getInvalidInput().getCode(),
                responseProperty.getInvalidInput().getMessage(), errors);

        return ResponseEntity.status(400).body(responseDto);
    }

}
