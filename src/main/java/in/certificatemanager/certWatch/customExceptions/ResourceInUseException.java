package in.certificatemanager.certWatch.customExceptions;

public class ResourceInUseException extends RuntimeException{
    public ResourceInUseException(String message) {
        super(message);
    }
}
