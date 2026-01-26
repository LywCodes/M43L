package ita.dto;

import ita.validator.FileSize;
import ita.validator.FileType;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import static ita.enumeration.FileType.CSV_TYPE;
import static ita.enumeration.FileType.EXCEL_TYPE;

@Data
@AllArgsConstructor
public class ContactBulkRequestDto {

    @FileSize(message = "{size.file}" , maxSizeInMB = 1)
    @FileType(fileTypes = {CSV_TYPE, EXCEL_TYPE})
    private MultipartFile file;

}
