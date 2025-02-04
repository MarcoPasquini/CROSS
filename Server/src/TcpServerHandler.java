package cross.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ThreadPoolExecutor;

import cross.utils.ConnectionParameters;

public class TcpServerHandler {

	//Avvia la comunicazione TCP con i client
	public static void startTcpSocketHandler(ThreadPoolExecutor executor, SharedResources sharedResources, ConnectionParameters parameters) {
		try (ServerSocket serverSocket = new ServerSocket(parameters.getPort())) {

            listenForConnections(executor, sharedResources, serverSocket);

        } catch (IOException e) {
            System.out.println("Error creating socket: " + e.getMessage());
        } finally {
            //Arresta il pool di thread in caso di errore
        	executor.shutdown();
        }
		
	}

	//Attende e accetta connessioni dai client
	private static void listenForConnections(ThreadPoolExecutor executor, SharedResources sharedResources, ServerSocket serverSocket) {
		 try {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                
                //Delega la gestione del client a un thread nel pool
                executor.execute(new ClientHandler(clientSocket, sharedResources));
            }
        } catch (IOException e) {
            System.out.println("Error while accepting connection: " + e.getMessage());
        }
		
	}
}
