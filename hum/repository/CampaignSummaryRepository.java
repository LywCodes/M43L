package ita.repository;

import ita.entity.CampaignSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CampaignSummaryRepository extends JpaRepository<CampaignSummary, UUID> {

    @Query("SELECT cs FROM CampaignSummary cs WHERE cs.day BETWEEN :startDay AND :endDay AND cs.month BETWEEN :startMonth AND :endMonth AND cs.year BETWEEN :startYear AND :endYear")
    List<CampaignSummary> findByDate(Integer startDay, Integer endDay, Integer startMonth, Integer endMonth, Integer startYear, Integer endYear);

}
