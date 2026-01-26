package ita.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.UUID;

@Setter
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    @JsonIgnore
    private Long createdAt;

    @JsonIgnore
    private Long updatedAt;
}
