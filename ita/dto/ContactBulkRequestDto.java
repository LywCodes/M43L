package ita.dto;

import ita.validator.FileSize;
import ita.validator.FileType;
import ita.validator.UniqueName;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import static ita.enumeration.EntityType.CONTACT_GROUP_TYPE;
import static ita.enumeration.FileType.CSV_TYPE;
import static ita.enumeration.FileType.EXCEL_TYPE;

@Data
@AllArgsConstructor
public class ContactBulkRequestDto {

    @NotBlank(message = "{mandatory.string}")
    @UniqueName(message = "{unique}", value = {CONTACT_GROUP_TYPE}, field = "name")
    private String name;

    @FileSize(message = "{size.file}" , maxSizeInMB = 1)
    @FileType(fileTypes = {CSV_TYPE, EXCEL_TYPE})
    private MultipartFile file;
}