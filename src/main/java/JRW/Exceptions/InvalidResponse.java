package JRW.Exceptions;

public class InvalidResponse extends RuntimeException {
    public InvalidResponse (String errorMessage) {
        super(errorMessage);
    }
}
