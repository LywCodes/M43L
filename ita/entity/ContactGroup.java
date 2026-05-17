package ita.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.List;

@Entity
@Table(name = "contact_group")
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
@Setter
@NoArgsConstructor
public class ContactGroup extends BaseEntity {

    @ManyToMany(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinTable(name = "group_contact",
        joinColumns = @JoinColumn(name = "contact_group_id"),
            inverseJoinColumns = @JoinColumn(name = "contact_id"))
    private List<Contact> contacts;

    private Integer size;

}
