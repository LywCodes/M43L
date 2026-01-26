package ita.repository;

import ita.entity.CampaignDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CampaignDetailRepository extends JpaRepository<CampaignDetail, UUID>, JpaSpecificationExecutor<CampaignDetail> {

    Optional<CampaignDetail> findByTrackerId(String trackerId);

    @Query("SELECT cd FROM CampaignDetail cd WHERE cd.sentAt BETWEEN :startDate AND :endDate OR cd.softBouncedAt BETWEEN :startDate AND :endDate OR cd.openedAt BETWEEN :startDate AND :endDate OR cd.unsubscribedAt BETWEEN :startDate AND :endDate")
    List<CampaignDetail> findAllByTimeRange(Long startDate, Long endDate);

//    Boolean existsByName(String subject);

}
