package ita.dto;

import lombok.*;

import java.util.UUID;

@Setter
@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class AttachmentResponseDto extends AuditResponseDto {

    private UUID id;
    private String name;

}
