package cross.client;

public class ConnectionWithServerException extends Exception {
	public ConnectionWithServerException(String message) {
        super(message);
    }
	public ConnectionWithServerException(String message, Throwable cause) {
        super(message, cause);
    }
}