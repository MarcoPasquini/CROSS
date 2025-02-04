package cross.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import cross.utils.ConnectionParameters;

public class ServerConnection {
	//Istanza per singleton
    private static ServerConnection instance;
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;

    private ServerConnection(InetAddress host, int port, int timeout) throws IOException {
        socket = new Socket(host, port);
        socket.setSoTimeout(timeout);
        //Prepara stream per input e output
        writer = new PrintWriter(socket.getOutputStream(), true);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }
    
    //Se non esiste crea l'unica istanza possibile
    public static synchronized ServerConnection getInstance(InetAddress host, int port, int timeout) throws IOException {
        if (instance == null) {
            instance = new ServerConnection(host, port, timeout);
        }
        return instance;
    }
    public static synchronized ServerConnection getInstance() throws IOException {
        return instance;
    }

    public PrintWriter getWriter() {
        return writer;
    }
    public BufferedReader getReader() {
        return reader;
    }
    
    public boolean isValidConnection() {
    	return socket != null && !socket.isClosed() && socket.isConnected();
    }
    
    public void close() throws IOException {
    	if (reader != null) reader.close();
        if (writer != null) writer.close();
        if (socket != null) socket.close();
    }
    public static void connectToServer(ConnectionParameters parameters) throws ConnectionWithServerException {
		try {
	        ServerConnection.getInstance(parameters.getAddress(), parameters.getPort(), parameters.getConnectionTimeout());
	    } catch (IOException e) {
	    	throw new ConnectionWithServerException("Could not connect to "+ parameters.getAddress());
	    }
	}
	
}
