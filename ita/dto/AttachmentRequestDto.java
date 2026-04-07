package ita.dto;

import ita.validator.FileSize;
import ita.validator.FileType;
import ita.validator.UniqueName;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import static ita.enumeration.EntityType.ATTACHMENT_TYPE;
import static ita.enumeration.FileType.PDF_TYPE;

@Data
@AllArgsConstructor
public class AttachmentRequestDto {

    @NotBlank(message = "{mandatory.string}")
    @UniqueName(message = "{unique}", value = {ATTACHMENT_TYPE}, field = "name")
    private String name;

    @FileSize(message = "{size.file}" , maxSizeInMB = 2)
    @FileType(fileTypes = {PDF_TYPE})
    private MultipartFile file;

}
