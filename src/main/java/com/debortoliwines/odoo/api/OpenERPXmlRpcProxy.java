/*
 *   Copyright 2011-2012, 2014 De Bortoli Wines Pty Limited (Australia)
 *
 *   This file is part of OpenERPJavaAPI.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License. 
 *
 */

package com.debortoliwines.odoo.api;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcSun15HttpTransportFactory;
import org.apache.xmlrpc.client.XmlRpcTransportFactory;

/**
 * An XMLRRPC Client that connects to OpenERP 
 * @author Pieter van der Merwe
 */
public class OpenERPXmlRpcProxy extends XmlRpcClient {
	
	/**
	 * Enum for the main RPC services that OpenERP expose 
	 * @author Pieter van der Merwe
	 */
	public enum RPCServices {
		RPC_COMMON,
		RPC_OBJECT,
		RPC_DATABASE
	}
	
	/**
   * Enum for the RPC protocol used to connect to OpenERP 
   * @author Pieter van der Merwe
   */
  public enum RPCProtocol {
    RPC_HTTP,
    RPC_HTTPS
  }
	
	private final String RPC_COMMON_URL = "/xmlrpc/2/common";
	private final String RPC_OBJECT_URL = "/xmlrpc/2/object";
	private final String RPC_DATABASE_URL = "/xmlrpc/2/db";
	
	/**
	 * Proxy object to handle calls to and from the OpenERP server
	 * @param protocol Protocol to use when connecting to the RPC service ex. http/https
   * @param host Host name or IP address where the OpenERP server is hosted
	 * @param port XML-RPC port number to connect to.  Typically 8069.
	 * @param service OpenERP webservice to call (db/common etc)
	 */
	public OpenERPXmlRpcProxy(RPCProtocol protocol, String host, int port, RPCServices service) {
		super();
		
		String URL = "";
		
		switch (service) {
		case RPC_COMMON:
			URL = this.RPC_COMMON_URL;
			break;
		case RPC_OBJECT:
			URL = this.RPC_OBJECT_URL;
			break;
		case RPC_DATABASE:
			URL = this.RPC_DATABASE_URL;
			break;
		}
		
		String protocol_str = "";
		switch (protocol) {
		case RPC_HTTP:
			protocol_str = "http";
			break;

		default:
			protocol_str = "https";
			break;
		}

		// If a proxy is defined, use it:
		XmlRpcTransportFactory factory = this.getTransportFactory();
		if (factory != null && factory instanceof XmlRpcSun15HttpTransportFactory) {
			if (protocol == RPCProtocol.RPC_HTTP) {
				String proxyHost = System.getProperty("http.proxyHost");
				String proxyPortString = System.getProperty("http.proxyPort");
				if (proxyHost != null && !proxyHost.isEmpty()) {
					int proxyPort = 80;
					if (proxyPortString != null && !proxyPortString.isEmpty()) {
						try {
							proxyPort = Integer.parseInt(proxyPortString);
						} catch (NumberFormatException e) {
							// Port badly defined, keep the default
						}
					}
					Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
					((XmlRpcSun15HttpTransportFactory) factory).setProxy(proxy);
				}
			} else {
				String proxyHost = System.getProperty("https.proxyHost");
				if (proxyHost == null || proxyHost.isEmpty()) {
					proxyHost = System.getProperty("http.proxyHost");
				}
				String proxyPortString = System.getProperty("https.proxyPort");
				if (proxyPortString == null || proxyPortString.isEmpty()) {
					proxyPortString = System.getProperty("http.proxyPort");
				}

				if (proxyHost != null && !proxyHost.isEmpty()) {
					int proxyPort = 443;
					if (proxyPortString != null && !proxyPortString.isEmpty()) {
						try {
							proxyPort = Integer.parseInt(proxyPortString);
						} catch (NumberFormatException e) {
							// Port badly defined, keep the default
						}
					}
					Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
					((XmlRpcSun15HttpTransportFactory) factory).setProxy(proxy);
				}
			}
		} else {
			System.err.println("No transport factory or not compatible with Proxy support!");
		}

		XmlRpcClientConfigImpl xmlrpcConfigLogin = new XmlRpcClientConfigImpl();
		
		// OpenERP does not support extensions
		xmlrpcConfigLogin.setEnabledForExtensions(false);
		
		// The URL is hardcoded and can not be malformed
		try {
			xmlrpcConfigLogin.setServerURL(new URL(protocol_str, host, port, URL));
		} catch (MalformedURLException e) {
		}
	
		this.setConfig(xmlrpcConfigLogin);
	}
	
	/**
	 * Proxy object to handle calls to and from the OpenERP server.  Uses the http protocol to connect.
   * @param host Host name or IP address where the OpenERP server is hosted
   * @param port XML-RPC port number to connect to.  Typically 8069.
   * @param service OpenERP webservice to call (db/common etc)
   */
  public OpenERPXmlRpcProxy(String host, int port, RPCServices service) {
    this(RPCProtocol.RPC_HTTP, host, port, service);
  }
	
  /***
   * Get a list of databases available on a specific host and port
   * @param protocol Protocol to use when connecting to the RPC service ex. http/https
   * @param host Host name or IP address where the OpenERP server is hosted
   * @param port XML-RPC port number to connect to
   * @return A list of databases available for the OpenERP instance
   * @throws XmlRpcException
   */
  public static ArrayList<String> getDatabaseList (RPCProtocol protocol, String host, int port) throws XmlRpcException
  {
    OpenERPXmlRpcProxy client = new OpenERPXmlRpcProxy(protocol, host, port, RPCServices.RPC_DATABASE);
    
    //Retrieve databases
    Object [] result = (Object []) client.execute("list", new Object[] {});

    ArrayList<String> finalResults = new ArrayList<String>();
    for (Object res : result)
      finalResults.add((String) res);

    return finalResults;
  }
  
  /***
   * Get a list of databases available on a specific host and port with the http protocol.
   * @param host Host name or IP address where the OpenERP server is hosted
   * @param port XML-RPC port number to connect to
   * @return A list of databases available for the OpenERP instance
   * @throws XmlRpcException
   */
  public static ArrayList<String> getDatabaseList (String host, int port) throws XmlRpcException
  {
    return getDatabaseList(RPCProtocol.RPC_HTTP, host, port);
  }
  
  /***
   * Returns the OpenERP server version.  For example 7.0-20130216-002451 or 6.1-1
   * @param host Host name or IP address where the OpenERP server is hosted
   * @param port XML-RPC port number to connect to
   * @return The version number as a String
   * @throws XmlRpcException
   */
  public static Version getServerVersion (String host, int port) throws XmlRpcException
  {
    return getServerVersion(RPCProtocol.RPC_HTTP, host, port);
  }
  
  /***
   * Returns the OpenERP server version.  For example 7.0-20130216-002451 or 6.1-1
   * @param protocol Protocol to use when connecting to the RPC service ex. http/https
   * @param host Host name or IP address where the OpenERP server is hosted
   * @param port XML-RPC port number to connect to
   * @return The version number as a String
   * @throws XmlRpcException
   */
  public static Version getServerVersion (RPCProtocol protocol, String host, int port) throws XmlRpcException
  {
    OpenERPXmlRpcProxy client = new OpenERPXmlRpcProxy(protocol, host, port, RPCServices.RPC_DATABASE);
    
    return new Version(client.execute("server_version", new Object[] {}).toString());
  }
}
