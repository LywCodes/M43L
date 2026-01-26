package ita.repository;

import ita.entity.CampaignHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CampaignHeaderRepository extends JpaRepository<CampaignHeader, UUID>, JpaSpecificationExecutor<CampaignHeader> {

    Boolean existsByName(String name);

//    Optional<CampaignHeader> findByQuartzJobId(String quartzJobId);

    //  (return PAGE) - Digunakan oleh CampaignSchedulerService
//    @Query("SELECT ch FROM CampaignHeader ch WHERE ch.status = :status")
//    Page<CampaignHeader> findByStatus(@Param("status") CampaignStatus status, Pageable pageable);

    //  (return LIST) - Digunakan oleh SchedulerMonitorService
//    List<CampaignHeader> findAllByStatus(CampaignStatus status);
//
//
//    @Query("SELECT ch FROM CampaignHeader ch WHERE ch.status = 'SCHEDULED' AND ch.scheduledTime <= :currentTime")
//    List<CampaignHeader> findScheduledCampaignsToExecute(@Param("currentTime") Long currentTime);
//
//    @Query("SELECT ch FROM CampaignHeader ch WHERE ch.type = :type")
//    Page<CampaignHeader> findByType(@Param("type") CampaignType type, Pageable pageable);
//
//
//    @Query("SELECT ch FROM CampaignHeader ch WHERE ch.scheduledTime BETWEEN :startTime AND :endTime")
//    List<CampaignHeader> findByScheduledTimeBetween(@Param("startTime") Long startTime,
//                                                    @Param("endTime") Long endTime);
//
//    @Query("SELECT ch FROM CampaignHeader ch WHERE ch.status IN :statuses ORDER BY ch.scheduledTime ASC")
//    List<CampaignHeader> findByStatusInOrderByScheduledTime(@Param("statuses") List<CampaignStatus> statuses);
}