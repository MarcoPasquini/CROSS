package cross.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

public class ConfigurationReader {

	private static final String FILE_PATH = "resources/config.properties";
	private static final String SERVER_HOST = "server.host";
    private static final String SERVER_PORT = "server.port";
    private static final String CONNECTION_TIMEOUT = "connection.timeout";
    private static final String REGISTRATION_FILE = "file.registration";
    private static final String ORDERS_FILE = "file.orders";
    private static final int MIN_PORT_VALUE = 1024;
    private static final int MAX_PORT_VALUE = 65535;
	
	public static ConnectionParameters getConnectionProperties(boolean isServer) throws ConfigurationException {
		
		Properties props = loadProperties(FILE_PATH);
		InetAddress host = null;

		if(isServer) {
			host = getPolishedAddress(props);
		}
		int port = getPolishedPort(props);
		int timeout = getPolishedTimeout(props);
		
        return new ConnectionParameters(host, port, timeout);
	}
	
	public static String[] getFileProperties() throws ConfigurationException {
		
		Properties props = loadProperties(FILE_PATH);

		String registrationFile = getPolishedFile(props, REGISTRATION_FILE);
		String ordersFile = getPolishedFile(props, ORDERS_FILE);
		
		String[] files = {registrationFile, ordersFile};
        return files;
	}
	
	
	//Funzione per caricare le proprietà da un file
	private static Properties loadProperties(String filePath) throws ConfigurationException {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(filePath)) {
            props.load(fis);
        } catch (FileNotFoundException ex) {
        	throw new ConfigurationException("Configuration file not found: " + filePath, ex);
        } catch (IOException ex) {
        	throw new ConfigurationException("Error reading configuration file: " + filePath, ex);
        }
        return props;
    }
	
	private static String getPolishedFile(Properties props, String name) throws ConfigurationException{
		String fileName = retriveProperty(props, name);
		return getAdjustedFile(fileName);
	}
	
	private static String getAdjustedFile(String fileName) throws ConfigurationException {
		if (fileName == null || fileName.isBlank() || !fileName.toLowerCase().endsWith(".json")) {
            throw new ConfigurationException("Invalid file");
        }
        return fileName;
	}

	private static InetAddress getPolishedAddress(Properties props) throws ConfigurationException{
		String sAddress = retriveProperty(props, SERVER_HOST);
		return getAdjustedAddress(sAddress);
	}
	private static int getPolishedPort(Properties props) throws ConfigurationException{
		String sPort = retriveProperty(props, SERVER_PORT);
		return getAdjustedPort(sPort);
	}
	private static int getPolishedTimeout(Properties props) throws ConfigurationException{
		String sTimeout = retriveProperty(props, CONNECTION_TIMEOUT);
		return getAdjustedTimeout(sTimeout);
	}
	
	private static String retriveProperty(Properties props, String name) throws ConfigurationException{
		String parameter = props.getProperty(name);
		isValidParameter(parameter, name);
		return parameter;
	}
	
	private static void isValidParameter(String parameter, String name) throws ConfigurationException{
		if(parameter == null) {
			throw new ConfigurationException("Missing property: "+ name);
		}
	}
	
	private static InetAddress getAdjustedAddress(String sAddress) throws ConfigurationException{
		try {
            InetAddress address = InetAddress.getByName(sAddress);
            return address;
        } catch (UnknownHostException ex) {
        	throw new ConfigurationException("Could not find the address", ex);
        }
	}
	private static int getAdjustedPort(String sPort) throws ConfigurationException{
		try {
            int port = Integer.parseInt(sPort);
            isValidPort(port);
            return port;
        } catch (NumberFormatException ex) {
        	throw new ConfigurationException("Port format not valid", ex);
        }
	}
	private static int getAdjustedTimeout(String sTimeout) throws ConfigurationException{
		try {
            int timeout = Integer.parseInt(sTimeout);
            isValidTimeout(timeout);
            return timeout;
        } catch (NumberFormatException ex) {
        	throw new ConfigurationException("Timeout format not valid", ex);
        }
	}
	private static void isValidPort(int port) throws ConfigurationException{
		if(port < MIN_PORT_VALUE || port > MAX_PORT_VALUE) {
			throw new ConfigurationException("Port not valid");
		}
	}
	private static void isValidTimeout(int timeout) throws ConfigurationException{
		if(timeout < 0) {
			throw new ConfigurationException("Timeout not valid");
		}
	}
}
