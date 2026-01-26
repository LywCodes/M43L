package ita.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "permission")
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
@Setter
@NoArgsConstructor
public class Permission extends BaseEntity{
}
