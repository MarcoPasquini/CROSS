package cross.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public abstract class ActionStrategy {
	
	private ClientHandler clientHandler;

	
	public ActionStrategy(ClientHandler clientHandler) {
		this.clientHandler = clientHandler;
	}
	
	public abstract boolean requiresLogin();
	public abstract boolean requiresLogout();
	public abstract JsonObject constructResponse(JsonObject message);
	protected boolean hasLoginConflict() {
		return ((requiresLogin() && !isCurrentlyLoggedIn()) || (requiresLogout() && isCurrentlyLoggedIn()));
	}
	protected JsonObject getValidMessage(JsonObject message) {
		return message.getAsJsonObject("values");
	}
	protected String retriveStringValue(JsonObject values, String name) {
		JsonElement value = values.get(name);
		if(value == null) return null;
		return values.get(name).getAsString();
	}
	protected int retriveIntValue(JsonObject values, String name) {
		JsonElement value = values.get(name);
		if(value == null) return -1;
		return values.get(name).getAsInt();
	}
	protected boolean isValidString(String string) {
		return !(string == null || string.isBlank());
	}
	//Crea struttura di risposta con codice
	protected static JsonObject createCodeResponse(int code, String message) {
        JsonObject response = new JsonObject();
        response.addProperty("response", code);
        response.addProperty("errorMessage", message);
        return response;
	}
	protected String getUsername() {
		return clientHandler.getUsername();
	}
	protected void setLoggedIn(String username) {
		clientHandler.setUsername(username);
		clientHandler.login();
	}
	protected SharedResources getSharedResources() {
		return clientHandler.getSharedResources();
	}
	protected boolean isCurrentlyLoggedIn() {
		return clientHandler.isLoggedUser();
	}
	protected int logoutUser() {
		return clientHandler.logoutClient(clientHandler.getUsername());
	}
	
}
