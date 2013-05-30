/*
 *   This file is part of OpenERPJavaAPI.
 *
 *   OpenERPJavaAPI is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   OpenERPJavaAPI is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with OpenERPJavaAPI.  If not, see <http://www.gnu.org/licenses/>.
 *
 *   Copyright 2011 De Bortoli Wines Pty Limited (Australia)
 *   Copyright 2012 De Bortoli Wines Pty Limited (Australia)
 */

package com.debortoliwines.openerp.api;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

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
	
	private final String RPC_COMMON_URL = "/xmlrpc/common";
	private final String RPC_OBJECT_URL = "/xmlrpc/object";
	private final String RPC_DATABASE_URL = "/xmlrpc/db";
	
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
  public static String getServerVersion (String host, int port) throws XmlRpcException
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
  public static String getServerVersion (RPCProtocol protocol, String host, int port) throws XmlRpcException
  {
    OpenERPXmlRpcProxy client = new OpenERPXmlRpcProxy(protocol, host, port, RPCServices.RPC_DATABASE);
    
    return client.execute("server_version", new Object[] {}).toString();
  }
}
