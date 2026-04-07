package ita.service;

import ita.dto.BaseSearchCriteriaDto;
import ita.dto.RoleRequestDto;
import ita.dto.RoleResponseDto;
import ita.dto.RoleUpdateDto;
import ita.entity.LocalRole;
import ita.entity.Permission;
import ita.exception.NotFoundException;
import ita.repository.RoleRepository;
import ita.specification.RoleSpecification;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static ita.enumeration.EntityType.ROLE_TYPE;

@Service
@Slf4j
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionService permissionService;

    @Autowired
    public RoleService(RoleRepository roleRepository, PermissionService permissionService) {
        this.roleRepository = roleRepository;
        this.permissionService = permissionService;
    }

    public Page<RoleResponseDto> findAllRole(BaseSearchCriteriaDto searchCriteria) {
        int pageSize = (searchCriteria.getSize() == 999) ? Integer.MAX_VALUE : searchCriteria.getSize();

        Sort sort = Sort.by(searchCriteria.getParam());
        Pageable pageable = PageRequest.of(
                searchCriteria.getPage(),
                pageSize,
                searchCriteria.getType().equalsIgnoreCase("desc") ? sort.descending() : sort.ascending()
        );

        Specification<LocalRole> roleSpecification = RoleSpecification.nameLike(searchCriteria.getName());

        return roleRepository.findAll(roleSpecification, pageable)
                .map(localRole -> RoleResponseDto.builder()
                        .id(localRole.getId())
                        .name(localRole.getName())
                        .description(localRole.getDescription())
                        .build());
    }

    public List<LocalRole> findAllById(List<UUID> roleIds) {
        return roleRepository.findAllById(roleIds);
    }

    public LocalRole findById(UUID id) {
        Optional<LocalRole> roleFromDB = roleRepository.findById(id);

        if (roleFromDB.isEmpty()) {
            throw new NotFoundException(ROLE_TYPE, "id", id.toString());
        }

        return roleFromDB.get();
    }

    public LocalRole addRole(RoleRequestDto roleRequestDto) {
        LocalRole role = new LocalRole();

        role.setName(roleRequestDto.getName());
        role.setDescription(roleRequestDto.getDescription());
        role.setCreatedAt(System.currentTimeMillis());

        List<Permission> permissions = permissionService.findAllByIds(roleRequestDto.getPermissionIds());

        role.setPermissions(permissions);

        return roleRepository.save(role);
    }

    public LocalRole updateRole(RoleUpdateDto roleUpdateDto) {
        LocalRole role = findById(roleUpdateDto.getId());

        role.setName(roleUpdateDto.getName());
        role.setDescription(roleUpdateDto.getDescription());
        role.setUpdatedAt(System.currentTimeMillis());

        List<Permission> permissions = permissionService.findAllByIds(roleUpdateDto.getPermissionIds());

        role.setPermissions(permissions);

        return roleRepository.save(role);
    }

    @Transactional
    public void deleteRole(String id) {
        UUID roleId = UUID.fromString(id);

        roleRepository.deleteUserRoleRelations(roleId);

        roleRepository.deleteById(roleId);
    }

    public Boolean existsByName(String name) {
        return roleRepository.existsByName(name);
    }

    public boolean isUniqueForUpdate(String name, UUID id) {
        return !roleRepository.existsByNameAndNotId(name, id);
    }

}
