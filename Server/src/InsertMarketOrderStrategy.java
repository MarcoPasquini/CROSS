package cross.server;


import com.google.gson.JsonObject;

public class InsertMarketOrderStrategy extends OrderStrategy {
	
	public InsertMarketOrderStrategy(ClientHandler clientHandler) {
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
		Order order = retriveOrderValues(values, "market");
		if(order == null)
			return -1;
		makeMarketOrder(order);
		return order.getOrderId();
	}
}
