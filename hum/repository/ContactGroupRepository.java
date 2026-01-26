package ita.repository;

import ita.entity.ContactGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface ContactGroupRepository extends JpaRepository<ContactGroup, UUID>, JpaSpecificationExecutor<ContactGroup> {

    Boolean existsByName(String name);

}
