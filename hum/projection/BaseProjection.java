package ita.projection;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class BaseProjection {

    private final UUID id;
    private final String name;

}
