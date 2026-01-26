package ita.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@ToString
public class PermissionResponseDto {

    private UUID id;
    private String name;

}
