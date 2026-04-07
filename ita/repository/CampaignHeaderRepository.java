package ita.repository;

import ita.entity.CampaignHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CampaignHeaderRepository extends JpaRepository<CampaignHeader, UUID>, JpaSpecificationExecutor<CampaignHeader> {

    Boolean existsByName(String name);
}