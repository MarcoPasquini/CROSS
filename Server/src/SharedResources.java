package cross.server;

import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.PriorityBlockingQueue;

public class SharedResources {
	private static int bidSize = 0;
	private static int askSize = 0;
	private ConcurrentHashMap<String, String> registeredUsers = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String, ConnectionInfo> loggedUsers = new ConcurrentHashMap<>();
	private PriorityBlockingQueue<Order> fulfilledOrders = new PriorityBlockingQueue<Order>(10, new TimeOrderComparator());
	private ConcurrentHashMap<String, ConcurrentLinkedQueue<Order>> toNofityOrders = new ConcurrentHashMap<>();
	private PriorityQueue<Order> bidLimitOrders = new PriorityQueue<>(10, new BidComparator());
	private PriorityQueue<Order> askLimitOrders = new PriorityQueue<>(10, new AskComparator());
	private PriorityQueue<Order> bidStopOrders = new PriorityQueue<>(10, new BidComparator());
	private PriorityQueue<Order> askStopOrders = new PriorityQueue<>(10, new AskComparator());
	private String registrationFile;
	private String ordersFile;
	private Object syncOrderHandler = new Object();
	private Object syncAuthHandler = new Object();
	
	public SharedResources(String registrationFile, String ordersFile) {
		this.registrationFile = registrationFile;
		this.ordersFile = ordersFile;
	}
	
	public PriorityBlockingQueue<Order> getFulfilledOrders() {
		return fulfilledOrders;
	}
	
	public ConcurrentHashMap<String, ConnectionInfo> getLoggedUsers() {
		return loggedUsers;
	}
	
	public ConcurrentHashMap<String, String> getRegisteredUsers() {
		return registeredUsers;
	}
	public int getBidSize() {
		return bidSize;
	}
	public int getAskSize() {
		return askSize;
	}
	public void addBidSize(int size) {
		bidSize+=size;
	}
	public void addAskSize(int size) {
		askSize+=size;
	}
	public PriorityQueue<Order> getBidLimitOrders() {
		return bidLimitOrders;
	}
	public PriorityQueue<Order> getAskLimitOrders() {
		return askLimitOrders;
	}
	public PriorityQueue<Order> getBidStopOrders() {
		return bidStopOrders;
	}
	public PriorityQueue<Order> getAskStopOrders() {
		return askStopOrders;
	}
	public ConcurrentHashMap<String, ConcurrentLinkedQueue<Order>> getToNotifyOrders() {
		return toNofityOrders;
	}

	public String getRegistrationFile() {
		return registrationFile;
	}
	public String getOrdersFile() {
		return ordersFile;
	}
	public Object getSyncOrderHandler() {
		return syncOrderHandler;
	}
	public Object getSyncAuthHandler() {
		return syncAuthHandler;
	}
	
}
