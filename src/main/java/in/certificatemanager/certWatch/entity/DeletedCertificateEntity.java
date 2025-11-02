package in.certificatemanager.certWatch.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name="tbl_deleted_certificates")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeletedCertificateEntity {
    @Id
    private Long id;
    private LocalDate date;

    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private Boolean isActive;

    private Date validFrom;
    private Date validTo;

    private String serialNumber;

    private int version;

    private String subject;
    private String issuer;
    private String commonName;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private ProfileEntity profile;
}
