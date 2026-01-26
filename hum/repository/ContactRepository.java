package ita.repository;

import ita.entity.Contact;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface ContactRepository extends JpaRepository<Contact, UUID>, JpaSpecificationExecutor<Contact> {

//    @Query("SELECT c FROM Contact c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :value, '%')) OR LOWER(c.email) LIKE LOWER(CONCAT('%', :value, '%'))")
//    Page<Contact> findByFilter(@Param("value") String value, Pageable pageable);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM group_contact WHERE contact_id = :contactId", nativeQuery = true)
    void deleteGroupContactRelations(UUID contactId);

    Boolean existsByEmail(String email);

    List<Contact> findByEmailIn(Set<String> email);

}
