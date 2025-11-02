package in.certificatemanager.certWatch.dto;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificateDTO {

    private Long id;
    private Long categoryId;
    private String categoryName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isActive;
    private Date validFrom;
    private Date validTo;
    private String serialNumber;
    private int version;
    private String subject;
    private String issuer;
    private String commonName;
}
