package ita.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CampaignGrowthResponseDto {

    private String label;
    private List<String> labels;
    private List<Long> data;

}
