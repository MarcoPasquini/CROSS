package cross.server;

import java.util.PriorityQueue;

import com.google.gson.JsonObject;

public class InsertLimitOrderStrategy extends OrderStrategy {
	
	public InsertLimitOrderStrategy(ClientHandler clientHandler) {
		super(clientHandler);
	}

	public JsonObject constructStrategyResponse(JsonObject message) {
		if(hasLoginConflict())
			return createOrderResponse(-1);
		int orderId = getResponseId(message);
		return createOrderResponse(orderId);
	}

	private int getResponseId(JsonObject message) {
		JsonObject values = message.getAsJsonObject("values");
		if(values == null)
			return -1;
		Order order = retriveOrderValues(values, "limit");
		if(order == null)
			return -1;
		//Se richiesta valida piazza limit order
		makeLimitOrder(order);
		return order.getOrderId();
	}

	private void makeLimitOrder(Order order) {
		boolean isBid = "bid".equals(order.getType());
		PriorityQueue<Order> oppositeOrders = getOppositeLimitOrders(isBid);

		//Evade l'ordine se possibile, altrimenti solo piazzato
	    while (order.getSize() > 0 && !oppositeOrders.isEmpty()) {
	        Order last = oppositeOrders.peek();

	        if (last == null || order.getPrice() < last.getPrice()) {
	            insertNewLimitOrder(order, isBid);
	            return;
	        }

	        if (order.getSize() < last.getSize()) {
	            updateOrderBookSize(isBid, order.getSize());
	            processPartialMatch(order, last, isBid);
	            return;
	        } else {
	            updateOrderBookSize(isBid, order.getSize());
	            processFullMatch(order, last, oppositeOrders, isBid);
	        }
	    }

	    if (order.getSize() > 0) {
	        insertNewLimitOrder(order, isBid);
	    }
	}

	private void insertNewLimitOrder(Order order, boolean isBid) {
		if(isBid) {
			getSharedResources().getBidLimitOrders().add(order);
			getSharedResources().addBidSize(order.getSize());	
		}
		else {
			getSharedResources().getAskLimitOrders().add(order);
			getSharedResources().addAskSize(order.getSize());
			
		}
	}
}