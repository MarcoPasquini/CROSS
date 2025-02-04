package cross.client;

public class ComunicationWithServerException extends Exception {
	public ComunicationWithServerException(String message) {
        super(message);
    }
	public ComunicationWithServerException(String message, Throwable cause) {
        super(message, cause);
    }
}