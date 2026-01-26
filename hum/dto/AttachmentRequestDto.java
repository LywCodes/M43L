package ita.dto;

import ita.validator.FileSize;
import ita.validator.FileType;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import static ita.enumeration.FileType.PDF_TYPE;

@Data
@AllArgsConstructor
public class AttachmentRequestDto {
    @FileSize(message = "{size.file}" , maxSizeInMB = 2)
    @FileType(fileTypes = {PDF_TYPE})
    private MultipartFile file;

}
