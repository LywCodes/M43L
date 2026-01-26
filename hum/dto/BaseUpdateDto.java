package ita.dto;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.UUID;

@Setter
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseUpdateDto {

    @NotNull(message = "{mandatory.uuid}")
    private UUID id;

    @NotBlank(message = "{mandatory.string}")
    @Size(min = 3, max = 50, message = "{size.string}")
    private String name;

}
