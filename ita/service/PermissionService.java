package ita.service;

import ita.dto.BaseSearchCriteriaDto;
import ita.dto.PermissionRequestDto;
import ita.dto.PermissionResponseDto;
import ita.dto.PermissionUpdateDto;
import ita.entity.Permission;
import ita.exception.NotFoundException;
import ita.repository.PermissionRepository;
import ita.specification.PermissionSpecification;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static ita.enumeration.EntityType.PERMISSION_TYPE;

@Service
public class PermissionService {

    private final PermissionRepository permissionRepository;

    public PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    public Page<PermissionResponseDto> findAll(BaseSearchCriteriaDto searchCriteria) {
        int pageSize = (searchCriteria.getSize() == 999) ? Integer.MAX_VALUE : searchCriteria.getSize();

        Sort sort = Sort.by(searchCriteria.getParam());
        Pageable pageable = PageRequest.of(
                searchCriteria.getPage(),
                pageSize,
                searchCriteria.getType().equalsIgnoreCase("desc") ? sort.descending() : sort.ascending()
        );

        Specification<Permission> permissionSpecification = PermissionSpecification.nameLike(searchCriteria.getName());

        return permissionRepository.findAll(permissionSpecification, pageable)
                .map(permission -> new PermissionResponseDto(permission.getId(), permission.getName()));
    }

    public List<Permission> findAllByIds(List<UUID> ids) {
        return permissionRepository.findAllById(ids);
    }

    public PermissionResponseDto findDtoById(UUID id) {
        Permission permission = findById(id);

        PermissionResponseDto permissionResponseDto = new PermissionResponseDto();

        permissionResponseDto.setId(permission.getId());
        permissionResponseDto.setName(permission.getName());

        return permissionResponseDto;
    }

    public Permission addPermission(PermissionRequestDto permissionRequestDto) {
        Permission permission = new Permission();

        permission.setName(permissionRequestDto.getName());
        permission.setCreatedAt(System.currentTimeMillis());

        return permissionRepository.save(permission);
    }

    public Permission updatePermission(PermissionUpdateDto permissionUpdateDto) {
        Permission permission = findById(permissionUpdateDto.getId());

        permission.setName(permissionUpdateDto.getName());

        return permissionRepository.save(permission);
    }

    public Boolean existsByName(String name) {
        return permissionRepository.existsByName(name);
    }

    public boolean isUniqueForUpdate(String name, UUID id) {
        return !permissionRepository.existsByNameAndNotId(name, id);
    }

    @Transactional
    public void deletePermission(UUID id) {
        permissionRepository.deleteRolePermissionRelations(id);

        permissionRepository.deleteById(id);
    }

    private Permission findById(UUID id) {
        Optional<Permission> permission = permissionRepository.findById(id);

        if (permission.isEmpty()) {
            throw new NotFoundException(PERMISSION_TYPE, "id", id.toString());
        }

        return permission.get();
    }

}
