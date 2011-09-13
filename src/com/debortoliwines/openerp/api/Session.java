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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.xmlrpc.XmlRpcException;

import com.debortoliwines.openerp.api.OpenERPClient.RPCServices;

/***
 * Manages an OpenERP session 
 * @author Pieter van der Merwe
 *
 */
public class Session {

	private String host;
	private int port;
	private String databaseName;
	private String userName;
	private String password;
	private int userID;
	private Context context = new Context();

	/***
	 * Session constructor
	 * @param host Host name or IP address where the OpenERP server is hosted
	 * @param port XML-RPC port number to connect to.  Typically 8069.
	 * @param databaseName Database name to connect to
	 * @param userName Username to log into the OpenERP server
	 * @param password Password to log into the OpenERP server
	 */
	public Session(String host, int port, String databaseName, String userName, String password){
		this.host = host;
		this.port = port;
		this.databaseName = databaseName;
		this.userName = userName;
		this.password = password;
	}
	
	public CommandWrapper getCommandWrapper(){
		return new CommandWrapper(this);
	}

	/***
	 * Starts a session on the OpenERP server and saves the UserID for use in later calls
	 * @throws Exception
	 */
	public void startSession() throws Exception {

		ArrayList<String> dbList = OpenERPClient.getDatabaseList(host,port);
		if (dbList.indexOf(databaseName) < 0){
			StringBuffer dbListBuff = new StringBuffer();
			for (String dbName : dbList)
				dbListBuff.append(dbName + System.getProperty("line.separator"));

			throw new Exception("Error while connecting to OpenERP.  Database [" + databaseName + "] "
					+ " was not found in the following list: " + System.getProperty("line.separator") 
					+ System.getProperty("line.separator") + dbListBuff.toString());
		}

		// Connect
		OpenERPClient commonClient = new OpenERPClient(host, port, RPCServices.RPC_COMMON);
		Object id = commonClient.execute("login", new Object[] { databaseName, userName, password });

		if (id instanceof Integer)
			userID = (Integer) id;
		else
			throw new Exception("Incorrect username and/or password.  Login Failed.");
	}
	
	/***
	 * Get a list of databases available on a specific host and port
	 * @param host Host name or IP address where the OpenERP server is hosted
	 * @param port XML-RPC port number to connect to. Typically 8069.
	 * @return A list of databases available for the OpenERP instance
	 * @throws XmlRpcException
	 */
	public ArrayList<String> getDatabaseList (String host, int port) throws XmlRpcException
	{
		return OpenERPClient.getDatabaseList(host, port);
	}
	
	public Object executeCommand(String objectName, String commandName, Object[] parameters) throws XmlRpcException {
		Object[] connectionParams = new Object[] {databaseName,userID,password,objectName,commandName};
		
		// Combine the connection parameters and command parameters
		Object[] params = new Object[connectionParams.length + parameters.length];
		System.arraycopy(connectionParams, 0, params, 0, connectionParams.length);
		System.arraycopy(parameters, 0, params, connectionParams.length, parameters.length);
		   
		OpenERPClient objectClient = new OpenERPClient(host, port, RPCServices.RPC_OBJECT);
		return objectClient.execute("execute", params);		
	}
	
	/**
	 * Retrieves the context object for the session to set properties on 
	 * @return
	 */
	public Context getContext(){
		return context;
	}
}
