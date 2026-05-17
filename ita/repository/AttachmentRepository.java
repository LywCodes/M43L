package ita.repository;

import ita.entity.Attachment;
import ita.enumeration.ApprovalStatus;
import ita.projection.BaseProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
public interface AttachmentRepository extends JpaRepository<Attachment, UUID>, JpaSpecificationExecutor<Attachment> {

    @Query(value = "SELECT id, name FROM Attachment WHERE LOWER(name) LIKE LOWER(CONCAT('%', :name, '%'))", nativeQuery = true)
    Page<BaseProjection> findProjection (Pageable pageable, String name);

    boolean existsByName(String name);

//    @Query("SELECT COUNT(att) > 0 FROM Attachment att WHERE att.name = :name AND att.id <> :id")
//    boolean existsByNameAndNotId(String name, UUID id);

    @Query("SELECT COUNT(a) > 0 FROM Attachment a WHERE a.name = :name AND a.approvalStatus IN :statuses")
    Boolean checkExistsByNameActive(
            @Param("name") String name,
            @Param("statuses") List<ApprovalStatus> statuses
    );

    @Query("SELECT COUNT(a) > 0 FROM Attachment a WHERE a.name = :name AND a.id <> :id AND a.approvalStatus IN :statuses")
    Boolean checkExistsByNameActiveAndIdNot(
            @Param("name") String name,
            @Param("id") UUID id, @Param("statuses") Collection<ApprovalStatus> statuses);

    default Boolean existsByNameActive(String name) {
        return checkExistsByNameActive(name, Arrays.asList(ApprovalStatus.PENDING, ApprovalStatus.APPROVED)
        );
    }

    default boolean existsByNameActiveAndIdNot(String name, UUID id) {
        return checkExistsByNameActiveAndIdNot(name, id, Arrays.asList(ApprovalStatus.PENDING, ApprovalStatus.APPROVED));
    }
}
