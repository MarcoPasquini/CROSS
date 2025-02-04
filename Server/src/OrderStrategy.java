package cross.server;


import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.google.gson.JsonObject;

public abstract class OrderStrategy extends ActionStrategy {
	
	public OrderStrategy(ClientHandler clientHandler) {
		super(clientHandler);
	}

	public boolean requiresLogin() {
		return true;
	}
	public boolean requiresLogout() {
		return false;
	}
	
	public JsonObject constructResponse(JsonObject message) {
		synchronized(getSharedResources().getSyncOrderHandler()) {
			return constructStrategyResponse(message);
		}
	}
	public abstract JsonObject constructStrategyResponse(JsonObject message);

	private static final String MARKET = "market";
	
	protected static JsonObject createOrderResponse(int code) {
        JsonObject response = new JsonObject();
        response.addProperty("orderId", code);
        return response;
	}
	
	protected Order retriveOrderValues(JsonObject values, String category) {
		String type = retriveStringValue(values, "type");
		int size = retriveIntValue(values, "size");
		int price = 0;
		if(type == null || size == -1)
			return null;
		if(!category.equals(MARKET)) {
			price = retriveIntValue(values, "price");
			if(price ==-1)
				return null;
		}
		
		return new Order(category, type, size, price, getUsername());
	}
	protected PriorityQueue<Order> getOppositeLimitOrders(boolean isBid) {
		return isBid? getSharedResources().getAskLimitOrders() : getSharedResources().getBidLimitOrders();
	}
	//Una volta che un ordine è evaso, viene aggiunto a ordini evasi e da notificare
	protected void fulfillOrder(Order order) {
	    getSharedResources().getFulfilledOrders().add(order);
	    addToNotificationQueue(order);
	    synchronized(getSharedResources().getToNotifyOrders()) {
	    	//Sveglia thread per comunicazione UDP delle notifiche
	    	getSharedResources().getToNotifyOrders().notify();	    	
	    }
	}
	//Aggiorna la domanda e la richiesta globale
	protected void updateOrderBookSize(boolean isBid, int size) {
		if (isBid) {
	        getSharedResources().addAskSize(-size);
	    } else {
	        getSharedResources().addBidSize(-size);
	    }
	}
	
	//Gestisce il caso in cui un ordine non è totalmente evaso, suddivide l'ordine
	protected void processPartialMatch(Order order, Order last, boolean isBid) {
		last.setSize(last.getSize() - order.getSize());
        fulfillOrder(new Order(order.getOrderId(), order.getOrderCategory(), order.getType(), order.getSize(), last.getPrice(), order.getUsername()));
        fulfillOrder(new Order(last.getOrderId(), last.getOrderCategory(), last.getType(), order.getSize(), last.getPrice(), last.getUsername()));
        order.setSize(0);
	}
	
	//Gestisce il caso in cui un ordine è totalmente evaso
	protected void processFullMatch(Order order, Order last, PriorityQueue<Order> oppositeOrders, boolean isBid) {
		oppositeOrders.poll();
        order.setSize(order.getSize() - last.getSize());
        fulfillOrder(new Order(order.getOrderId(), order.getOrderCategory(), order.getType(), last.getSize(), last.getPrice(), order.getUsername()));
        fulfillOrder(last);
        checkStopOrders(last.getPrice(), isBid);
	}
	//Controlla presenza di ordini di tipo stop da attivare
	protected void checkStopOrders(int price, boolean isBid) {
		Order lastStopOrder = isBid 
		        ? getSharedResources().getAskStopOrders().peek() 
		        : getSharedResources().getBidStopOrders().peek();
		if(lastStopOrder == null || (isBid && lastStopOrder.getPrice() > price ) || (!isBid && lastStopOrder.getPrice() < price))
			return;
		makeMarketOrder(lastStopOrder);
	}
	
	protected void makeMarketOrder(Order order) {
		if(isFailMarketOrder(order)){
			order.setOrderId(-1);
			return;
		}
		elaborateMarketOrder(order);
	}
	//Controlla se un market order può essere evaso (richiesta globale sufficiente)
	protected boolean isFailMarketOrder(Order order) {
		String type = order.getType();
		int size = order.getSize();
		return (type.equals("bid") && size > getSharedResources().getAskSize()) || (type.equals("ask") && size > getSharedResources().getBidSize());
	}
	protected void elaborateMarketOrder(Order order) {
		boolean isBid = "bid".equals(order.getType());
		
		PriorityQueue<Order> oppositeOrders = getOppositeLimitOrders(isBid);
		
		processOrder(order, oppositeOrders, isBid);

	    updateOrderBookSize(isBid, order.getStartSize());
	}
	
	//Matcha ordine con la coda di ordini opposta
	protected void processOrder(Order order, PriorityQueue<Order> oppositeOrders, boolean isBid) {
		while (order.getSize() > 0) {
			Order last = oppositeOrders.peek();

	        if (last.getSize() <= order.getSize()) {
	        	processFullMatch(order, last, oppositeOrders, isBid);
	        } else {
	        	processPartialMatch(order, last, isBid);
	        }
		}
		
	}
	//Aggiunge ordine alla coda di notifica
	private void addToNotificationQueue(Order order) {
	    ConcurrentHashMap<String, ConcurrentLinkedQueue<Order>> toNotifyOrders = getSharedResources().getToNotifyOrders();
	    toNotifyOrders.computeIfAbsent(order.getUsername(), k -> new ConcurrentLinkedQueue<>()).add(order);
	}
	protected boolean isValidOrder(Order order) {
		return isValidTypeOrder(order.getType()) && isValidCategoryOrder(order.getOrderCategory()) && isValidPriceOrder(order.getPrice()) && isValidSizeOrder(order.getSize());
	}

	private boolean isValidTypeOrder(String type) {
		return type.equals("ask") || type.equals("bid");
	}
	
	private boolean isValidCategoryOrder(String category) {
		return category.equals("market") || category.equals("limit") || category.equals("stop");
	}
	
	private boolean isValidPriceOrder(int price) {
		return price>=0;
	}
	private boolean isValidSizeOrder(int size) {
		return size>0;
	}
	
}