package cross.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cross.utils.ConfigurationException;
import cross.utils.ConfigurationReader;
import cross.utils.ConnectionParameters;

public class ServerMain {
	
	private static final TimeUnit SYSTEM_TIME_UNIT = TimeUnit.SECONDS;
	private static final int KEEP_ALIVE_TIME = 10;
	private static String registrationFile = null;
	private static String ordersFile = null;
	
	public static void main(String[] args) {
		
		ConnectionParameters parameters = getConfigurationParameters();
		
		SharedResources sharedResources = initDataStructures(registrationFile, ordersFile);
		
		//Inizializza i thread del server
		ScheduledExecutorService scheduler = initBackUpThread(sharedResources);
		Thread notifier = initNotifierThread(sharedResources);
		ThreadPoolExecutor executor = createPoolForClients();
		
		addShutdownHook(executor, scheduler, notifier);
		
		TcpServerHandler.startTcpSocketHandler(executor, sharedResources, parameters);
        
		endServer(executor, scheduler, notifier);
	}


	//Prende i parametri di configurazione
	private static ConnectionParameters getConfigurationParameters() {
			try {
				//Prende i file path dal file di configurazione
				String[] fileParameters = ConfigurationReader.getFileProperties();
				registrationFile = fileParameters[0];
				ordersFile = fileParameters[1];
				//Prende i parametri per la connessione dal file di configurazione
				return ConfigurationReader.getConnectionProperties(false);
			}catch(ConfigurationException ex) {
				System.out.println("Error while accessing configuration properties: " + ex.getMessage());
				//Termina il programma con codice di errore 1
			    System.exit(1);
			    return null;
			}
	}

	//Inizializza le strutture dati condivise
	private static SharedResources initDataStructures(String registrationFile, String ordersFile) {
		SharedResources sharedResources = new SharedResources(registrationFile, ordersFile);
		initRegisteredUsers(sharedResources);
		initLastOrderId();
		return sharedResources;
	}
	

	private static void initRegisteredUsers(SharedResources sharedResources) {
		File input = new File(registrationFile);
		JsonArray trades = getTradesFromFile(input);
		addTradesToMap(sharedResources.getRegisteredUsers(), trades);
	}


	private static JsonArray getTradesFromFile(File input) {
		try {
			JsonElement fileElement = JsonParser.parseReader(new FileReader(input));
			JsonObject fileObject = fileElement.getAsJsonObject();
			return fileObject.getAsJsonArray("registeredUsers");
		}catch (FileNotFoundException e) {
			System.out.println("Impossibile trovare il file specificato");
			System.exit(1);
			return null;
		}
	}
	private static void addTradesToMap(ConcurrentHashMap<String, String> registeredUsers, JsonArray trades) {
		if(trades == null) return;
		for (JsonElement trade : trades) {
			JsonObject jsonObject = trade.getAsJsonObject();
			
			String username = jsonObject.get("username").getAsString();
			String password = jsonObject.get("password").getAsString();
			
			registeredUsers.put(username, password);
		}
	}
	
	private static void initLastOrderId() {
		
        try {
            // Legge il file JSON
            FileReader reader = new FileReader(ordersFile);
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            // Estrai l'array di "trades"
            JsonArray trades = jsonObject.getAsJsonArray("trades");
            // Ottieni l'ultimo ordine
            int lastOrderId = getLastOrderId(trades);
            Order.setInitialOrderId(lastOrderId);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	private static int getLastOrderId(JsonArray trades) {
	    // Verifica che ci siano ordini
	    if (trades == null || trades.size() == 0)
	        return 0;
	
	    // L'ultimo ordine è l'ultimo elemento dell'array
	    JsonObject lastTrade = trades.get(trades.size() - 1).getAsJsonObject();
	    
	    return lastTrade.get("orderId").getAsInt();
	}
	
	//Avvia il thread per il backup
	private static ScheduledExecutorService initBackUpThread(SharedResources sharedResources) {
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		//Eseguito ogni 10 minuti
		scheduler.scheduleWithFixedDelay(new DumpExecutor(sharedResources), 0, 10, TimeUnit.MINUTES);
		return scheduler;
	}
	
	//Avvia il thread che gestisce le notifiche
	private static Thread initNotifierThread(SharedResources sharedResources) {
        return createAndStartThread(new NotifyUDP(sharedResources), "NotifyUDPThread");
    }

    private static Thread createAndStartThread(Runnable runnable, String threadName) {
        Thread thread = new Thread(runnable, threadName);
        thread.start();
        return thread;
    }
    
    //Inizializza il pool di thread per la gestione dei client
    private static ThreadPoolExecutor createPoolForClients() {
    	int corePoolSize = getCorePoolSize();
    	//Operazioni IO bound, i thread core sono il doppio di quelli disponibili
        int maximumPoolSize = 2*corePoolSize;
        long keepAliveTime = KEEP_ALIVE_TIME;
        TimeUnit timeUnitForKeepAlive = SYSTEM_TIME_UNIT;
        BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>();

        return new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                timeUnitForKeepAlive,
                taskQueue
            );
	}

    //Numero di thread disponibili nella macchina
	private static int getCorePoolSize() {
		return Runtime.getRuntime().availableProcessors()*2;
	}

	//Termina l'esecuzione del server
	private static void endServer(ThreadPoolExecutor executor, ScheduledExecutorService scheduler, Thread notifier) {
		if(executor != null) {
    		System.out.println("Shutting down thread pool");
    		executor.shutdown();
    	}
		if(scheduler != null) {
			System.out.println("Shutting down backup thread");
			scheduler.shutdown();
		}
		if(notifier != null) {
			System.out.println("Shutting down notifier thread");
			notifier.interrupt();
			try {
				notifier.join();				
			}catch(InterruptedException e) {}
		}
	}

	//Gestire la terminazione dell'applicazione (Ctrl+C)
    private static void addShutdownHook(ThreadPoolExecutor executor, ScheduledExecutorService scheduler, Thread notifier) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            endServer(executor, scheduler, notifier);

        }));
    }
	
}
