package ita.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class ContactGroupMemberResponseDto {
    private UUID id;
    private UUID contactId;
    private String email;
    private String globalName; // Nama asli dari table Contact

    // Atribut dinamis (dari JSONB)
    private Map<String, String> attributes;
}