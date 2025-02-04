package cross.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.google.gson.JsonObject;

public class InsertLimitOrderStrategy extends ActionStrategy {
	
	public boolean requiresLogin() {
		return true;
	}
	public boolean requiresLogout() {
		return false;
	}
	
	public Map<String, Object> getParameters(Scanner scanner) {
		String type = retriveStringDataFromUserStricted(scanner, "type", ActionStrategy::isValidOrderType);
		int size = retriveIntDataFromUser(scanner, "size");
		int price = retriveIntDataFromUser(scanner, "price");
		
		Map<String, Object> valuesParameters = new HashMap<>();
        
        valuesParameters.put("type", type);
        valuesParameters.put("size", size);
        valuesParameters.put("price", price);
        
		return valuesParameters;
	}
	public boolean evaluateResponse(JsonObject message) {
		int response = message.get("orderId").getAsInt();
		if(response == -1) {
			System.out.println("Error, unable to place order");
			return false;
		}
		return printSuccessMessage("Order insert correctly with id: "+ response);
	}
}