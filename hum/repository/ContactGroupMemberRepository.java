package ita.repository;

import ita.entity.ContactGroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ContactGroupMemberRepository extends JpaRepository<ContactGroupMember, UUID> {

    // Cari member berdasarkan group ID
    List<ContactGroupMember> findByContactGroupId(UUID contactGroupId);

    // Cek apakah member sudah ada di grup ini (untuk avoid duplicate insert error)
    boolean existsByContactIdAndContactGroupId(UUID contactId, UUID contactGroupId);
}