package ita.repository;

import ita.dto.SenderResponseDto;
import ita.entity.Sender;
import ita.enumeration.ApprovalStatus;
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

public interface SenderRepository extends JpaRepository<Sender, UUID>, JpaSpecificationExecutor<Sender> {

    @Query("SELECT COUNT(s) > 0 FROM Sender s WHERE s.email = :email AND s.approvalStatus IN :statuses")
    Boolean checkExistsByEmailActive(
            @Param("email") String email,
            @Param("statuses") List<ApprovalStatus> statuses
    );

    @Query(value = "SELECT id, name, email FROM Sender WHERE LOWER(name) LIKE LOWER(CONCAT('%', :name, '%'))", nativeQuery = true)
    Page<SenderResponseDto> findProjection(Pageable pageable, String name);

    @Query("SELECT COUNT(s) > 0 FROM Sender s WHERE s.email = :email AND s.id <> :id AND s.approvalStatus IN :statuses")
    Boolean checkExistsByEmailActiveAndIdNot(
            @Param("email") String email,
            @Param("id") UUID id, @Param("statuses") Collection<ApprovalStatus> statuses);

    default Boolean existsByEmailActive(String email) {
        return checkExistsByEmailActive(email, Arrays.asList(ApprovalStatus.PENDING, ApprovalStatus.APPROVED)
        );
    }

    default boolean existsByEmailActiveAndIdNot(String email, UUID id) {
        return checkExistsByEmailActiveAndIdNot(email, id, Arrays.asList(ApprovalStatus.PENDING, ApprovalStatus.APPROVED));
    }

}
