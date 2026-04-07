package ita.repository;

import ita.entity.Sender;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface SenderRepository extends JpaRepository<Sender, UUID>, JpaSpecificationExecutor<Sender> {

    Boolean existsByEmail(String email);

    @Query("SELECT COUNT(s) > 0 FROM Sender s WHERE s.email = :email AND s.id <> :id")
    boolean existsByEmailAndNotId(String email, UUID id);

}
