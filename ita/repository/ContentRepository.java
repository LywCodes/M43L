package ita.repository;

import ita.entity.Content;
import ita.enumeration.ApprovalStatus;
import ita.projection.BaseAutoIncrementIdProjection;
import ita.projection.BaseProjection;
import ita.projection.ContentProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ContentRepository extends JpaRepository<Content, UUID>, JpaSpecificationExecutor<Content> {

    @Query(value = "SELECT id, auto_increment_id, name, html, approval_status, created_by, created_at, updated_by, updated_at," +
            "approved_by, approved_at, rejected_by, rejected_at, rejection_reason FROM Content WHERE LOWER(name) LIKE LOWER(CONCAT('%', :name, '%') AND status = :status)", nativeQuery = true)
    Page<ContentProjection> findProjection (Pageable pageable, String name, ApprovalStatus status);

    @Query(value = "SELECT id, auto_increment_id, name, html FROM Content WHERE LOWER(name) LIKE LOWER(CONCAT('%', :name, '%'))", nativeQuery = true)
    Page<BaseAutoIncrementIdProjection> findBaseProjection(Pageable pageable, String name);

    @Query("SELECT COUNT(c) > 0 FROM Content c WHERE c.name = :name AND c.approvalStatus IN :statuses")
    Boolean checkExistByNameActive(
            @Param("name") String name,
            @Param("statuses") List<ApprovalStatus> statuses
    );

    @Query("SELECT COUNT(c) > 0 FROM Content c WHERE c.name = :name AND c.id <> :id AND c.approvalStatus IN :statuses")
    Boolean checkExistByNameActiveAndIdNot(
            @Param("name") String name,
            @Param("id") UUID id, @Param("statuses") Collection<ApprovalStatus> statuses
    );

    default Boolean existByNameActive(String name) {
        return checkExistByNameActive(name, Arrays.asList(ApprovalStatus.PENDING, ApprovalStatus.APPROVED)
        );
    }

    Boolean existsByName(String name);

}
