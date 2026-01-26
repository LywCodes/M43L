package ita.service;

import ita.dto.BaseSearchCriteriaDto;
import ita.dto.RoleRequestDto;
import ita.dto.RoleResponseDto;
import ita.dto.RoleUpdateDto;
import ita.entity.LocalRole;
import ita.entity.Permission;
import ita.entity.Sender;
import ita.exception.NotFoundException;
import ita.repository.RoleRepository;
import ita.specification.RoleSpecification;
import ita.specification.SenderSpecification;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
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
        Pageable pageable;
        int pageSize;

        if (searchCriteria.getSize() == 999) {
            pageSize = Integer.MAX_VALUE;
        } else {
            pageSize = searchCriteria.getSize();
        }

        if (searchCriteria.getType().equals("desc")) {
            pageable = PageRequest.of(searchCriteria.getPage(), pageSize, Sort.by(searchCriteria.getParam()).descending());
        } else {
            pageable = PageRequest.of(searchCriteria.getPage(), pageSize, Sort.by(searchCriteria.getParam()).ascending());
        }

        Specification<LocalRole> roleSpecification = Specification.where(RoleSpecification.nameLike(searchCriteria.getName()));

        Page<LocalRole> localRoles = roleRepository.findAll(roleSpecification, pageable);

        List<RoleResponseDto> roleResponseDtos = localRoles.getContent().stream().map(localRole ->
                RoleResponseDto.builder()
                        .id(localRole.getId())
                        .name(localRole.getName())
                        .description(localRole.getDescription())
                        .build()).toList();

        return new PageImpl<>(roleResponseDtos, pageable, localRoles.getTotalElements());
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

}
