package ita.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "tbl_email_recipient")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmailRecipient {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String emailAddress;
    private String emailSubject;
    private String pixelId;
    private int readCount;
    private boolean isFailedToSend;
    private long createdAt;

}
