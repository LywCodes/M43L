package ita.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class ContentResponseDto {

    private UUID id;
    private String name;
    private String html;

}
