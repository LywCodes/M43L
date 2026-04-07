package ita.dto;

import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
public class AttachmentResponseDto {

    private UUID id;
    private String name;

}
