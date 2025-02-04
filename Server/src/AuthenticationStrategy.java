package cross.server;

import com.google.gson.JsonObject;

public abstract class AuthenticationStrategy extends ActionStrategy {

	public AuthenticationStrategy(ClientHandler clientHandler) {
		super(clientHandler);
	}
	public boolean requiresLogin() {
		return false;
	}
	
	public boolean requiresLogout() {
		return true;
	}
	public JsonObject constructResponse(JsonObject message) {
		synchronized(getSharedResources().getSyncAuthHandler()) {
			return constructStrategyResponse(message);
		}
	}
	public abstract JsonObject constructStrategyResponse(JsonObject message);
}
