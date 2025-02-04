package cross.server;

public class OrderDTO implements Comparable<OrderDTO> {
	private int orderId;
	private String type;
	private int size;
	private int price;
	private long timestamp;
	
	public OrderDTO(int orderId, String type, int size, int price, long timestamp) {
		this.orderId = orderId;
		this.type = type;
		this.size = size;
		this.price = price;
		this.timestamp = timestamp;
	}
	
	public int compareTo(OrderDTO other) {
        return Long.compare(this.orderId, other.orderId);
    }
}