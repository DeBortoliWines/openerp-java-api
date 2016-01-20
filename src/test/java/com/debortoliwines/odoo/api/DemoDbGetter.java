package com.debortoliwines.odoo.api;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

public class DemoDbGetter {
	public static final class DemoDbInfo {
		public final String url;
		public final String db;
		public final String username;
		public final String password;

		public DemoDbInfo(String url, String db, String username, String password) {
			this.url = url;
			this.db = db;
			this.username = username;
			this.password = password;
		}

		public static DemoDbInfo getNewDemoDb() {
			// TODO First start by looking for an existing demo db in properties file
			// previous demo db not found, request a new one.
			final XmlRpcClient client = new XmlRpcClient();

			final XmlRpcClientConfigImpl start_config = new XmlRpcClientConfigImpl();
			try {
				start_config.setServerURL(new URL("https://demo.odoo.com/start"));
				String methodName = "start";
				Object[] params = new Object[]{};
				@SuppressWarnings("unchecked")
				final Map<String, String> info = (Map<String, String>) client.execute(start_config, methodName, params);

				final String url = info.get("host"), db = info.get("database"), username = info.get("user"),
						password = info.get("password");

				// TODO Store demo db info into properties file for later reuse.
				return new DemoDbInfo(url, db, username, password);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (XmlRpcException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return null;
		}
	}
}
