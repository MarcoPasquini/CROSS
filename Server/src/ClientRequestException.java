package cross.server;

public class ClientRequestException extends Exception {
	public ClientRequestException(String message) {
        super(message);
    }
	public ClientRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
