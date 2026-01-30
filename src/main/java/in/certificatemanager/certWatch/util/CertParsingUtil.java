package in.certificatemanager.certWatch.util;

import in.certificatemanager.certWatch.customExceptions.InvalidCertificateFormatException;
import in.certificatemanager.certWatch.dto.DetailsDTO;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.security.auth.x500.X500Principal;

public class CertParsingUtil {

    private static boolean hasValidPemMarkers(String input) {
        return input.trim().startsWith("-----BEGIN CERTIFICATE-----") && input.trim().endsWith("-----END CERTIFICATE-----");
    }

    public static boolean hasValidBase64Content(String input) {
        try {
            String base64 = input
                    .replace("-----BEGIN CERTIFICATE-----", "")
                    .replace("-----END CERTIFICATE-----", "")
                    .replaceAll("\\s", "");

            Base64.getDecoder().decode(base64);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private static X509Certificate checkX509Certificate(String input) {
        try {
            // Converting String to X509Certificate
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
            return (X509Certificate) factory.generateCertificate(inputStream);
        } catch (Exception e) {
            return null;
        }
    }


    public static DetailsDTO parseCertificate(String fileData) throws CertificateParsingException {
               if(hasValidPemMarkers(fileData)){
                   if(hasValidBase64Content(fileData)){
                        if(checkX509Certificate(fileData)!=null){
                            X509Certificate certificate = checkX509Certificate(fileData);

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
                        }else{
                            throw new InvalidCertificateFormatException("Given certificate is not a valid X509 certificate");
                        }
                   }else{
                       throw new InvalidCertificateFormatException("File does not contain Base64 encoded data.");
                   }
               }else{
                   throw new InvalidCertificateFormatException("Invalid PEM format");
               }
    }
}

