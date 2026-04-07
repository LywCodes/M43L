package ita.repository;

import ita.entity.ContactAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface ContactAttributeRepository extends JpaRepository<ContactAttribute, UUID> {

    List<ContactAttribute>findByContactGroupId(UUID contactGroupId);

    @Modifying
    @Query("DELETE FROM ContactAttribute c WHERE c.contactGroup.id = :groupId")
    void deleteByContactGroupId(@Param("groupId") UUID groupId);

    Optional<ContactAttribute> findFirstByContactGroupId(UUID contactGroupId);
    List<ContactAttribute>findByContactIdIn(List<UUID> contactIds);

    @Query(value = """
    SELECT DISTINCT lower(jsonb_object_keys(ca.attributes))
    FROM contact_attribute ca
    WHERE ca.contact_id IN (
        SELECT DISTINCT cd.contact_id
        FROM campaign_detail cd
        WHERE cd.sent_at BETWEEN :startDate AND :endDate
        OR cd.soft_bounced_at BETWEEN :startDate AND :endDate
        OR cd.opened_at BETWEEN :startDate AND :endDate
        OR cd.unsubscribed_at BETWEEN :startDate AND :endDate
    )
    """, nativeQuery = true)
    Set<String> findAllDynamicKeys(
            @Param("startDate") Long startDate,
            @Param("endDate") Long endDate
    );
}
