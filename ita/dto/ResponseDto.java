package ita.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResponseDto<T> {

    private ErrorSchema errorSchema;
    private T outputSchema;

}
