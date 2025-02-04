package cross.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.google.gson.JsonObject;

import cross.utils.ConvertionException;
import cross.utils.MessageConvertion;

public class ClientHandler implements Runnable {
	
	private SharedResources sharedResources;
	private Socket clientSocket;
	private PrintWriter writer;
	private BufferedReader reader;
	private boolean isLogged;
	private String username;
	private int udpPort;
	
	public ClientHandler(Socket clientSocket, SharedResources sharedResources) throws IOException {
		this.clientSocket = clientSocket;
		this.sharedResources = sharedResources;
		this.writer = new PrintWriter(clientSocket.getOutputStream(), true);
        this.reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
	}

	public void run() {
		try {
			udpPort = getUdpPortPacket();
			while(!Thread.currentThread().isInterrupted())
				comunicateWithClient();
		}catch(ComunicationWithClientException e) {
			System.out.println(e.getMessage());
		}finally {
			logoutClient(username);
			clearResources();
		}
	}
	
	//Riceve il messaggio della porta UDP del client
	private int getUdpPortPacket() throws ComunicationWithClientException{
		try {
			String message = reader.readLine();
			return isValidPort(message);
		}catch (IOException e) {
			throw new ComunicationWithClientException("Invalid client port");
	    }
	}

	private int isValidPort(String message) throws ComunicationWithClientException {
		try {
            //Converte la stringa in un numero intero
            int port = Integer.parseInt(message);

            if(port >= 1024 && port <= 65535) {
            	return port;
            }
        } catch (Exception e) {}
		throw new ComunicationWithClientException("Invalid client port");
	}

	private void comunicateWithClient() throws ComunicationWithClientException {
		if (Thread.currentThread().isInterrupted()) {
	        throw new ComunicationWithClientException("Thread interrupted, closing connection.");
	    }
		JsonObject message = getMessageFromClient();
		String response = elaborateMessage(message);
		sendResponse(response);
	}

	//Riceve un messaggio dal client e lo converte in Json
	private JsonObject getMessageFromClient() throws ComunicationWithClientException {
		try {
	        return MessageConvertion.convertMessageToJson(reader.readLine());
	    } catch (IOException e) {
	        throw new ComunicationWithClientException("Error communicating with client: " + e.getMessage());
	    }catch (ConvertionException e) {
	        throw new ComunicationWithClientException("Error converting message to JSON: " + e.getMessage());
	    }catch(Exception e) {
	    	throw new ComunicationWithClientException("Client disconnected");
	    }
	}
	
	//Azione in base alla richiesta dell'utente
	private String elaborateMessage(JsonObject message) {
		String operation = message.get("operation").getAsString();
        switch(operation) {
	        case "register":
	        	return actionHandler(message, new RegisterStrategy(this));
	        case "updateCredentials":
	        	return actionHandler(message, new UpdateCredentialsStrategy(this));
	        case "login":
	        	return actionHandler(message, new LoginStrategy(this));
	        case "logout":
	        	return actionHandler(message, new LogoutStrategy(this));
	        case "insertLimitOrder":
	    		return actionHandler(message, new InsertLimitOrderStrategy(this));
	        case "insertMarketOrder":
	        	return actionHandler(message, new InsertMarketOrderStrategy(this));
	        case "insertStopOrder":
	        	return actionHandler(message, new InsertStopOrderStrategy(this));
	        case "cancelOrder":
	        	return actionHandler(message, new CancelOrderStrategy(this));
	        case "getPriceHistory":
	        	return actionHandler(message, new GetPriceHistoryStrategy(this));
        }
        return null;
	}
	
	private static String actionHandler(JsonObject message, ActionStrategy strategy) {
		JsonObject response = strategy.constructResponse(message);
		return response.toString();
	}
	
	private void sendResponse(String response) {
		writer.println(response);
	}
	
	protected int logoutClient(String username) {
		if(username == null)
			return 101;
		if(sharedResources.getLoggedUsers().remove(username) != null) {
			isLogged = false;
			return 100;
		}
		return 101;
	}
	private void clearResources() {
		try {
	        if (reader != null) {
	            reader.close();
	        }
	        if (writer != null) {
	            writer.close();
	        }
	        if (clientSocket != null && !clientSocket.isClosed()) {
	            clientSocket.close();
	        }
	    } catch (IOException e) {
	        System.out.println("Error while closing resources: " + e.getMessage());
	    }
		
	}

	protected String getUsername() {
		return username;
	}
	
	protected SharedResources getSharedResources() {
		return sharedResources;
	}
	
	protected boolean isLoggedUser() {
		return isLogged;
	}
	
	protected Socket getClientSocket() {
		return clientSocket;
	}

	protected void login() {
		isLogged = true;
	}
	protected void setUsername(String username) {
		this.username = username;
	}

	protected int getUdpPort() {
		return udpPort;
	}
}


/*
gestisce la fase di registrazione e di login degli utenti (incluse politiche di logout automatico in caso di inattività prolungata di un utente o interruzione della connessione)
memorizza le informazioni relative a tutti gli ordini ricevuti dagli utenti
riceve dall’utente le richieste di dati storici relativi agli ordini conclusi in un mese. Per ogni giorno del mese considerato, calcola prezzo di apertura, quello di chiusura, il prezzo massimo e quello minimo, e le invia al client.
*/