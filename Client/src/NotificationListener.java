package cross.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import com.google.gson.*;

import cross.utils.ConvertionException;
import cross.utils.MessageConvertion;

public class NotificationListener implements Runnable {
    private static final int BUFFER_SIZE = 2048;
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static Object syncConsole; //Oggetto su cui sincronizzare la console con thread TCP
    private static DatagramSocket socket; //Socket su cui leggere messaggi UDP

    public NotificationListener(DatagramSocket socketListener, Object sharedMonitor) {
    	socket = socketListener;
    	syncConsole = sharedMonitor;
    }

    public void run() {
    	try {
	    	//Legge ed elabora notifiche ricevute
	    	getNotification();
    	}catch(SocketException e) {
    		//Thread termina correttamente
    	}catch(ListenerException e) {
    		System.out.println("UDP Thread error: "+e.getMessage());
    	}finally {
    		closeUDPSocket();
    	}
    }
    
    

    

    //Attende ed elabora una notifica
	private static void getNotification() throws ListenerException, SocketException {
    	while (!Thread.currentThread().isInterrupted()) {
            
            //Attende un pacchetto
            DatagramPacket packet = receivePacket();

            String message = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);

            JsonObject jsonObject;
            try {
            	jsonObject = MessageConvertion.convertMessageToJson(message);
            }catch(ConvertionException e) {
            	throw new ListenerException(e.getMessage(), e);
            }
            synchronized (syncConsole) {
            	printNotification(jsonObject);
            	ClientMain.printMenu();
            }
        }
    }
	
    private static DatagramPacket receivePacket() throws ListenerException, SocketException {
        DatagramPacket packet = preparePacket();
        try {
            socket.receive(packet);
            return packet;
        }catch(SocketException e) {
        	//Chiusura thread, deve essere rilanciata altrimenti catturata da IOException
        	throw new SocketException();
        }catch(IOException e) {
            throw new ListenerException("Error receiving packet", e);
        }
    }
    
    private static void closeUDPSocket() {
    	if(socket != null && !socket.isClosed()) //Controlla se già
    		socket.close();
	}
    
    private static DatagramPacket preparePacket() {
    	//Buffer per i dati in arrivo
    	byte[] buffer = new byte[BUFFER_SIZE];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        return packet;
    }
    
    private static void printNotification(JsonObject jsonObject) throws ListenerException {
    	//Controlla che la notifica sia valida
        if (!jsonObject.has("notification") || jsonObject.get("notification").isJsonNull()) {
            throw new ListenerException("The received message does not contain a valid 'notification' field.");
        }
        System.out.println("\n\nOrder Update:\n");
        printTradesUpdate(jsonObject.getAsJsonArray("trades"));
    }
    
    private static void printTradesUpdate(JsonArray trades) {
    	
		for (int i = 0; i < trades.size(); i++) {
			
			//Ottiene i dati sul trade a partire dal JSON
            JsonObject trade = trades.get(i).getAsJsonObject();
            int orderId = trade.get("orderId").getAsInt();
            String type = trade.get("type").getAsString();
            String orderType = trade.get("orderType").getAsString();
            float size = trade.get("size").getAsInt();
            float price = trade.get("price").getAsInt();
            long timestamp = trade.get("timestamp").getAsLong();
            String time = longToDate(timestamp);
            
            printOrder(type, orderId, orderType, size, price, time);
        }
    }
    
    private static void printOrder(String type, int orderId, String orderType, float size, float price, String time) {
    	
		System.out.println("----------------------");
        System.out.println("Your " + type + " order with ID: " + orderId);
        System.out.println("Order details:");
        System.out.println("   - Order Type: " + orderType);
        System.out.println("   - Size: " + size);
        System.out.println("   - Filled successfully at the price of: $" + price);
        System.out.println("\nThis order was completed on: " + time);
        System.out.println("----------------------\n");
	}
    
    private static String longToDate(long timestamp) {
    	LocalDateTime dateTime = convertTimestampToLocalDate(timestamp);
        
        return formattedDateTime(dateTime, DATE_FORMAT);
    }
    
    private static LocalDateTime convertTimestampToLocalDate(long timestamp) {
    	return Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
    //Converte la data in una stringa
    private static String formattedDateTime(LocalDateTime dateTime, String format) {
    	
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return dateTime.format(formatter);
    }
}