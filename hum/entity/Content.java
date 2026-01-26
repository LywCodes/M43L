package ita.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "content")
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
@Setter
@NoArgsConstructor
public class Content extends BaseEntity {

    @Column(columnDefinition = "TEXT")
    @JsonIgnore
    private String html;

    private Integer numberOfParam;

}
