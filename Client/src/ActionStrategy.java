package cross.client;

import java.util.InputMismatchException;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Predicate;

import com.google.gson.JsonObject;

public abstract class ActionStrategy {
	
	public abstract boolean requiresLogin();
	public abstract boolean requiresLogout();
	public abstract Map<String, Object> getParameters(Scanner scanner);
	public abstract boolean evaluateResponse(JsonObject response);
	
	//Chiede in input una stringa
	protected static String retriveStringDataFromUser(Scanner scanner, String parameter){
		System.out.println("Insert "+parameter+": ");
		return scanner.nextLine();
	}
	//Chiede in input una stringa che rispetti un formato
	protected String retriveStringDataFromUserStricted(Scanner scanner, String string, Predicate<String> validator) {
		String data;
		while (true) {
			data = retriveStringDataFromUser(scanner, string);
	        if (validator.test(data)) {
	            return data;
	        }
	        System.out.println("Invalid parameter, try again");
	    }
	}
	//Ottiene un intero dall'utente
	protected static int retriveIntDataFromUser(Scanner scanner, String parameter){
		int value;
	    while (true) {
	    	System.out.println("Insert "+parameter+": ");
	        value = readInt(scanner);
	        //Ogni valore intero deve essere positivo
	        if (isPositive(value)) {
	            return value;
	        }
	    }
	}
	//Chiede in input un'intero finchè non è valido
	private static int readInt(Scanner scanner) {
		int value;
	    while (true) {
	        try {
	        	value = scanner.nextInt();
	        	scanner.nextLine();
	            return value;
	        } catch (InputMismatchException e) {
	            System.out.println("Invalid input. Please enter an integer.");
	            scanner.nextLine();  //Consuma l'input errato
	        }
	    }
	}
	//Stampa un messaggio se la risposta è positiva
	protected static boolean printSuccessMessage(String message) {
		System.out.println(message);
		return true;
	}
	protected static void printErrorMessage(String message, String errorMessage) {
		System.out.println(message + errorMessage);
	}
	protected static boolean isValidOrderType(String type) {
		if(type.equals("ask") || type.equals("bid")) {
			return true;
		}
		System.out.println("Invalid order type.");
		return false;
	}
	protected static boolean isPositive(int value) {
		if(value>0)
			return true;
		System.out.println("The value must be positive.");
		return false;
	}
}
