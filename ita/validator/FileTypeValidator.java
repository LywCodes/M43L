package ita.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

import static ita.enumeration.FileType.*;

@Component
@Slf4j
public class FileTypeValidator implements ConstraintValidator<FileType, MultipartFile> {

    private ita.enumeration.FileType[] fileTypes;

    @Override
    public void initialize(FileType constraintAnnotation) {
        this.fileTypes = constraintAnnotation.fileTypes();
    }

    @Override
    public boolean isValid(MultipartFile multipartFile, ConstraintValidatorContext constraintValidatorContext) {
        for (ita.enumeration.FileType fileType : fileTypes) {
            switch (fileType) {
                case PDF_TYPE -> {
                    return Objects.equals(multipartFile.getContentType(), PDF_TYPE.getValue());
                }
                case EXCEL_TYPE -> {
                    return Objects.equals(multipartFile.getContentType(), EXCEL_TYPE.getValue());
                }
                case CSV_TYPE -> {
                    return Objects.equals(multipartFile.getContentType(), CSV_TYPE.getValue());
                }
                default -> {
                    return false;
                }
            }
        }
        return false;
    }
}
