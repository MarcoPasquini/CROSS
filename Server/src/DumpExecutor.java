package cross.server;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

public class DumpExecutor implements Runnable {
	
	private SharedResources sharedResources;

	public DumpExecutor(SharedResources sharedResources) {
		this.sharedResources = sharedResources;
	}

	public void run() {
		updateRegisteredUsers();
		updateFulfilledOrders();
	}
	
	//Aggiorna gli utenti registrati
	private void updateRegisteredUsers() {
		
        String registeredJson = getRootRegisteredUsers();
        
		writeJsonFile(sharedResources.getRegistrationFile(), registeredJson);
	}
	
	//Costruisce Json degli utenti registrati
	private String getRootRegisteredUsers() {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		JsonObject root = new JsonObject();
        JsonArray usersArray = getJsonArrayFromRegisteredUsers();
       
        root.add("registeredUsers", usersArray);

        return gson.toJson(root);
	}
	
	private JsonArray getJsonArrayFromRegisteredUsers() {
		JsonArray usersArray = new JsonArray();
        ConcurrentHashMap<String, String> registeredUsers = sharedResources.getRegisteredUsers();
        registeredUsers.forEach((username, password) -> {
            JsonObject userObject = new JsonObject();
            userObject.addProperty("username", username);
            userObject.addProperty("password", password);
            usersArray.add(userObject);
        });
        return usersArray;
	}
	//Aggiorna ordini evasi
	private void updateFulfilledOrders() {
		String existingOrders = getRootUpdatedOrders();
		if(existingOrders == null) return;
		writeJsonFile(sharedResources.getOrdersFile(), existingOrders);
	}
	
	//Aggiunge i nuovi ordini evasi a quelli salvati in memoria
	private String getRootUpdatedOrders() {
		try {
			String filePath = sharedResources.getOrdersFile();
			PriorityQueue<OrderDTO> existingOrders = getExistingOrdersFromFile(filePath);
			synchronized(sharedResources.getSyncOrderHandler()) {
				PriorityBlockingQueue<OrderDTO> ordersQueue = getFulfilledOrdersQueue();
		        if (existingOrders == null) {
		            existingOrders = new PriorityQueue<OrderDTO>();
		        }
		        existingOrders.addAll(ordersQueue);
		        ordersQueue.clear();
			}
			return getUpdatedOrders(existingOrders);

		}catch(Exception e) {
			System.out.println(e.getMessage());
			return null;
		}
	}
	//Costruisce coda degli ordini evasi, usando OrderDTO come formato da salvare in memoria
	private PriorityBlockingQueue<OrderDTO> getFulfilledOrdersQueue() {
		Iterator<Order> oldOrdersQueueIterator = sharedResources.getFulfilledOrders().iterator();
		PriorityBlockingQueue<OrderDTO> newOrdersQueue = new PriorityBlockingQueue<OrderDTO>();
		while(oldOrdersQueueIterator.hasNext()) {
			Order tmp = oldOrdersQueueIterator.next();
			newOrdersQueue.add(new OrderDTO(tmp.getOrderId(), tmp.getType(), tmp.getSize(), tmp.getPrice(), tmp.getTime()));
		}
		sharedResources.getFulfilledOrders().clear();
		return newOrdersQueue;
	}

	//Recupera nuovi ordini evasi
	private String getUpdatedOrders(PriorityQueue<OrderDTO> existingOrders) {
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("trades", gson.toJsonTree(existingOrders));
        return gson.toJson(jsonObject);
	}

	private PriorityQueue<OrderDTO> getExistingOrdersFromFile(String filePath) {
		Gson gson = new Gson();
        try (FileReader reader = new FileReader(filePath)) {
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray tradesArray = jsonObject.getAsJsonArray("trades");
            Type listType = new TypeToken<PriorityQueue<OrderDTO>>() {}.getType();
            return gson.fromJson(tradesArray, listType);
        } catch (IOException e) {
            System.out.println("Errore durante la lettura del file JSON: " + e.getMessage());
        }catch(Exception e) {
        	System.out.println(e.getMessage());
        }
    	return null;
	}

    //Scrive su file
	private void writeJsonFile(String path, String data) {
		try (FileWriter writer = new FileWriter(path)) {
            writer.write(data);
            System.out.println(path+" successfully updated");
        } catch (IOException e) {
            System.out.println("Error writing file: " + e.getMessage());
        }catch(Exception e) {
        	System.out.println("Error writing file: " + e.getMessage());
        }
	}

}


/*

salva in memoria permanente periodicamente le informazioni su utenti registrati e ordini evasi


*/