package ita.repository;

import ita.entity.CampaignDetail;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CampaignDetailRepository extends JpaRepository<CampaignDetail, UUID>, JpaSpecificationExecutor<CampaignDetail> {

    Optional<CampaignDetail> findByTrackerId(String trackerId);

    @Query("SELECT cd FROM CampaignDetail cd WHERE cd.sentAt BETWEEN :startDate AND :endDate OR cd.softBouncedAt BETWEEN :startDate AND :endDate OR cd.openedAt BETWEEN :startDate AND :endDate OR cd.unsubscribedAt BETWEEN :startDate AND :endDate")
    List<CampaignDetail> findAllByTimeRange(Long startDate, Long endDate);

    @Query("SELECT DISTINCT cd FROM CampaignDetail cd WHERE (cd.sentAt BETWEEN :startDate AND :endDate)" +
            " OR (cd.softBouncedAt BETWEEN :startDate AND :endDate)" +
            " OR (cd.openedAt BETWEEN :startDate AND :endDate)" +
            " OR (cd.unsubscribedAt BETWEEN :startDate AND :endDate)" +
            " ORDER BY cd.id ASC")
    Slice<CampaignDetail> findSliceByTimeRange(@Param("startDate") Long startDate,@Param("endDate") Long endDate, Pageable pageable);

}
