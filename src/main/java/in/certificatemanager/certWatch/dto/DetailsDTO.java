package in.certificatemanager.certWatch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DetailsDTO {
    private String subject;
    private String issuer;
    private String serialNumber;
    private Date validFrom;
    private Date validTo;
    private int version;
}
