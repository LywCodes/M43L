package ita.repository;

import ita.entity.LocalUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<LocalUser, UUID>, JpaSpecificationExecutor<LocalUser> {
    Boolean existsByUsername(String username);

    @Query("SELECT COUNT(us) > 0 FROM LocalUser us WHERE us.username = :username AND us.id <> :id")
    boolean existsByUsernameAndNotId(String username, UUID id);

    Optional<LocalUser> findByEmail(String email);
    Optional<LocalUser> findByUsernameIgnoreCase(String username);

    @Query("SELECT u FROM LocalUser u " +
            "JOIN u.roles r " +
            "WHERE r.name = :roleName " +
            "AND u.id <> :currentUserId")
    List<LocalUser> findApprovers(@Param("roleName") String roleName, @Param("currentUserId") UUID currentUserId);

}
