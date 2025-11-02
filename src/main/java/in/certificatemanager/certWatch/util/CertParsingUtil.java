package in.certificatemanager.certWatch.util;

import in.certificatemanager.certWatch.dto.DetailsDTO;
import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import javax.security.auth.x500.X500Principal;

public class CertParsingUtil {
    public static DetailsDTO parseCertificate(String fileData) {
           try{
               // Converting String to X509Certificate
               CertificateFactory factory = CertificateFactory.getInstance("X.509");
               ByteArrayInputStream inputStream = new ByteArrayInputStream(fileData.getBytes(StandardCharsets.UTF_8));
               X509Certificate certificate = (X509Certificate) factory.generateCertificate(inputStream);

               // Extracting certificate details
               // Getting Subject from the certificate and storing it is string format
               String subjectStr = certificate.getSubjectX500Principal().getName(X500Principal.RFC2253);
               // Extracting issuer details from the certificate and storing it in string format
               String issuerStr = certificate.getIssuerX500Principal().getName(X500Principal.RFC2253);
               // Extracting serial number details from the certificate
               String serialNumber = certificate.getSerialNumber().toString();

               return DetailsDTO.builder()
                       .subject(subjectStr)
                       .issuer(issuerStr)
                       .serialNumber(certificate.getSerialNumber().toString())
                       .version(certificate.getVersion())
                       .validFrom(certificate.getNotBefore())
                       .validTo(certificate.getNotAfter())
                       .build();
           }catch (Exception e) {
                   System.err.println("****Error in extracting details****");
                   throw new RuntimeException(e.getMessage());
               }
    }
}

