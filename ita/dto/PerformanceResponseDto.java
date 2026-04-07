package ita.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PerformanceResponseDto {

    private String label;
    private long sentCount;
    private long openedCount;
    private long clickedCount;
    private long softBouncedCount;
    private long hardBouncedCount;
    private long unsubscribedCount;

}
