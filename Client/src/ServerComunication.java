package cross.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import com.google.gson.JsonObject;

import cross.utils.ConvertionException;
import cross.utils.MessageConvertion;

public class ServerComunication {
	
	public static JsonObject executeServerRequest(Map<String, Object> valuesParameters, String operation) throws ComunicationWithServerException {
		String message = constructMessage(operation, valuesParameters);
		return comunicateWithServer(message);
	}
	
	private static String constructMessage(String operation, Map<String, Object> valuesParameters) {
		JsonObject message = createOperationMessage(operation);
		JsonObject values = createValueMessage(valuesParameters);
		
		message.add("values", values);

        return message.toString();
	}
	//Crea l'oggetto JSON indicando il tipo di operazione
	private static JsonObject createOperationMessage(String operation) {
	        
	        JsonObject message = new JsonObject();
	        message.addProperty("operation", operation);
	        return message;
	}
	//Inserisce i parametri nel messaggio
	private static JsonObject createValueMessage(Map<String, Object> valuesParameters) {
		JsonObject values = new JsonObject();
		if(valuesParameters == null) return values;
        for (Map.Entry<String, Object> entry : valuesParameters.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            addGenericProperty(values, key, value);
        }
        return values;
	}
    //Aggiunge il parametro al messaggio
	private static void addGenericProperty(JsonObject values, String name, Object value) {
        if (value instanceof Integer) {
        	values.addProperty(name, (Integer) value);
        } else {
        	values.addProperty(name, (String) value);
        }
	}
	private static JsonObject comunicateWithServer(String message) throws ComunicationWithServerException {
		sendMessage(message);
		return receiveServerMessage();
	}
	public static void sendMessage(String message) throws ComunicationWithServerException {
		try {
	        ServerConnection connection = ServerConnection.getInstance();
	        //Scrive nello stram di output verso il server
	        PrintWriter writer = connection.getWriter();
	        writer.println(message);
	    } catch (IOException e) {
	        throw new ComunicationWithServerException("Error communicating with server: " + e.getMessage());
	    }
	}
	//Riceve il messaggio e lo converte in un oggetto JSON
	private static JsonObject receiveServerMessage() throws ComunicationWithServerException {
		try {
	        ServerConnection connection = ServerConnection.getInstance();
	        BufferedReader reader = connection.getReader();
	        return MessageConvertion.convertMessageToJson(reader.readLine());
	    } catch (IOException e) {
	        throw new ComunicationWithServerException("Error communicating with server: " + e.getMessage());
	    }catch (ConvertionException e) {
	        throw new ComunicationWithServerException("Error converting message to JSON: " + e.getMessage());
	    }
	}
}
