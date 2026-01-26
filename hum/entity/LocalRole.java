package ita.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Fetch;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "local_role")
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
@Setter
@NoArgsConstructor
public class LocalRole extends BaseEntity {
    private String description;

    @ManyToMany
    @JoinTable(name = "role_permission",
            joinColumns = @JoinColumn(name = "local_role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id"))
    private List<Permission> permissions;

}
