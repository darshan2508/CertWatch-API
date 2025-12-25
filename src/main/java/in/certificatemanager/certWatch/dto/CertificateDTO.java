package in.certificatemanager.certWatch.dto;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificateDTO {

    private Long id;
    private Long categoryId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isArchived;
    private LocalDate issuedDate;
    private LocalDate expiryDate;
    private String serialNumber;
    private int version;
    private String subject;
    private String issuedBy;
    private String subjectAltName;
    private String comments;
    private String signatureAlgorithm;
}
