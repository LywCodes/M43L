package ita.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "local_user")
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
@Setter
@NoArgsConstructor
public class LocalUser extends BaseEntity {

    @JsonIgnore
    private String email;
    private String username;

    @JsonIgnore
    private String password;

    @ManyToMany(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinTable(name = "user_role", joinColumns = @JoinColumn(name = "local_user_id"), inverseJoinColumns = @JoinColumn(name = "local_role_id"))
    private Set<LocalRole> roles;
}
