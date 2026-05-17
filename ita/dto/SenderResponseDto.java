package ita.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class SenderResponseDto {

    private UUID id;
    private String name;
    private String email;

}
