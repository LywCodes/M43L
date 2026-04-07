package ita.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactAttributeResponseDto {
    private UUID id;
    private  UUID contactId;
    private String email;
    private String name;
    private Map<String,String> attributes;
}
