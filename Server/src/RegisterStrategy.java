package cross.server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.JsonObject;

public class RegisterStrategy extends AuthenticationStrategy {
	
	private static final Map<Integer, String> CODES = Map.of(
			100, "",
			101, "The password cannot be null or empty",
			102, "Username not available",
			103, "Unexpected error"
			);
	
	public RegisterStrategy(ClientHandler clientHandler) {
		super(clientHandler);
	}

	public JsonObject constructStrategyResponse(JsonObject message) {
		if(hasLoginConflict()) {
			return createCodeResponse(103, "You cannot be logged in for this action");
		}
		int code = getResponseCode(message);
		return createCodeResponse(code, CODES.get(code));
	}

	private int getResponseCode(JsonObject message) {

		JsonObject values = message.getAsJsonObject("values");
		if(values == null) return 103;
		String username = retriveStringValue(values, "username");
		String password = retriveStringValue(values, "password");
		if(!isValidString(password))
			return 101;
		if(!isValidString(username))
			return 103;
		
		return registerUser(username, password, getSharedResources().getRegisteredUsers());
	}

	private static int registerUser(String username, String password, ConcurrentHashMap<String, String> registeredUsers) {
		if(registeredUsers.putIfAbsent(username, password) == null) return 100;
		return 102;
	}
}