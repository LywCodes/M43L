package ita.repository;

import ita.entity.ContactGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface ContactGroupRepository extends JpaRepository<ContactGroup, UUID>, JpaSpecificationExecutor<ContactGroup> {

    Boolean existsByName(String name);

    @Query("SELECT COUNT(cg) > 0 FROM ContactGroup cg WHERE cg.name = :name AND cg.id <> :id")
    boolean existsByNameAndNotId(String name, UUID id);
}
