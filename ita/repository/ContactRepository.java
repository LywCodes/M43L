package ita.repository;

import ita.entity.Contact;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface ContactRepository extends JpaRepository<Contact, UUID>, JpaSpecificationExecutor<Contact> {

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM group_contact WHERE contact_id = :contactId", nativeQuery = true)
    void deleteGroupContactRelations(UUID contactId);

    Boolean existsByEmail(String email);

    @Query("SELECT COUNT(co) > 0 FROM Contact co WHERE co.email = :email AND co.id <> :id")
    boolean existsByEmailAndNotId(String email, UUID id);

    List<Contact> findByEmailIn(Set<String> email);

}
