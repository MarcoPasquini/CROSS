package cross.server;

import java.net.InetAddress;

public class ConnectionInfo {
	private InetAddress address;
	private int port;
	
	public ConnectionInfo(InetAddress address, int port) {
		this.address = address;
		this.port = port;
	}
	
	public InetAddress getAddress() {
		return address;
	}
	
	public int getPort() {
		return port;
	}
}
