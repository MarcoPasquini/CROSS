package cross.server;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.JsonObject;

public class UpdateCredentialsStrategy extends AuthenticationStrategy {
	
	private static final Map<Integer, String> CODES = Map.of(
			100, "",
			101, "New password cannot be null or empty",
			102, "Non existent username or old password is wrong",
			103, "New password cannot be equal to the old one",
			104, "User currently logged in",
			105, "Unexpected error"
			);
	
	public UpdateCredentialsStrategy(ClientHandler clientHandler) {
		super(clientHandler);
	}
	
	public JsonObject constructStrategyResponse(JsonObject message) {
		if(hasLoginConflict()) {
			return createCodeResponse(103, "You are already logged in");
		}
		int code = getResponseCode(message);
		return createCodeResponse(code, CODES.get(code));
	}
	
	private int getResponseCode(JsonObject message) {
		JsonObject values = message.getAsJsonObject("values");
		if(values == null) return 105;
		
		String username = retriveStringValue(values, "username");
		String oldPassword = retriveStringValue(values, "old_password");
		String newPassword = retriveStringValue(values, "new_password");
		if(!isValidString(newPassword))
			return 101;
		if(!isValidString(username) || !isValidString(oldPassword))
			return 102;
		
		return modifyPassowrdOfUser(username, oldPassword, newPassword);
	}

	private int modifyPassowrdOfUser(String username, String oldPassword, String newPassword) {
		ConcurrentHashMap<String, String> registeredUsers = getSharedResources().getRegisteredUsers();
		//Utente non registrato
		if(registeredUsers.get(username) == null)
			return 102;
		//Password registrata uguale a quella nuova
		if(registeredUsers.get(username).equals(newPassword))
			return 103;
		//Utente loggato
		if(getSharedResources().getLoggedUsers().containsKey(username))
			return 104;
		if(registeredUsers.replace(username, oldPassword, newPassword))
			return 100;
		return 102;
	}
}