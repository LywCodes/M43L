package ita.repository;

import ita.entity.LocalRole;
import ita.entity.LocalUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<LocalUser, UUID>, JpaSpecificationExecutor<LocalUser> {
    Boolean existsByUsername(String username);

    Optional<LocalUser> findByEmail(String email);
    Optional<LocalUser> findByUsername(String username);

}
