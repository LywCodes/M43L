package ita.dto;

import lombok.*;

import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PermissionResponseDto {

    private UUID id;
    private String name;

}
