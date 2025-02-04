package cross.server;

import java.util.Map;

import com.google.gson.JsonObject;

public class LogoutStrategy extends AuthenticationStrategy {
	
	private static final Map<Integer, String> CODES = Map.of(
			100, "",
			101, "User not logged in"
			);
	
	public LogoutStrategy(ClientHandler clientHandler) {
		super(clientHandler);
	}

	public boolean requiresLogin() {
		return true;
	}
	
	public boolean requiresLogout() {
		return false;
	}
	
	public JsonObject constructStrategyResponse(JsonObject message) {
		int code = logoutUser();
		return createCodeResponse(code, CODES.get(code));
	}
}