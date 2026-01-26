package ita.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "sender")
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
@Setter
@NoArgsConstructor
public class Sender extends BaseEntity {
    private String email;

}
