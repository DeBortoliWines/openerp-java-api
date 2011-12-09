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

import org.apache.xmlrpc.XmlRpcException;

import com.debortoliwines.openerp.api.OpenERPXmlRpcProxy.RPCServices;

/***
 * Manages an OpenERP session by holding context and initiating all calls to the server.
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
	private static boolean connecting = false;

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
	
	/**
	 * Returns an initialized OpenERPCommand object for ease of reference.
	 * A OpenERPCommand provides basic calls to the server
	 * @return
	 */
	public OpenERPCommand getOpenERPCommand(){
		return new OpenERPCommand(this);
	}
	
	/**
	 * Returns an initialized ObjectAdapter object for ease of reference.
	 * A ObjectAdapter object does type conversions and error checking before making a call to the server
	 * @return
	 */
	public ObjectAdapter getObjectAdapter(String objectName) throws XmlRpcException, OpeneERPApiException{
		return new ObjectAdapter(this, objectName);
	}

	/***
	 * Starts a session on the OpenERP server and saves the UserID for use in later calls
	 * @throws Exception
	 */
	public void startSession() throws Exception {

		ArrayList<String> dbList = OpenERPXmlRpcProxy.getDatabaseList(host,port);
		if (dbList.indexOf(databaseName) < 0){
			StringBuffer dbListBuff = new StringBuffer();
			for (String dbName : dbList)
				dbListBuff.append(dbName + System.getProperty("line.separator"));

			throw new Exception("Error while connecting to OpenERP.  Database [" + databaseName + "] "
					+ " was not found in the following list: " + System.getProperty("line.separator") 
					+ System.getProperty("line.separator") + dbListBuff.toString());
		}

		// Connect
		OpenERPXmlRpcProxy commonClient = new OpenERPXmlRpcProxy(host, port, RPCServices.RPC_COMMON);
		
		// Synchronize all threads to login.  If you login with the same user at the same time you get concurrency
		// errors in the OpenERP server (for example by running a multi threaded ETL process like Kettle).
		Session.startConnecting();
		
		Object id = null;
		try{
			id = commonClient.execute("login", new Object[] { databaseName, userName, password });
		}
		finally{
			Session.connecting = false;
		}

		if (id instanceof Integer)
			userID = (Integer) id;
		else
			throw new Exception("Incorrect username and/or password.  Login Failed.");
	}
	
	private synchronized static void startConnecting(){
		while (Session.connecting){
			try {
				Thread.sleep(100);
			}
			catch (Exception e){}
		}
		Session.connecting = true;
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
		return OpenERPXmlRpcProxy.getDatabaseList(host, port);
	}
	
	/**
	 * Executes any command on the server linked to the /xmlrpc/object service.
	 * All parameters are prepended by: "databaseName,userID,password"
	 * @param objectName Object or model name to execute the command on
	 * @param commandName Command name to execute
	 * @param parameters List of parameters for the command.  For easy of use, consider the OpenERPCommand object or ObjectAdapter
	 * @return The result of the call
	 * @throws XmlRpcException
	 */
	public Object executeCommand(final String objectName, final String commandName, final Object[] parameters) throws XmlRpcException {
		Object[] connectionParams = new Object[] {databaseName,userID,password,objectName,commandName};
		
		// Combine the connection parameters and command parameters
		Object[] params = new Object[connectionParams.length + (parameters == null ? 0 : parameters.length)];
		System.arraycopy(connectionParams, 0, params, 0, connectionParams.length);
		
		if (parameters != null && parameters.length > 0)
			System.arraycopy(parameters, 0, params, connectionParams.length, parameters.length);
		   
		OpenERPXmlRpcProxy objectClient = new OpenERPXmlRpcProxy(host, port, RPCServices.RPC_OBJECT);
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
