package in.certificatemanager.certWatch.customExceptions;

public class InvalidCertificateFormatException extends RuntimeException{
    public InvalidCertificateFormatException(String message) {
        super(message);
    }
}
