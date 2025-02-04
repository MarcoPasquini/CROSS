package cross.server;

import com.google.gson.JsonObject;

public class InsertStopOrderStrategy extends OrderStrategy {
	
	public InsertStopOrderStrategy(ClientHandler clientHandler) {
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
		Order order = retriveOrderValues(values, "stop");
		if(order == null)
			return -1;
		makeStopOrder(order);
		return order.getOrderId();
	}

	private void makeStopOrder(Order order) {
		boolean isBid = "bid".equals(order.getType());
		int price = order.getPrice();
		addStopOrder(order, isBid);
		checkStopOrders(price, isBid);
	}

	private void addStopOrder(Order order, boolean isBid) {
		if(isBid) {
			getSharedResources().getBidStopOrders().add(order);
			getSharedResources().addBidSize(order.getSize());
		}
		else
			getSharedResources().getAskStopOrders().add(order);
			getSharedResources().addAskSize(order.getSize());
	}
	
	
}