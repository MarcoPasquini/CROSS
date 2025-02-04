package cross.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class MessageConvertion {
	
	//Converte la stringa in un oggetto JSON
	public static JsonObject convertMessageToJson(String message) throws ConvertionException {
        JsonElement jsonElement = JsonParser.parseString(message);
        return validateJsonObject(jsonElement);
    }
	
	//Verifica se l'elemento è un oggetto JSON
	public static JsonObject validateJsonObject(JsonElement jsonElement) throws ConvertionException {
		if (jsonElement.isJsonObject()) {
	        return jsonElement.getAsJsonObject();
	    } else {
	        throw new ConvertionException("Not a valid JSON object");
	    }
    }
}
