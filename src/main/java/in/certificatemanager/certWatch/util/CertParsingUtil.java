package in.certificatemanager.certWatch.util;

import in.certificatemanager.certWatch.dto.DetailsDTO;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.security.auth.x500.X500Principal;

public class CertParsingUtil {
    public static DetailsDTO parseCertificate(String fileData) {
           try{
               // Converting String to X509Certificate
               CertificateFactory factory = CertificateFactory.getInstance("X.509");
               ByteArrayInputStream inputStream = new ByteArrayInputStream(fileData.getBytes(StandardCharsets.UTF_8));
               X509Certificate certificate = (X509Certificate) factory.generateCertificate(inputStream);

               // Extracting certificate details
               // Getting Subject from the certificate and storing it in string format
               String subjectStr = certificate.getSubjectX500Principal().getName(X500Principal.RFC2253);

               // Extracting issuer details from the certificate and storing it in string format
               String issuerStr = certificate.getIssuerX500Principal().getName(X500Principal.RFC2253);

               // Extracting serial number details from the certificate
               String serialNumber = certificate.getSerialNumber().toString();

               // Get Subject Alternative Names if any
               Collection<List<?>> subjectAlternativeNames = certificate.getSubjectAlternativeNames();

               String sanString = "";
               if (subjectAlternativeNames != null) {
                   sanString = subjectAlternativeNames.stream()
                           .map(list -> list.get(1))        // index 1 contains the SAN value
                           .map(Object::toString)
                           .collect(Collectors.joining("; "));
               }

               return DetailsDTO.builder()
                       .subject(subjectStr)
                       .issuedBy(issuerStr)
                       .serialNumber(serialNumber)
                       .version(certificate.getVersion())
                       .signatureAlgorithm(certificate.getSigAlgName())
                       .issuedDate(certificate.getNotBefore().toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
                       .expiryDate(certificate.getNotAfter().toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
                       .subjectAltName(sanString)
                       .build();
           }catch (Exception e) {
                   System.err.println("****Error in extracting details****");
                   throw new RuntimeException(e.getMessage());
               }
    }
}

