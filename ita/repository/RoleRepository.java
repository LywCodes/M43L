package ita.repository;

import ita.entity.LocalRole;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface RoleRepository extends JpaRepository<LocalRole, UUID>, JpaSpecificationExecutor<LocalRole> {

    Boolean existsByName(String name);

    @Query("SELECT COUNT(rl) > 0 FROM LocalRole rl WHERE rl.name = :name AND rl.id <> :id")
    boolean existsByNameAndNotId(String name, UUID id);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM user_role WHERE local_role_id = :localRoleId", nativeQuery = true)
    void deleteUserRoleRelations(UUID localRoleId);

}
