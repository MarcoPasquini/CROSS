package cross.utils;

import java.net.InetAddress;

public class ConnectionParameters {
	private InetAddress address;
	private int port;
	private int connectionTimeout;
	
	public ConnectionParameters() {}
	
	public ConnectionParameters(InetAddress address, int port, int connectionTimeout) {
		this.address = address;
		this.port = port;
		this.connectionTimeout = connectionTimeout;
	}
	
	public InetAddress getAddress() {
		return this.address;
	}
	
	public int getPort() {
		return this.port;
	}
	
	public int getConnectionTimeout() {
		return this.connectionTimeout;
	}
	
}