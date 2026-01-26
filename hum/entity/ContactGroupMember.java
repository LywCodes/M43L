package ita.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "contact_group_members",
        uniqueConstraints = @UniqueConstraint(columnNames = {"contact_id", "contact_group_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactGroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Relasi ke Contact (Global ID)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id", nullable = false)
    private Contact contact;

    // Relasi ke Group (Campaign)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_group_id", nullable = false)
    private ContactGroup contactGroup;

    // INI KUNCINYA: Kolom dinamis untuk atribut CSV
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> attributes;

    private Long joinedAt;

    @PrePersist
    protected void onCreate() {
        this.joinedAt = System.currentTimeMillis();
    }
}