package cross.client;

public class ListenerException extends Exception {
	public ListenerException(String message) {
        super(message);
    }
	public ListenerException(String message, Throwable cause) {
        super(message, cause);
    }
}