package ita.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

@Entity
@Table(name = "content")
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
@Setter
@NoArgsConstructor
public class Content extends BaseFullAuditEntity {

    @Column(columnDefinition = "TEXT")
    private String html;

    @Generated(event = EventType.INSERT)
    @Column(name = "auto_increment_id", insertable = false, updatable = false)
    private Long autoIncrementId;

    private Integer numberOfParam;

}
