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
public class OpenERPClient extends XmlRpcClient {
	
	/**
	 * Enum for the main RPC services that OpenERP expose 
	 * @author Pieter van der Merwe
	 */
	public enum RPCServices {
		RPC_COMMON,
		RPC_OBJECT,
		RPC_DATABASE
	}
	
	private final String RPC_COMMON_URL = "/xmlrpc/common";
	private final String RPC_OBJECT_URL = "/xmlrpc/object";
	private final String RPC_DATABASE_URL = "/xmlrpc/db";
	
	/**
	 * @param host Host name or IP address where the OpenERP server is hosted
	 * @param port XML-RPC port number to connect to.  Typically 8069.
	 * @param rpcObjectType 
	 */
	public OpenERPClient(String host, int port, RPCServices service) {
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
		
		XmlRpcClientConfigImpl xmlrpcConfigLogin = new XmlRpcClientConfigImpl();
		xmlrpcConfigLogin.setEnabledForExtensions(true);
		// The URL is hardcoded and can not be malformed
		try {
			xmlrpcConfigLogin.setServerURL(new URL("http", host, port,URL));
		} catch (MalformedURLException e) {
		}
	
		this.setConfig(xmlrpcConfigLogin);
	}
	
	/***
	 * Get a list of databases available on a specific host and port
	 * @param host Host name or IP address where the OpenERP server is hosted
	 * @param port XML-RPC port number to connect to
	 * @return A list of databases available for the OpenERP instance
	 * @throws XmlRpcException
	 */
	public static ArrayList<String> getDatabaseList (String host, int port) throws XmlRpcException
	{
		OpenERPClient client = new OpenERPClient(host, port, RPCServices.RPC_DATABASE);
		
		//Retrieve databases
		Object [] result = (Object []) client.execute("list", new Object[] {});

		ArrayList<String> finalResults = new ArrayList<String>();
		for (Object res : result)
			finalResults.add((String) res);

		return finalResults;
	}
}
