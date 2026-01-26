package ita.repository;

import ita.entity.Sender;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface SenderRepository extends JpaRepository<Sender, UUID>, JpaSpecificationExecutor<Sender> {

    Boolean existsByEmail(String email);

}
