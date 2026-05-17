package ita.repository;

import ita.entity.CampaignHeader;
import ita.enumeration.CampaignStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface CampaignHeaderRepository extends JpaRepository<CampaignHeader, UUID>, JpaSpecificationExecutor<CampaignHeader> {

//    Boolean existsByName(String name);

    @Query("SELECT COUNT(c) > 0 FROM CampaignHeader c WHERE c.name = :name AND c.status NOT IN :excludedStatuses")
    boolean checkExistsByNameActive(@Param("name") String name, @Param("excludedStatuses") Collection<CampaignStatus> excludedStatuses);

    @Query("SELECT COUNT(c) > 0 FROM CampaignHeader c WHERE c.name = :name AND c.id <> :id AND c.status NOT IN :excludedStatuses")
    boolean checkExistsByNameActiveAndIdNot(@Param("name") String name, @Param("id") UUID id, @Param("excludedStatuses") Collection<CampaignStatus> excludedStatuses);

    default boolean existsByNameActive(String name) {
        return checkExistsByNameActive(name, Arrays.asList(CampaignStatus.CANCELLED, CampaignStatus.REJECTED));
    }

    default boolean existsByNameActiveAndIdNot(String name, UUID id) {
        return checkExistsByNameActiveAndIdNot(name, id, Arrays.asList(CampaignStatus.CANCELLED, CampaignStatus.REJECTED));
    }

    //cron job auto cancel
    @Query("SELECT c FROM CampaignHeader c WHERE c.status = 'WAITING_APPROVAL' " + "AND c.type = 'SCHEDULED' " + "AND c.scheduledTime <= :currentTime")
    List<CampaignHeader>findExpiredScheduledCampaigns(@Param("currentTime") long currentTime);


    @Query("SELECT c FROM CampaignHeader c WHERE c.status = 'WAITING_APPROVAL' AND c.approverId = :approverId")
    List<CampaignHeader> findPendingApprovals(@Param("approverId") UUID approverId);

    //List<CampaignHeader> findAllByStatusAndApproverId(String status, UUID approverId);
}