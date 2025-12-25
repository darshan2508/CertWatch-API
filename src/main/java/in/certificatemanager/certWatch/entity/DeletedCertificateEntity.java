package in.certificatemanager.certWatch.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name="tbl_deleted_certificates")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeletedCertificateEntity {
    @Id
    private Long id;

    private String subject;
    private String issuedBy;

    private String serialNumber;
    private int version;
    private String signatureAlgorithm;

    private LocalDate issuedDate;
    private LocalDate expiryDate;

    private String subjectAltName;
    private String comments;

    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private ProfileEntity profile;

}

