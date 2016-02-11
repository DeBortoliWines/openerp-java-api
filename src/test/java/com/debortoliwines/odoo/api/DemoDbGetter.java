package com.debortoliwines.odoo.api;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import com.debortoliwines.odoo.api.OpenERPXmlRpcProxy.RPCProtocol;

public class DemoDbGetter {
	private static final String HTTP_PROTOCOL = "http://";
	private static final String HTTPS_PROTOCOL = "https://";
	private static final String PASSWORD_KEY = "password";
	private static final String USERNAME_KEY = "userName";
	private static final String DATABASENAME_KEY = "databaseName";
	private static final String PORT_KEY = "port";
	private static final String HOST_KEY = "host";
	private static final String PROTOCOL_KEY = "protocol";
	private static final String DEMODB_CONNECTION_DATA_PROPERTIES_FILENAME = "demodb.properties";

	private static final class DemoDbInfo {
		public final RPCProtocol protocol;
		public final String host;
		public final Integer port;
		public final String db;
		public final String username;
		public final String password;

		public DemoDbInfo(final RPCProtocol protocol, final String host, final Integer port, final String db,
				final String username, final String password) {
			this.protocol = protocol;
			this.host = host;
			this.port = port;
			this.db = db;
			this.username = username;
			this.password = password;
		}
	}

	public static interface DemoDbInfoRequester {
		void setProtocol(RPCProtocol protocol);

		void setHost(String host);

		void setPort(Integer port);

		void setDatabaseName(String databaseName);

		void setUserName(String userName);

		void setPassword(String password);
	}

	/**
	 * Provides a valid demo DB connection data to the passed requester, setting
	 * the protocol, host, port, database name, user name and password.
	 * 
	 * Validity is checked by connecting to said database and asking for server
	 * version.
	 * 
	 * connection data is either restored from previous request or requested
	 * from https://demo.odoo.com/start if no previous request could be found or
	 * the restored data is invalid (demo db has been destroyed by
	 * https://demo.odoo.com/start, etc)
	 * 
	 * @param requester
	 * @throws XmlRpcException
	 */
	public static void getDemoDb(DemoDbInfoRequester requester) throws XmlRpcException {
		// First try to read previous Demo DB infos to avoid requesting a
		// new one uselessly
		DemoDbInfo infos = readDemoDbConnectionData();

		// Test validity of restored data
		boolean connectionSucceeded = false;
		if (infos != null) {
			System.out.print("Connection data restored from previous, testing validity : ");
			connectionSucceeded = testConnection(infos);
			if (connectionSucceeded) {
				System.out.println("[VALID]");
			} else {
				System.out.println("[INVALID]");
			}
		}
		
		if (!connectionSucceeded) {
			// If previous infos were not found or DB is not available anymore,
			// request a new one and save its infos
			// No try-catch on call below because if that fails, there is no way
			// to play the test cases of this class...
			infos = DemoDbGetter.getNewDemoDb();
			saveDemoDbConnectionData(infos);
		}
		
		System.out.println("Connection used for the following tests:\n  url=" + (infos.protocol == RPCProtocol.RPC_HTTPS ? "https://" : "http://") + infos.host
				+ ":" + infos.port + ",\n  databaseName=" + infos.db + ",\n  userName=" + infos.username
				+ ",\n  password=" + infos.password);

		transmitDataToRequester(requester, infos);
	}

	private static void transmitDataToRequester(DemoDbInfoRequester requester, DemoDbInfo infos) {
		requester.setProtocol(infos.protocol);
		requester.setHost(infos.host);
		requester.setPort(infos.port);
		requester.setDatabaseName(infos.db);
		requester.setUserName(infos.username);
		requester.setPassword(infos.password);
	}

	private static boolean testConnection(DemoDbInfo infos) {
		// Try to connect to that DB to check it is still available
		final XmlRpcClient client = new XmlRpcClient();
		final XmlRpcClientConfigImpl common_config = new XmlRpcClientConfigImpl();
		try {
			String protocolAsString = infos.protocol == RPCProtocol.RPC_HTTP ? "http://" : "https://"; 
			common_config.setServerURL(
					new URL(String.format("%s%s:%s/xmlrpc/2/common", protocolAsString, infos.host, infos.port)));

			int uid = (int) client.execute(common_config, "authenticate",
					new Object[] { infos.db, infos.username, infos.password, new Object[] {} });
			// Informations are valid if user could log in.
			return uid != 0;

		} catch (MalformedURLException e1) {
			// Previously saved data is causing this...
			// We will have to request a new demo db
			return false;
		} catch (XmlRpcException e) {
			// Connection to previous demo db failed somehow, we will have
			// to request a new one...
			return false;
		}
	}

