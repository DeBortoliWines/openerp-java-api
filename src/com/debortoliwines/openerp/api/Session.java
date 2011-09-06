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

	/***
	 * Starts a session on the OpenERP server and saves the UserID for use in later calls
	 * @throws Exception
	 */
	public void startSession() throws Exception {

		ArrayList<String> dbList = getDatabaseList(host,port);
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
	 * Searches for objects that satisfies the filter
	 * @param objectName The object name to do a search for
	 * @param filter For example new Object[][] { new Object [] {"customer","=",true}}
	 * @return Array of ID's for the objects found
	 * @throws XmlRpcException
	 */
	public Object[] searchObject(String objectName, Object [][] filter) throws XmlRpcException {
		OpenERPClient objectClient = new OpenERPClient(host, port, RPCServices.RPC_OBJECT);
		Object[] params = new Object[] {databaseName,userID,password,objectName,"search", filter};
		Object[] ids = (Object[]) objectClient.execute("execute", params);
		return ids;
	}

	/***
	 * Fetches field information for an object n OpenERP
	 * @param objectName Object or model name to fetch field information for
	 * @return FieldCollecton data for all fields of the object
	 * @throws XmlRpcException
	 */
	public FieldCollection getFields(String objectName) throws XmlRpcException {
		return getFields(objectName,new String[] {});
	}

	/***
	 * Fetches field information for an object n OpenERP
	 * @param objectName Object or model name to fetch field information for
	 * @param filterFields Only return data for files in the filter list
	 * @return FieldCollecton data for selected fields of the object
	 * @throws XmlRpcException
	 */
	@SuppressWarnings("unchecked")
	public FieldCollection getFields(String objectName, String[] filterFields) throws XmlRpcException {
		FieldCollection collection = new FieldCollection();

		OpenERPClient objectClient = new OpenERPClient(host, port, RPCServices.RPC_OBJECT);
		Object[] params = new Object[] {databaseName,userID,password,objectName,"fields_get"};
		HashMap<String, Object> fields = (HashMap<String, Object>) objectClient.execute("execute", params);

		for (String fieldName: fields.keySet()){
			boolean include = true;

			if (filterFields.length > 0){
				include = false;
				for (String filterFieldName : filterFields)
					if (filterFieldName.equals(fieldName))
						include = true;
			}
			if (include == false)
				continue;


			HashMap<String, Object> fieldDetails = (HashMap<String, Object>) fields.get(fieldName);
			collection.add(new Field(fieldName, fieldDetails));
		}

		return collection;
	}

	/***
	 * Reads object data from the OpenERP server
	 * @param objectName Name of the object to return data for
	 * @param ids List of id to fetch data for.  Call searchObject to get a potential list
	 * @param fields List of fields to return data for
	 * @return A collection of rows for an OpenERP object
	 * @throws XmlRpcException
	 */
	public RowCollection readObject(String objectName, Object [] ids, String [] fields) throws XmlRpcException {
		FieldCollection fieldCol = getFields(objectName, fields);

		OpenERPClient objectClient = new OpenERPClient(host, port, RPCServices.RPC_OBJECT);
		Object[] params = new Object[] {databaseName, userID, password, objectName, "read", ids, fields};
		Object[] results = (Object[]) objectClient.execute("execute", params);

		RowCollection rows = new RowCollection(results, fieldCol);

		return rows;
	}
	
	/***
	 * Combines the searchObject and readObject calls.  Allows for easy read of all data
	 * @param objectName Name of the object to return data for
	 * @param filter For example new Object[][] { new Object [] {"customer","=",true}}
	 * @param fields List of fields to return data for
	 * @return A collection of rows for an OpenERP object
	 * @throws XmlRpcException
	 */
	public RowCollection searchAndReadObject(String objectName, Object [][] filter, String [] fields) throws XmlRpcException {
		
		return searchAndReadObject(objectName,filter,fields,0,new RowsReadListener() {
			@Override
			public void rowsRead(RowCollection rows) {
			}
		});
		
	}

	/***
	 * Combines the searchObject and readObject calls and returns rows in batches.  Useful for multi-threaded ETL applications.
	 * @param objectName Name of the object to return data for
	 * @param filter For example new Object[][] { new Object [] {"customer","=",true}}
	 * @param fields List of fields to return data for
	 * @param batchSize Number of rows to include in one batch.  A value of 0 or less creates one big batch.
	 * @param rowListener Row listener to call after a batch of rows were fetched from the OpenERP server
	 * @return The last batch that was collected.
	 * @throws XmlRpcException
	 */
	public RowCollection searchAndReadObject(String objectName, Object [][] filter, String [] fields, int batchSize, RowsReadListener rowListener) throws XmlRpcException {
		if (filter == null)
			filter = new Object [][]{};
		
		if (fields == null)
			fields = new String []{};
		
		Object[] idList = searchObject(objectName,filter);	

		RowCollection resultlist = null;

		if (batchSize <= 0){
			resultlist = readObject(objectName,idList,fields);
			rowListener.rowsRead(resultlist);
		}
		else{
			int numBatches = (idList.length / batchSize) + (idList.length % batchSize > 0 ? 1 : 0);
			for (int batchNum = 0; batchNum < numBatches; batchNum++){

				Object[] newIdList = new Object[batchSize];
				for(int i = 0; i < batchSize; i++){
					int index = batchNum * batchSize + i;

					/* Check that last batch doesn't stops at the last row */
					if (batchNum == numBatches -1 && index >= idList.length)
						continue;


					newIdList[i] = idList[batchNum * batchSize + i];
				}

				resultlist = readObject(objectName, newIdList, fields);
				rowListener.rowsRead(resultlist);
			}
		}
		
		
		return resultlist;
	}
	
	/**
	 * Updates object values
	 * @param objectName Name of the object to update
	 * @param id Database ID number of the object to update
	 * @param valueList Field/Value pairs to update on the object
	 * @return
	 * @throws XmlRpcException
	 */
	public boolean writeObject(String objectName, int id, HashMap<String, Object> valueList) throws XmlRpcException{
		boolean result = false;
		
		OpenERPClient objectClient = new OpenERPClient(host, port, RPCServices.RPC_OBJECT);
		Object[] params = new Object[] {databaseName, userID, password, objectName, "write", id, valueList};
		result = (Boolean) objectClient.execute("execute", params);
		
		return result;
	}
	
	/**
	 * Calls the import function on the server to bulk create/update records
	 * @param objectName Name of the object to update
	 * @param fieldList List of fields to update
	 * @param rows Rows to import.  Fields must be in the same order as the 'fieldList' parameter
	 * @return The result returned from the server
	 * @throws ImportException 
	 * @throws XmlRpcException 
	 */
	public Object [] importData(String objectName, String[] fieldList, ArrayList<Object[]> rows) throws XmlRpcException, ImportException{
		Object [][] convertedRows = rows.toArray(new Object[rows.size()][]);
		return importData(objectName, fieldList, convertedRows);
	}
	
	/**
	 * Calls the import function on the server to bulk create/update records
	 * @param objectName Name of the object to update
	 * @param fieldList List of fields to update
	 * @param rows Rows to import.  Fields must be in the same order as the 'fieldList' parameter
	 * @return The result returned from the server
	 * @throws XmlRpcException 
	 * @throws ImportException 
	 */
	public Object [] importData(String objectName, String[] fieldList, Object [][] rows) throws XmlRpcException, ImportException {
		Object [] result = null;
		
		OpenERPClient objectClient = new OpenERPClient(host, port, RPCServices.RPC_OBJECT);
		Object[] params = new Object[] {databaseName, userID, password, objectName, "import_data", fieldList, rows};
		result = (Object []) objectClient.execute("execute", params);
		
		// Should return the number of rows committed.  If there was an error, it returns -1
		if ((Integer) result[0] != rows.length)
			throw new ImportException(result[2].toString());
		
		return result;
	}
	
	/**
	 * Helper function to format many2many field values for update
	 * @param fieldValues List of ids for a many to many field, for example [1,2,3] for res.users.groups_id
	 * @return A formatted Object that can be put into a HashMap, ready for update
	 */
	public Object prepareMany2ManyValue(Object [] fieldValues){
		return new Object [][]{new Object[] {6,0, fieldValues}};
	}
	
	/***
	 * Miscellanous test functions
	 * To be finished later when I've got use for them
	public void nameGet() throws XmlRpcException{
		Object result = null;
		
		OpenERPClient objectClient = new OpenERPClient(host, port, RPCServices.RPC_OBJECT);
		HashMap<Object, String> values = new HashMap<Object, String> ();
		values.put("user_email", "test@gmail.com");
		
		HashMap<String, Object> rows = new HashMap<String, Object> ();
		rows.put("28", values);
		
		Object[] params = new Object[] {databaseName, userID, password, "res.users", "name_get", new Object[] {28}};
		result = objectClient.execute("execute", params);
		
		//return result;
	}
	
	public void nameSearch() throws XmlRpcException{
		Object result = null;
		
		OpenERPClient objectClient = new OpenERPClient(host, port, RPCServices.RPC_OBJECT);
		HashMap<Object, String> values = new HashMap<Object, String> ();
		values.put("user_email", "test@gmail.com");
		
		HashMap<String, Object> rows = new HashMap<String, Object> ();
		rows.put("28", values);
		
		Object[] params = new Object[] {databaseName, userID, password, "res.users", "name_search", "Pieter van der Merwe"};
		result = objectClient.execute("execute", params);
		
		//return result;
	}
	
	public Object getDefaults() throws XmlRpcException{
		Object result = null;
		OpenERPClient objectClient = new OpenERPClient(host, port, RPCServices.RPC_OBJECT);
		Object[] params = new Object[] {databaseName, userID, password, "res.users", "default_get", new Object[]{"active"}};
		result = objectClient.execute("execute", params);
		return result;	}
    ***/
	
	/***
	 * Event handler when rows 
	 * @author Pieter van der Merwe
	 *
	 */
	public static interface RowsReadListener {
		void rowsRead(final RowCollection rows);
	}
}
