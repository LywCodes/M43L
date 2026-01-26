package ita.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "unsubscribe")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Unsubscribe {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    private Contact contact;
    private String reason;
    private String trackerId;
    private Boolean isUnsubscribed;
    private Long unsubscribedAt;
    private Boolean isWhitelisted;
    private Long whitelistedAt;

}
