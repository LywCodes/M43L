package ita.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorSchema {

    private String code;
    private String message;

}
