package in.certificatemanager.certWatch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DetailsDTO {
    private String subject;
    private String issuedBy;
    private String serialNumber;
    private LocalDate issuedDate;
    private LocalDate expiryDate;
    private int version;
    private String signatureAlgorithm;
    private String subjectAltName;
}
