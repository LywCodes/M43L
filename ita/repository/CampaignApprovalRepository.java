package ita.repository;

import ita.entity.CampaignApprovalHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface CampaignApprovalRepository extends JpaRepository<CampaignApprovalHistory, UUID>, JpaSpecificationExecutor<CampaignApprovalHistory> {
    //List<CampaignApprovalHistory> findByCampaignHeaderIdOrderByCreatedAtDesc(UUID campaignHeaderId);
}
