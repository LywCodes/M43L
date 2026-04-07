package ita.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
public class ContactAttributeRequestDto {
    private String name;
    private MultipartFile file;
}
