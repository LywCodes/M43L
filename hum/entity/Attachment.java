package ita.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "attachment")
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class Attachment extends BaseEntity {

    @Lob
    @Column(length = 100000)
    private byte[] file;

    @Override
    public String toString() {
        return "Attachment(" + "size in KB=" + (file.length / 1024) + ", name=" + super.getName() + ')';
    }

}
