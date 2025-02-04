package cross.server;

import java.util.Map;
import java.util.PriorityQueue;

import com.google.gson.JsonObject;

public class CancelOrderStrategy extends OrderStrategy {
	
	private static final Map<Integer, String> CODES = Map.of(
		    100, "Order succesfully cancelled",
		    101, "Order does not exits or belongs to an other user"
		);
	
	public CancelOrderStrategy(ClientHandler clientHandler) {
		super(clientHandler);
	}
	
	public JsonObject constructStrategyResponse(JsonObject message) {
		if(hasLoginConflict()) {
			return createCodeResponse(101, "You must be logged in to perform this action");
		}
		int code = getResponseCode(message);
		return createCodeResponse(code, CODES.get(code));
	}
	
	private int getResponseCode(JsonObject message) {

		JsonObject values = message.getAsJsonObject("values");
		if(values == null) return 101;
		int orderId = retriveIntValue(values, "orderId");
		
		if(orderId == -1)
			return 101;
		//Se richiesta valida, tenta rimozione
		return removeOrder(orderId);
	}
	
	private int removeOrder(int orderId) {
		SharedResources sharedResources = getSharedResources();
		//Controllo su tutte le strutture contenenti ordini non evasi
		if(removeOrderFrom(sharedResources.getBidLimitOrders(), orderId, true)) {
			return 100;
		}
		if(removeOrderFrom(sharedResources.getAskLimitOrders(), orderId, false)) {
			return 100;
		}
		if(removeOrderFrom(sharedResources.getBidStopOrders(), orderId, true)) {
			return 100;
		}
		if(removeOrderFrom(sharedResources.getAskStopOrders(), orderId, false)) {
			return 100;
		}
		return 101;
	}
	
	//Scansiona le code con gli ordini
	private boolean removeOrderFrom(PriorityQueue<Order> placedOrders, int orderId, boolean isBid) {
		//Coda temporanea per elementi rimanenti
		PriorityQueue<Order> tempQueue;
		if(isBid)
			tempQueue = new PriorityQueue<Order>(new BidComparator());
		else
			tempQueue = new PriorityQueue<Order>(new AskComparator());
        boolean orderRemoved = false;
        
        //Itera la coda
        while (!placedOrders.isEmpty()) {
            Order order = placedOrders.poll();
            //Controlla id e autore dell'ordine
            if (order.getOrderId() == orderId && order.getUsername().equals(getUsername())) {
                orderRemoved = true;
                break;
            } else {
                tempQueue.offer(order);
            }
        }

        //Riempie la coda originale con quella temporanea (tutti tranne elemento rimosso)
        while (!tempQueue.isEmpty()) {
        	placedOrders.offer(tempQueue.poll());
        }

        return orderRemoved;
	}
	
}