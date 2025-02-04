package cross.server;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

public class Order {
	private static AtomicInteger globalId = new AtomicInteger(1);
	private int orderId;
	private String orderCategory;
	private String type;
	private int size;
	private int price;
	private String username;
	private long timestamp;
	private int startSize;
	
	public Order(int orderId, String orderCategory, String type, int size, int price, String username) {
		this.orderId = orderId;
		this.orderCategory = orderCategory;
		this.type = type;
		this.size = size;
		this.startSize = size;
		this.price = price;
		this.username = username;
		Instant time = Instant.now();
		this.timestamp = time.getEpochSecond();
	}
	

	public Order(String orderCategory, String type, int size, int price, String username) {
		this.orderId = globalId.getAndIncrement();
		this.orderCategory = orderCategory;
		this.type = type;
		this.size = size;
		this.startSize = size;
		this.price = price;
		this.username = username;
		Instant time = Instant.now();
		this.timestamp = time.getEpochSecond();
	}
	
	public Order(String orderCategory, String type, int size, String username) {
		this.orderId = globalId.getAndIncrement();
		this.orderCategory = orderCategory;
		this.type = type;
		this.size = size;
		this.startSize = size;
		this.orderCategory = "market";
		this.username = username;
		Instant time = Instant.now();
		this.timestamp = time.getEpochSecond();
	}
	public int getOrderId() {
		return orderId;
	}
	public void setOrderId(int id) {
		orderId = id;
	}

	public String getOrderCategory() {
		return orderCategory;
	}

	public String getType() {
		return type;
	}

	public int getSize() {
		return size;
	}

	public int getPrice() {
		return price;
	}
	
	public String getUsername() {
		return username;
	}
	public long getTime() {
		return timestamp;
	}
	public void setSize(int size) {
		this.size = size;
	}

	public int getStartSize() {
		return startSize;
	}
	public static void setInitialOrderId(int id) {
		globalId = new AtomicInteger(id+1);
	}
	public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", orderCategory='" + orderCategory + '\'' +
                ", type='" + type + '\'' +
                ", size=" + size +
                ", price=" + price +
                ", username='" + username + '\'' +
                ", time=" + timestamp +
                ", startSize=" + startSize +
                '}';
    }
}
