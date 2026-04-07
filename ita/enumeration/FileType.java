package ita.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
@AllArgsConstructor
public enum FileType {

    PDF_TYPE("application/pdf"),
    EXCEL_TYPE("application/vnd.ms-excel"),
    CSV_TYPE("text/csv");

    private final String value;

}
