package ita.dto;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CampaignHistoryResponseDto {

    private String label;
    private List<String> labels;
    private List<Long> datas;

}
