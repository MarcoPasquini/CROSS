package cross.server;

public class ComunicationWithClientException extends Exception {
	public ComunicationWithClientException(String message) {
        super(message);
    }
	public ComunicationWithClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
