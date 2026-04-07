package ita.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StatisticSearchCriteriaDto {

    private String granularity;
    private Long startDate;
    private Long endDate;
    private String campaignName;

}
