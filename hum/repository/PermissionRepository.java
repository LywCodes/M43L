package ita.repository;

import ita.entity.Permission;
import ita.entity.Sender;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface PermissionRepository extends JpaRepository<Permission, UUID>, JpaSpecificationExecutor<Permission> {

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM role_permission WHERE permission_id = :permissionId", nativeQuery = true)
    void deleteRolePermissionRelations(UUID permissionId);

    Boolean existsByName(String name);

}
