package cross.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.google.gson.JsonObject;

public class UpdateCredentialsStrategy extends ActionStrategy {
	private static final int OK = 100;
	private static final int INVALID_NEW_PASSWORD = 101;
	private static final int INVALID_OLD_CREDENTIALS = 102;
	private static final int NEW_PASSWORD_EQUALS_OLD = 103;
	private static final int CURRENTLY_LOGGED = 104;
	private static final int OTHER = 105;
	
	public boolean requiresLogin() {
		return false;
	}
	public boolean requiresLogout() {
		return true;
	}
	
	public Map<String, Object> getParameters(Scanner scanner) {
		String username = retriveStringDataFromUser(scanner, "username");
		String oldPassword = retriveStringDataFromUser(scanner, "old password");
		String newPassword = retriveStringDataFromUser(scanner, "new password");
		
		Map<String, Object> valuesParameters = new HashMap<>();
        
        valuesParameters.put("username", username);
        valuesParameters.put("old_password", oldPassword);
        valuesParameters.put("new_password", newPassword);
        
		return valuesParameters;
	}
	public boolean evaluateResponse(JsonObject message) {
		int response = message.get("response").getAsInt();
        String errorMessage = message.get("errorMessage").getAsString();
        switch(response) {
	        case OK -> {return printSuccessMessage("Password changed correctly");}
	        case INVALID_NEW_PASSWORD -> printErrorMessage("Invalid new password: ", errorMessage);
	        case INVALID_OLD_CREDENTIALS -> printErrorMessage("Wrong username/old_password: ", errorMessage);
	        case NEW_PASSWORD_EQUALS_OLD -> printErrorMessage("New password can't be equals to the old one: ", errorMessage);
	        case CURRENTLY_LOGGED -> printErrorMessage("User currently logged: ", errorMessage);
	        case OTHER -> printErrorMessage("Error: ", errorMessage);
	        default -> printErrorMessage("Unexpected response code: ", errorMessage);
        }
        return false;
	}

}
