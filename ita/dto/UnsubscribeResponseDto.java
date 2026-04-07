package ita.dto;

import lombok.*;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UnsubscribeResponseDto {

    private UUID id;
    private String name;
    private String email;
    private Boolean isUnsubscribed;
    private Long unsubscribedAt;
    private Boolean isWhitelisted;
    private Long whitelistedAt;

}
