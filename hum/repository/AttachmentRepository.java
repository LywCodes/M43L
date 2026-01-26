package ita.repository;

import ita.entity.Attachment;
import ita.projection.BaseProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, UUID> {

    @Query(value = "SELECT id, name FROM Attachment WHERE LOWER(name) LIKE LOWER(CONCAT('%', :name, '%'))", nativeQuery = true)
    Page<BaseProjection> findProjection (Pageable pageable, String name);

}
