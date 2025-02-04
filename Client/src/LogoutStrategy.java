package cross.client;

import java.util.Map;
import java.util.Scanner;

import com.google.gson.JsonObject;

public class LogoutStrategy extends ActionStrategy {
	private static final int OK = 100;
	private static final int OTHER = 101;
	
	public boolean requiresLogin() {
		return true;
	}
	public boolean requiresLogout() {
		return false;
	}
	
	public Map<String, Object> getParameters(Scanner scanner) {
		return null;
	}
	public boolean evaluateResponse(JsonObject message) {
		int response = message.get("response").getAsInt();
	    String errorMessage = message.get("errorMessage").getAsString();
	    switch(response) {
	        case OK -> {return printSuccessMessage("Logged out correctly");}
	        case OTHER -> printErrorMessage("Error: ", errorMessage);
	        default -> printErrorMessage("Unexpected response code: ", errorMessage);
	    }
	    return false;
	}
}