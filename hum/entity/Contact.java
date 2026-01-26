package ita.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "contact")
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
@Setter
@NoArgsConstructor
public class Contact extends BaseEntity {

    @Column(unique = true)
    private String email;

    @Column(columnDefinition = "boolean default false")
    private Boolean isUnsubscribed;

}
