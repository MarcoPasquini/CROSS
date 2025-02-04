package cross.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.google.gson.JsonObject;

public class RegisterStrategy extends ActionStrategy {
	private static final int OK = 100;
	private static final int INVALID_PASSWORD = 101;
	private static final int USERNAME_NOT_AVAILABLE = 102;
	private static final int OTHER = 103;
	
	public boolean requiresLogin() {
		return false;
	}
	public boolean requiresLogout() {
		return true;
	}
	
	public Map<String, Object> getParameters(Scanner scanner) {
		String username = retriveStringDataFromUser(scanner, "username");
		String password = retriveStringDataFromUser(scanner, "password");
		
		Map<String, Object> valuesParameters = new HashMap<>();
        
        valuesParameters.put("username", username);
        valuesParameters.put("password", password);
        
		return valuesParameters;
	}
	
	public boolean evaluateResponse(JsonObject message) {
		int response = message.get("response").getAsInt();
        String errorMessage = message.get("errorMessage").getAsString();
        switch(response) {
	        case OK -> {return printSuccessMessage("Registration confirmed");}
	        case INVALID_PASSWORD -> printErrorMessage("Invalid password: ", errorMessage);
	        case USERNAME_NOT_AVAILABLE -> printErrorMessage("Username not available: ", errorMessage);
	        case OTHER -> printErrorMessage("Error: ", errorMessage);
	        default -> printErrorMessage("Unexpected response code: ", errorMessage);
        }
        return false;
	}
}
