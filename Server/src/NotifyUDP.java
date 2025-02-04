package cross.server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class NotifyUDP implements Runnable {
	
	private SharedResources sharedResources;

	public NotifyUDP(SharedResources sharedResources) {
		this.sharedResources = sharedResources;
	}

	public void run() {
		try {
			processOrders();
		}catch(InterruptedException e) {}
	}

	private void processOrders() throws InterruptedException {
		while(true) {
			if(!nothingToNotify()) {
				notifyHandler();
			}
			synchronized(sharedResources.getToNotifyOrders()) {
				if (Thread.currentThread().isInterrupted()) {
                    return;
                }
				//Attende nuove notifiche
				sharedResources.getToNotifyOrders().wait();
			}
		}
	}
	private boolean nothingToNotify() {
		return sharedResources.getToNotifyOrders().isEmpty();
	}
	
	private void notifyHandler() {
		Iterator<Map.Entry<String, ConcurrentLinkedQueue<Order>>> iterator = sharedResources.getToNotifyOrders().entrySet().iterator();
		//Per ogni utente crea un array di notifiche da mandare
        while (iterator.hasNext()) {
            Map.Entry<String, ConcurrentLinkedQueue<Order>> trades = iterator.next();
            ConnectionInfo connection = retriveConnectionInfoOfUsername(trades.getKey());
            if (connection != null){
            	String message = constructNotificationMessage(trades.getValue());
            	sendNotification(message, connection);            	
            }
            iterator.remove();
        }
	}

	private ConnectionInfo retriveConnectionInfoOfUsername(String username) {
		return sharedResources.getLoggedUsers().get(username);
	}
	//Costruisce messaggio di notifica, elencandole per ogni utente
	private String constructNotificationMessage(ConcurrentLinkedQueue<Order> orders) {
		JsonObject message = new JsonObject();
		
		message.addProperty("notification", "closedTrades");
		
		JsonArray trades = new JsonArray();
		
        for (Order order : orders) {
        	trades.add(retriveTrade(order));
        }
        message.add("trades", trades);
		
		return message.toString();
	}
	
	private JsonObject retriveTrade(Order orderToNotify) {
		JsonObject trade = new JsonObject();
		trade.addProperty("orderId", orderToNotify.getOrderId());
		trade.addProperty("type", orderToNotify.getType());
		trade.addProperty("orderType", orderToNotify.getOrderCategory());
		trade.addProperty("size", orderToNotify.getSize());
		trade.addProperty("price", orderToNotify.getPrice());
		trade.addProperty("timestamp", orderToNotify.getTime());
		
		return trade;
	}
	private void sendNotification(String message, ConnectionInfo connection) {
		try (DatagramSocket socket = new DatagramSocket();) {
	        
	        byte[] buffer = message.getBytes();
	        
	        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, connection.getAddress(), connection.getPort());
	        
	        socket.send(packet);
		}catch(Exception e) {}
	}
	
}




/*
comunica agli utenti interessati l’avvenuta esecuzione di un ordine
*/