package ita.repository;

import ita.entity.Attachment;
import ita.entity.Content;
import ita.projection.BaseProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface ContentRepository extends JpaRepository<Content, UUID> {

    @Query(value = "SELECT id, name FROM Content WHERE LOWER(name) LIKE LOWER(CONCAT('%', :name, '%'))", nativeQuery = true)
    Page<BaseProjection> findProjection (Pageable pageable, String name);

}
