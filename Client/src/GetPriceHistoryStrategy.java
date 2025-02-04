package cross.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import cross.utils.DateValidator;

public class GetPriceHistoryStrategy extends ActionStrategy {
	
	private static final int OK = 100;
	private static final int ERROR_CODE = 101;
	
	public boolean requiresLogin() {
		return false;
	}
	public boolean requiresLogout() {
		return false;
	}
	
	public Map<String, Object> getParameters(Scanner scanner) {
		String month = retriveStringDataFromUserStricted(scanner, "month", GetPriceHistoryStrategy::isValidMonth);
		
		Map<String, Object> valuesParameters = new HashMap<>();
        
        valuesParameters.put("month", month);
        
		return valuesParameters;
	}
	
	private static boolean isValidMonth(String month) {
        return DateValidator.isPastMonth(month);
	}

	public boolean evaluateResponse(JsonObject message) {
		int response = message.get("response").getAsInt();
		JsonArray values = message.getAsJsonArray("values");
        switch(response) {
	        case OK -> printPriceHistory(values);
	        case ERROR_CODE -> System.out.println("Server unable to retrive data");
	        default -> System.out.println("Unexpected response code");
        }
        return false;
	}
	private void printPriceHistory(JsonArray values) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        // Stampa del JsonArray in modo leggibile
        System.out.println(gson.toJson(values));
	}
}
