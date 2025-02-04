package cross.server;

import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.JsonObject;

public class LoginStrategy extends AuthenticationStrategy {
	
	private static Socket clientSocket;
	private static int udpPort;
	private static final Map<Integer, String> CODES = Map.of(
			100, "",
			101, "Non existent username or wrong password",
			102, "User already logged in",
			103, "Unexpected error"
			);

	public LoginStrategy(ClientHandler clientHandler) {
		super(clientHandler);
		clientSocket = clientHandler.getClientSocket();
		udpPort = clientHandler.getUdpPort();
	}
	
	public JsonObject constructStrategyResponse(JsonObject message) {
		if(hasLoginConflict()) {
			return createCodeResponse(103, "You are already logged in");
		}
		int code = getResponseCode(message);
		return createCodeResponse(code, CODES.get(code));
	}
	
	private int getResponseCode(JsonObject message) {
		JsonObject values = message.getAsJsonObject("values");
		if(values == null) return 103;
		
		String username = retriveStringValue(values, "username");
		String password = retriveStringValue(values, "password");
		if(!isValidString(password) || !isValidString(username)) {
			return 101;
		}
		
		return loginUser(username, password);
	}

	private int loginUser(String username, String password) {
		ConcurrentHashMap<String, String> registeredUsers = getSharedResources().getRegisteredUsers();
		ConcurrentHashMap<String, ConnectionInfo> loggedUsers = getSharedResources().getLoggedUsers();
		if(!registeredUsers.containsKey(username) || !registeredUsers.get(username).equals(password)) {
			return 101;
		}
		if(loggedUsers.putIfAbsent(username, new ConnectionInfo(clientSocket.getInetAddress(), udpPort)) == null) {
			setLoggedIn(username);
			return 100;
		}
		else
			return 102;
	}
}