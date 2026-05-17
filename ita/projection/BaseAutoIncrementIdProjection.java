package ita.projection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class BaseAutoIncrementIdProjection {

    private UUID id;
    private Long autoIncrementId;
    private String name;
    private String html;

}
