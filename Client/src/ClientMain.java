
package cross.client;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.InputMismatchException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

import com.google.gson.JsonObject;

import cross.utils.ConfigurationException;
import cross.utils.ConfigurationReader;
import cross.utils.ConnectionParameters;

public class ClientMain {
	
	private static final Object syncConsole = new Object();
	private static ActionStrategy actionStrategy;
	private static boolean isLogged;
	private static final Map<String, ActionStrategy> STRATEGIES = Map.of(
		    "register", new RegisterStrategy(),
		    "updateCredentials", new UpdateCredentialsStrategy(),
		    "login", new LoginStrategy(),
		    "logout", new LogoutStrategy(),
		    "insertLimitOrder", new InsertLimitOrderStrategy(),
		    "insertMarketOrder", new InsertMarketOrderStrategy(),
		    "insertStopOrder", new InsertStopOrderStrategy(),
		    "cancelOrder", new CancelOrderStrategy(),
		    "getPriceHistory", new GetPriceHistoryStrategy()
		);
	private static final Scanner scanner = new Scanner(System.in);
	public static DatagramSocket socketListener;
	public static Thread uDpListener;
	
	public static void main(String[] args) {
		
			ConnectionParameters parameters = getParameters();
			
			startListener();
			
			uDpListener = startUDPListener();
			
			addShutdownHook();
			
			handleServerConnection(parameters);

			comunicateUdpPortToServer(socketListener.getLocalPort());
	        
			cliHandler();
			
	        stopExecution();
    }
	
	//Inizializza socket UDP
	private static void startListener() {
		try {
			socketListener = new DatagramSocket();
		} catch (SocketException e) {
		    System.out.println("Failed to initialize UDP socket");
		    stopExecution();
		}
    }

	private static void comunicateUdpPortToServer(int port) {
		try {
			ServerComunication.sendMessage(port+"");
		}catch(ComunicationWithServerException e) {
			System.out.println(e.getMessage());
			stopExecution();
			System.exit(1);
		}
	}

	//Connette il thread TCP al server
	private static void handleServerConnection(ConnectionParameters parameters) {
		try {
			ServerConnection.connectToServer(parameters);				
		}catch(ConnectionWithServerException e) {
			System.out.println(e.getMessage());
			stopExecution();
			System.exit(1);
		}
	}

	//Crea e avvia thread UDP
	private static Thread startUDPListener() {
		Thread UDPListener = new Thread(new NotificationListener(socketListener, syncConsole));
        UDPListener.start();
        return UDPListener;
	}

	//Prende i parametri di configurazione
	private static ConnectionParameters getParameters() {
		try {
			return ConfigurationReader.getConnectionProperties(true);
		}catch(ConfigurationException ex) {
			System.out.println("Error while accessing connection properties: " + ex.getMessage());
			//Termina il programma con codice di errore 1
		    System.exit(1);
		    return null;
		}
	}

	//Gestione interazione con l'utente
	private static void cliHandler() {
		Scanner scanner = new Scanner(System.in);
		int choice;
		do{
			printMenu();
			choice = getchoice(scanner);
			responseTochoice(choice);
		}while(choice != 0);
	}
	
	protected static void printMenu() {
		synchronized(syncConsole) {
			System.out.println("\n--- CROSS Service Menu ---");
	        System.out.println("1. Register (username, password)");
	        System.out.println("2. Update Credentials (username, currentPassword, newPassword)");
	        System.out.println("3. Login (username, password)");
	        System.out.println("4. Logout (username)");
	        System.out.println("5. Insert Limit Order (type, size, limitPrice)");
	        System.out.println("6. Insert Market Order (type, size)");
	        System.out.println("7. Insert Stop Order (type, size, stopPrice)");
	        System.out.println("8. Cancel Order (orderID)");
	        System.out.println("9. Get Price History (month)");
	        System.out.println("0. Exit");
	        System.out.print("Choose an option: ");
		}
	}
	private static int getchoice(Scanner scanner) {
		int choice = -1; // Valore predefinito per scelte non valide
	    try {
	        choice = scanner.nextInt();
	        scanner.nextLine(); // Consuma la linea rimanente dopo il numero
	    } catch (InputMismatchException e) {
	        scanner.nextLine(); // Consuma la linea rimanente (stringa non valida)
	    }catch(NoSuchElementException e) {
	    	return 0;
	    }
	    return choice;
	}
	private static void responseTochoice(int choice){
		switch (choice) {
	    case 1 -> executeAction("register");
	    case 2 -> executeAction("updateCredentials");
	    case 3 -> executeAction("login");
	    case 4 -> executeAction("logout");
	    case 5 -> executeAction("insertLimitOrder");
	    case 6 -> executeAction("insertMarketOrder");
	    case 7 -> executeAction("insertStopOrder");
	    case 8 -> executeAction("cancelOrder");
	    case 9 -> executeAction("getPriceHistory");
	    case 0 -> {
	        System.out.println("Exiting...");
	    }
	    default -> System.out.println("Invalid choice. Please try again.");
		}
	}
	
	private static void setActionStrategy(ActionStrategy strategy) {
		actionStrategy = strategy;
	}
	
	private static void executeAction(String action) {
	    setActionStrategy(STRATEGIES.get(action));
	    if(isLoginConflict(actionStrategy)) return;
	    try {
	    	boolean success = actionHandler(action);
	    	updateLoginStatus(action, success);	    	
	    }catch(ComunicationWithServerException e) {
	    	if(e.getMessage().endsWith("Connection reset"))
	    		stopExecution();
	    	System.out.println(e.getMessage());
	    }
	    
	}
	//Inoltra al server la richiesta dell'utente ed elabora la risposta
	private static boolean actionHandler(String action) throws ComunicationWithServerException{
		Map<String, Object> valuesParameters = actionStrategy.getParameters(scanner);
		synchronized(syncConsole) {
			JsonObject response = ServerComunication.executeServerRequest(valuesParameters, action);
			return actionStrategy.evaluateResponse(response);			
		}
	}
	
	private static boolean isLoginConflict(ActionStrategy strategy) {
		if (strategy.requiresLogin() && !isLogged) {
	    	System.out.println("You must be logged in to perform this action.");
	    	return true;
	    }
	    if (strategy.requiresLogout() && isLogged) {
	    	System.out.println("You must not be logged in to perform this action.");
	    	return true;
	    }
	    return false;
	}
	
	private static void updateLoginStatus(String action, boolean success) {
		if ("login".equals(action) && success) isLogged = true;
	    if ("logout".equals(action) && success) isLogged = false;
	}
	private static void stopExecution() {
		scanner.close();
		closeServerSocket();
		waitUDPThread();
	}

	private static void waitUDPThread() {
		try {
			//Interrompe il thread UDP
			if(socketListener != null)
				socketListener.close();
			//Attende terminazione
			uDpListener.join();
		}catch(InterruptedException e) {
			System.out.println("Main thread interrupted while waiting");
		}
	}

	//Chiude socket TCP
	private static void closeServerSocket() {
		try {
			ServerConnection.getInstance().close();
		}catch(IOException e) {
			System.out.println("Error while closing server socket.");
		}catch(Exception e) {
			//Socket server già chiuso
		}
	}
	
	//Gestire la terminazione dell'applicazione (Ctrl+C)
    private static void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        	stopExecution();
        }));
    }
	
}