	private static void saveDemoDbConnectionData(DemoDbInfo infos) {
		Properties connectionData = new Properties();
		connectionData.put(PROTOCOL_KEY, infos.protocol.toString());
		connectionData.put(HOST_KEY, infos.host);
		connectionData.put(PORT_KEY, infos.port.toString());
		connectionData.put(DATABASENAME_KEY, infos.db);
		connectionData.put(USERNAME_KEY, infos.username);
		connectionData.put(PASSWORD_KEY, infos.password);
		
		try (PrintWriter out = new PrintWriter(
				new BufferedWriter(new FileWriter(DEMODB_CONNECTION_DATA_PROPERTIES_FILENAME)))) {
			connectionData.store(out, "demo database connection data used for unit tests");
		} catch (IOException e) {
			// Connection data will not be saved and thus won't be restored, but
			// that doesn't mean we can't use it...
			System.err.println("Could not save connection data");
			e.printStackTrace();
		}
	}

	private static DemoDbInfo readDemoDbConnectionData() {
		// If file does not exist, no point in trying to read it...
		if (!Files.exists(Paths.get(DEMODB_CONNECTION_DATA_PROPERTIES_FILENAME))) {
			return null;
		}

		RPCProtocol protocol = null;
		String host = null;
		Integer port = null;
		String databaseName = null;
		String userName = null;
		String password = null;

		Properties connectionData = new Properties();
		try (FileReader reader = new FileReader(DEMODB_CONNECTION_DATA_PROPERTIES_FILENAME)) {
			connectionData.load(reader);
			protocol = RPCProtocol.valueOf(connectionData.getProperty(PROTOCOL_KEY));
			host = connectionData.getProperty(HOST_KEY);
			port = Integer.parseInt(connectionData.getProperty(PORT_KEY));
			databaseName = connectionData.getProperty(DATABASENAME_KEY);
			userName = connectionData.getProperty(USERNAME_KEY);
			password = connectionData.getProperty(PASSWORD_KEY);
			return new DemoDbInfo(protocol, host, port, databaseName, userName, password);
		} catch (IOException e) {
			// Should not happen, since we tested for existence earlier...
			// File does not exist, read failed
			return null;
		} catch (NumberFormatException e) {
			// Most likely: failure to convert port number string back to
			// integer Read failed
			return null;
		} catch (IllegalArgumentException e) {
			// Most likely: failure while trying to convert protocol string back
			// to RPCProtocol enum
			// Read failed
			return null;
		}
	}

	private static DemoDbInfo getNewDemoDb() throws XmlRpcException {
		System.out.println("Requesting new demo db from https://demo.odoo.com/start");
		final XmlRpcClient client = new XmlRpcClient();

		final XmlRpcClientConfigImpl start_config = new XmlRpcClientConfigImpl();
		try {
			start_config.setServerURL(new URL("https://demo.odoo.com/start"));
			String methodName = "start";
			Object[] params = new Object[] {};
			@SuppressWarnings("unchecked")
			final Map<String, String> info = (Map<String, String>) client.execute(start_config, methodName, params);

			String url = info.get("host");
			final String db = info.get("database");
			final String username = info.get("user");
			final String password = info.get("password");

			RPCProtocol protocol;
			Integer port;

			if (url.startsWith(HTTPS_PROTOCOL)) {
				protocol = RPCProtocol.RPC_HTTPS;
				url = url.substring(HTTPS_PROTOCOL.length());
				port = 443;
			} else {
				protocol = RPCProtocol.RPC_HTTP;
				url = url.substring(HTTP_PROTOCOL.length());
				port = 80;
			}

			int lastIndexOfColon = url.lastIndexOf(':');
			if (lastIndexOfColon != -1) {
				// Port specified in the url
				String portString = url.substring(lastIndexOfColon + 1);
				if (portString != null && !portString.isEmpty()) {
					port = Integer.parseInt(portString);
				}
				url = url.substring(0, lastIndexOfColon - 1);
			}

			return new DemoDbInfo(protocol, url, port, db, username, password);
		} catch (MalformedURLException e) {
			// Won't happen, as "https://demo.odoo.com/start" is a valid URL...
			return null;
		}
	}
}
