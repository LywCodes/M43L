package ita.repository;

import ita.entity.Permission;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface PermissionRepository extends JpaRepository<Permission, UUID>, JpaSpecificationExecutor<Permission> {

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM role_permission WHERE permission_id = :permissionId", nativeQuery = true)
    void deleteRolePermissionRelations(UUID permissionId);

    Boolean existsByName(String name);

    @Query("SELECT COUNT(p) > 0 FROM Permission p WHERE p.name = :name AND p.id <> :id")
    boolean existsByNameAndNotId(String name, UUID id);

}
