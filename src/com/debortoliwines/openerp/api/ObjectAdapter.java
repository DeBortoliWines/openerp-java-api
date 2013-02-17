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
import java.util.Date;
import java.util.HashMap;

import org.apache.xmlrpc.XmlRpcException;

import com.debortoliwines.openerp.api.Field.FieldType;
import com.debortoliwines.openerp.api.FilterCollection.FilterOperator;
import com.debortoliwines.openerp.api.helpers.FilterHelper;

/**
 * Main class for communicating with the server.  It provides extra validation for making calls to the OpenERP server.  It converts data types, validates model names, validates filters, checks for nulls etc.
 * @author Pieter van der Merwe
 *
 */
public class ObjectAdapter {

	private final String objectName;
	private final OpenERPCommand commands;
	private final FieldCollection allFields;
	
	// Object name cache so the adapter doesn't have to reread model names from the database for every new object.
	// Bulk loads/reads can become very slow if every adapter requires a call back to the server
	private static ArrayList<String> objectNameCache = new ArrayList<String>();
	
	// Cache used to store the name_get result of an model to cater for many2many relations in the import function
	// It is cleared every time the import function is called for a specific object
	private HashMap<String, HashMap<String, String>> modelNameCache = new HashMap<String, HashMap<String, String>>();

	/**
	 * Default constructor
	 * @param session Session object that will be used to make the calls
	 * @param objectName Object name that this adapter will work for.
	 * @throws XmlRpcException
	 * @throws OpeneERPApiException
	 */
	public ObjectAdapter(Session session, String objectName) throws XmlRpcException, OpeneERPApiException{
		this.commands = session.getOpenERPCommand();
		this.objectName = objectName;

		objectExists(this.commands, this.objectName);

		allFields = getFields();
	}
	
	/**
	 * Validates a model name against entries in ir.model
	 * The function is synchronized to make use of a global (static) list of objects to increase speed
	 * @param commands  Command object to use
	 * @param objectName Object name that needs to be validated
	 * @throws OpeneERPApiException If the model could not be validated
	 */
	@SuppressWarnings("unchecked")
	private synchronized static void objectExists(OpenERPCommand commands, String objectName) throws OpeneERPApiException{
		// If you can't find the object name, reload the cache.  Somebody may have added a new module after the cache was created
	  // Ticket #1 from sourceforge
	  if (objectNameCache.indexOf(objectName) < 0){
		  objectNameCache.clear();
			try{
				Object[] ids = commands.searchObject("ir.model", new Object[]{});
				Object [] result = commands.readObject("ir.model", ids, new String[]{"model"});
				for (Object row : result){
					objectNameCache.add(((HashMap<String, Object>) row).get("model").toString());
				}
			}
			catch (XmlRpcException e){
				throw new OpeneERPApiException("Could not validate model name: ",e);
			}
		}

		if (objectNameCache.indexOf(objectName) < 0)
			throw new OpeneERPApiException("Could not find model with name '" + objectName + "'");
	}
	
	/**
	 * Prepares a ROW object to be used for setting values in import/write methods
	 * @param fields Fields that should be included in the row definition
	 * @return An empty row with the specified fields
	 * @throws XmlRpcException
	 * @throws OpeneERPApiException
	 */
	public Row getNewRow(FieldCollection fields) throws XmlRpcException, OpeneERPApiException{
		Row row = new Row(new HashMap<String, Object>(), fields);
		return row;
	}
	
	/**
	 * Prepares a ROW object to be used for setting values in import/write methods.
	 * This method calls to the server to get the fieldCollection.  Use getNewRow(FieldCollection fields)
	 * if you can to reduce the number of calls to the server for a bulk load.
	 * @param fields Fields that should be included in the row definition
	 * @return An empty row with the specified fields
	 * @throws XmlRpcException
	 * @throws OpeneERPApiException
	 */
	public Row getNewRow(String [] fields) throws XmlRpcException, OpeneERPApiException{
		FieldCollection fieldCol = getFields(fields);
		Row row = new Row(new HashMap<String, Object>(), fieldCol);
		return row;
	}

	/**
	 * Reads objects from the OpenERP server if you already have the ID's.  If you don't, use searchAndRead with filters.
	 * @param ids List of ids to fetch objects for
	 * @param fields List of fields to fetch data for
	 * @return A collection of rows for an OpenERP object
	 * @throws XmlRpcException
	 * @throws OpeneERPApiException
	 */
	public RowCollection readObject(Object [] ids, String [] fields) throws XmlRpcException, OpeneERPApiException {

		// Faster to do read existing fields that to do a server call again
		FieldCollection fieldCol = new FieldCollection(); 
		for (String fieldName : fields){
			for (Field fld : allFields){
				if (fld.getName().equals(fieldName)){
					fieldCol.add(fld);
				}
			}
		}
		
		Object[] results = commands.readObject(objectName, ids, fields);
		
/****	18/04/2012 - PvdM Maybe reconsider this piece of code for later.  Does it matter if it isn't sorted by ID?

		// OpenERP doesn't use the sorting you pass (specified in the search function to get a sorted list of IDs).
		// When they fix it, remove this section of code
		ArrayList<Integer> idList = new ArrayList<Integer>();
		for (Object id : ids){
		  idList.add(Integer.parseInt(id.toString()));
		}
    Object[] sortedResults = new Object[ids.length];
    for (Object result : results){
      @SuppressWarnings("unchecked")
      int id = Integer.parseInt(((HashMap<String, Object>)result).get("id").toString());
      sortedResults[idList.indexOf(id)] = result;
    }
****/
		
		RowCollection rows = new RowCollection(results, fieldCol);

		return rows;
	}

	/***
	 * Fetches field information for the current OpenERP object this adapter is linked to 
	 * @return FieldCollecton data for all fields of the object
	 * @throws XmlRpcException
	 */
	public FieldCollection getFields() throws XmlRpcException {
		return getFields(new String[] {});
	}

	/***
	 * Fetches field names for the current OpenERP object this adapter is linked to
	 * @return Array of field names
	 * @throws XmlRpcException
	 */
	public String [] getFieldNames() throws XmlRpcException {
		FieldCollection fields = getFields(new String[] {});
		String [] fieldNames = new String[fields.size()];
		for (int i = 0; i < fields.size(); i++)
			fieldNames[i] = fields.get(i).getName();
		return fieldNames;
	}

	/***
	 * Fetches field information for the current OpenERP object this adapter is linked to
	 * @param filterFields Only return data for files in the filter list
	 * @return FieldCollecton data for selected fields of the object
	 * @throws XmlRpcException
	 */
	@SuppressWarnings("unchecked")
	public FieldCollection getFields(String[] filterFields) throws XmlRpcException {
		FieldCollection collection = new FieldCollection();

		HashMap<String, Object> fields = commands.getFields(objectName,filterFields);

		for (String fieldName: fields.keySet()){
			HashMap<String, Object> fieldDetails = (HashMap<String, Object>) fields.get(fieldName);
			collection.add(new Field(fieldName, fieldDetails));
		}

		return collection;
	}

	/**
	 * Helper function to validate filter parameters and returns a filter object suitable
	 * for the OpenERP search function by fixing data types and converting values where appropriate.
	 * @param filters FilterCollection containing the specified filters
	 * @return A validated filter Object[] correctly formatted for use by the OpenERP search function 
	 * @throws OpeneERPApiException
	 */
	public Object[] validateFilters(final FilterCollection filters) throws OpeneERPApiException{

		if (filters == null)
			return new Object[0];
		
		ArrayList<Object> processedFilters = new ArrayList<Object>();

		for (int i = 0; i < filters.getFilters().length; i++){
			Object filter = filters.getFilters()[i];

			if (filter == null)
				throw new OpeneERPApiException("The first filter parameter is mandatory");

			// Is a logical operator
			if (filter instanceof String){
				String operator = filter.toString();

				if (operator.equals(FilterOperator.AND))
					continue;

				// OR must have two parameters following
				if (operator.equals(FilterOperator.OR))
					if (filters.getFilters().length <= i + 2)
						throw new OpeneERPApiException("Logical operator OR needs two parameters.  Please read the OpenERP help.");

				// NOT must have one parameter following
				if (operator.equals(FilterOperator.NOT))
					if (filters.getFilters().length <= i + 1)
						throw new OpeneERPApiException("Logical operator NOT needs one parameter.  Please read the OpenERP help.");

				processedFilters.add(operator);
				continue;
			}

			if (!(filter instanceof Object[]) && ((Object[])filter).length != 3)
				throw new OpeneERPApiException("Filters aren't in the correct format.  Please read the OpenERP help.");

			String fieldName = ((Object[])filter)[0].toString();
			String comparison = ((Object[])filter)[1].toString();
			Object value = ((Object[])filter)[2];

			Field fld = null;
			for (int j = 0; j < allFields.size(); j++){
				if (allFields.get(j).getName().equals(fieldName)){
					fld = allFields.get(j);
					break;
				}
			}
			
			// Can't search on calculated fields
			if (fld != null && fld.getFunc_method() == true)
				throw new OpeneERPApiException("Can not search on function field " + fieldName);
			
			// Fix the value type if required for the OpenERP server
			if (!fieldName.equals("id") && fld == null)
				throw new OpeneERPApiException("Unknow filter field " + fieldName);
			else if (comparison.equals("is null")){
				comparison = "=";
				value = false;
			}
			else if (comparison.equals("is not null")){
				comparison = "!=";
				value = false;
			}
			else if (fld != null && fld.getType() == FieldType.BOOLEAN && !(value instanceof Boolean)){
				if (value instanceof String){
					char firstchar = value.toString().toLowerCase().charAt(0);
					if (firstchar == '1' || firstchar == 'y' || firstchar == 't')
						value = true;
					else if (firstchar == '0' || firstchar == 'n' || firstchar == 'f') 
						value = false;
					else throw new OpeneERPApiException ("Unknown boolean " + value.toString());
				}
			}
			else if (fld != null && fld.getType() == FieldType.FLOAT && !(value instanceof Double) )
				value = Double.parseDouble(value.toString());
			else if (comparison.equals("=")){

			  // If a integer field is not an integer in a '=' comparison, parse it as an int
			  if (!(value instanceof Integer)){
			      if (fieldName.equals("id") || 
			          (fld != null && fld.getType() == FieldType.INTEGER && !(value instanceof Integer)) ||
			          (fld != null && fld.getType() == FieldType.MANY2ONE && !(value instanceof Integer))){
			        value = Integer.parseInt(value.toString());
			      }
			  }
			}
			else if (comparison.equalsIgnoreCase("in")){
			  if (value instanceof String){
  			  // Split by , where the , isn't preceded by a \
  			  String [] entries = value.toString().split("(?<!\\\\),");
  			  Object [] valueArr = new Object[entries.length];
  			  for (int entrIdx = 0; entrIdx < entries.length; entrIdx++){
  			    String entry = FilterHelper.csvDecodeString(entries[entrIdx]);
  			    
  			    // For relation fields or integer fields we build an array of integers
  			    if (fld != null
  			        && (fld.getType() == FieldType.INTEGER
  			            || fld.getType() == FieldType.ONE2MANY
  			            || fld.getType() == FieldType.MANY2MANY
  			            || fld.getType() == FieldType.MANY2ONE)
  			        || fieldName.equals("id")){
  			      valueArr[entrIdx] = Integer.parseInt(entry);  
  			    }
  			    else valueArr[entrIdx] = entry;
  			  }
  			  value = valueArr;
			  }
			  // If it is a single value, just put it in an array
			  else if (!(value instanceof Object[])){
			    value = new Object[]{value};
			  }
			}
			    
			processedFilters.add(new Object[] {fieldName,comparison,value});
		}
		
		return processedFilters.toArray(new Object[processedFilters.size()]);
	}
	
	private Object[] fixImportData(Row inputRow) throws OpeneERPApiException, XmlRpcException {

		// +1 because we need to include the ID field
		Object[] outputRow = new Object[inputRow.getFields().size() + 1];

		// ID must be an integer
		outputRow[0] = inputRow.get("id");
		if (outputRow[0] == null)
			outputRow[0] = 0;
		else
			outputRow[0] = Integer.parseInt(inputRow.get("id").toString());
		
		for (int i = 0; i < inputRow.getFields().size(); i++){
			int columnIndex = i + 1;
			
			Field fld = inputRow.getFields().get(i);
			String fieldName = fld.getName();
			Object value = inputRow.get(fieldName);

			outputRow[columnIndex] = value;
			
			if (fld.getType() == FieldType.MANY2ONE){
				if (value == null)
					outputRow[columnIndex] = 0;
				else
					outputRow[columnIndex] = Integer.parseInt(value.toString());
				continue;
			}
			
			// Null values must be false
			if (value == null){
				outputRow[columnIndex] = false;
				continue;
			}
			
			value = formatValueForWrite(fld, value);
			
			// Check types
			switch (fld.getType()) {
			case SELECTION:
				boolean validValue = false;
				for (SelectionOption option : fld.getSelectionOptions()){
					// If the database code was specified, replace it with the value.
					// The import procedure uses the value and not the code
					if (option.code.equals(value.toString())){
						validValue = true;
						outputRow[columnIndex] = option.value;
						break;
					}
					else if (option.value.equals(value.toString())){
						outputRow[columnIndex] = value;
						validValue = true;
						break;
					}
				}
				if (!validValue)
					throw new OpeneERPApiException("Could not find a valid value for section field " + fieldName + " with value " + value);
				break;
			case MANY2MANY:
				/* The import function uses the Names of the objects for the import.  Replace the ID list passed
				 * in with a Name list for the import_data function that we are about to call
				 */
				HashMap<String, String> idToName = null;
				if (!modelNameCache.containsKey(fld.getRelation())){
					idToName = new HashMap<String, String>();
					Object [] ids = commands.searchObject(fld.getRelation(), new Object[]{});
					Object[] names = commands.nameGet(fld.getRelation(), ids);
					for (int j = 0; j < ids.length; j++){
						Object [] nameValue = (Object [])names[j]; 
						idToName.put(nameValue[0].toString(), nameValue[1].toString());
					}
					modelNameCache.put(fld.getRelation(), idToName);
				}
				else idToName = modelNameCache.get(fld.getRelation());
				
				String newValue = "";
				// Comma separated list of IDs
				if (value instanceof String){
					for (String singleID : value.toString().split(","))
						if (idToName.containsKey(singleID))
							newValue = newValue + "," + idToName.get(singleID);
						else throw new OpeneERPApiException("Could not find " + fld.getRelation() + " with ID " + singleID);
				}
				else {
					// Object[] of values -- default
					for (Object singleID : (Object[]) value)
						if (idToName.containsKey(singleID.toString()))
							newValue = newValue + "," + idToName.get(singleID.toString());
						else throw new OpeneERPApiException("Could not find " + fld.getRelation() + " with ID " + singleID.toString());
				}
				outputRow[columnIndex] = newValue.substring(1);
				
				break;
			
			// The import procedure expects most types to be strings
			default:
				outputRow[columnIndex] = value.toString();
				break;
			}
		}
		
		return outputRow;
	}
	
	private String[] getFieldListForImport(FieldCollection currentFields) {

		ArrayList<String> fieldList = new ArrayList<String>();
		fieldList.add(".id");

		for (Field field : currentFields){
			if (field.getType() == FieldType.MANY2ONE)
				fieldList.add(field.getName() + ".id");
			else
				fieldList.add(field.getName());
		}

		return fieldList.toArray(new String[fieldList.size()]);

	}
	
	/**
	 * Calls the import function on the server to bulk create/update records
	 * @param rows Rows to import.
	 * @return If the import was successful
	 * @throws XmlRpcException 
	 * @throws OpeneERPApiException 
	 */
	public boolean importData(RowCollection rows) throws OpeneERPApiException, XmlRpcException {

		modelNameCache.clear();
		String[] targetFieldList = getFieldListForImport(rows.get(0).getFields());
		
		Object[][] importRows = new Object[rows.size()][];
		
		for (int i = 0; i < rows.size(); i++){
			Row row = rows.get(i);
			importRows[i] = fixImportData(row);
		}
		
		Object [] result = commands.importData(objectName, targetFieldList, importRows);

		// Should return the number of rows committed.  If there was an error, it returns -1
		if ((Integer) result[0] != importRows.length)
			throw new OpeneERPApiException(result[2].toString() + "\nRow :" + result[1].toString() + "");

		return true;
	}
	
	/**
	 * Gets the number of records that satisfies the filter
	 * @param filter A filter collection that contains a list of filters to be applied
	 * @return The number of record count.
	 * @throws XmlRpcException
	 * @throws OpeneERPApiException
	 */
	public int getObjectCount(FilterCollection filter) throws XmlRpcException, OpeneERPApiException {
		Object[] preparedFilters = validateFilters(filter);
		return Integer.parseInt(commands.searchObject(objectName,preparedFilters, -1, -1, null, true).toString());
	}

	/***
	 * Combines the searchObject and readObject calls.  Allows for easy read of all data
	 * @param filter A filter collection that contains a list of filters to be applied
	 * @param fields List of fields to return data for
	 * @return A collection of rows for an OpenERP object
	 * @throws XmlRpcException
	 * @throws OpeneERPApiException 
	 */
	public RowCollection searchAndReadObject(FilterCollection filter, String [] fields) throws XmlRpcException, OpeneERPApiException {
		return searchAndReadObject(filter,fields,-1,-1,"");
	}
	
	/**
	 * Combines the searchObject and readObject calls and returns rows in batches.  Useful for multi-threaded ETL applications. 
	 * @param filter A filter collection that contains a list of filters to be applied 
	 * @param fields List of fields to return data for
	 * @param offset Number of records to skip.  -1 for no offset.
	 * @param limit Maximum number of rows to return.  -1 for no limit.
	 * @param order Field name to order on
	 * @return A collection of rows for an OpenERP object
	 * @throws XmlRpcException
	 * @throws OpeneERPApiException
	 */
	public RowCollection searchAndReadObject(final FilterCollection filter, final String[] fields, int offset, int limit, String order) throws XmlRpcException, OpeneERPApiException {

		String[] fieldArray = (fields == null ? new String[]{} : fields);
		Object[] preparedFilters = validateFilters(filter);
		Object[] idList = (Object[]) commands.searchObject(objectName,preparedFilters, offset, limit, order, false);	

		return readObject(idList,fieldArray);
		
	}
	
	private Object formatValueForWrite(Field fld, Object value){
		if (value == null)
			return false;
		
		switch (fld.getType()) {
		case BOOLEAN:
			value = (Boolean) value;
			break;
		case FLOAT:
			value = Double.parseDouble(value.toString());
			break;
		case MANY2ONE:
			value = Double.valueOf(value.toString()).intValue();
			break;
		case MANY2MANY:
			// For write, otherwise it is a comma separated list of strings used by import
			if (value instanceof Object[])
				value = new Object [][]{new Object[] {6,0, (Object[]) value}};
			break;
		case ONE2MANY:
		case INTEGER:
			// To make sure 1.0 is converted to 1
			value = Double.valueOf(value.toString()).intValue();
			break;
		default:
			value = value.toString();
			break;
		}
		
		return value; 
	}
	
	/**
	 * Writes a collection of rows to the database by calling the write function on the object the Row is holding data for
	 * @param rows Row collection to submit
	 * @param changesOnly Only changed values will be submitted to the database.
	 * @return An array of logicals.  One for each row to indicate if the update was successful
	 * @throws OpeneERPApiException
	 * @throws XmlRpcException
	 */
	public Boolean[] writeObject(final RowCollection rows, final boolean changesOnly) throws OpeneERPApiException, XmlRpcException{
		Boolean [] returnValues = new Boolean[rows.size()];
		
		for (int i = 0; i < rows.size(); i++)
			returnValues[i] = writeObject(rows.get(i), changesOnly);
		
		return returnValues;
	}
	
	/**
	 * Writes a Row to the database by calling the write function on the object the Row is holding data for
	 * @param row Row to be committed
	 * @param changesOnly Only changed values will be submitted to the database.
	 * @return If the update was successful
	 * @throws OpeneERPApiException
	 * @throws XmlRpcException
	 */
	public boolean writeObject(final Row row, boolean changesOnly) throws OpeneERPApiException, XmlRpcException{
		HashMap<String, Object> valueList = new HashMap<String, Object>();
		
		Object idObj = row.get("id");
		
		if (idObj == null || Integer.parseInt(idObj.toString()) <= 0)
			throw new OpeneERPApiException("Please set the id field with the database ID of the object");
		
		int id = Integer.parseInt(idObj.toString());
		
		if (changesOnly){
			for (Field fld : row.getChangedFields())
				valueList.put(fld.getName(), formatValueForWrite(fld,row.get(fld)));
		}
		else for (Field fld : row.getFields()){
			valueList.put(fld.getName(), formatValueForWrite(fld,row.get(fld)));
		}
		
		if (valueList.size() == 0)
			return false;
		
		boolean success = commands.writeObject(objectName, id, valueList);
		
		if (success)
			row.changesApplied();
		
		return success;
	}
	
	/**
	 * Creates an Object on the OpenERP server by calling the create function on the server.
	 * The id column is set on the row after the object was successfully created
	 * @param row Data row read data from to create the Object
	 * @throws OpeneERPApiException
	 * @throws XmlRpcException
	 */
	public void createObject(final Row row) throws OpeneERPApiException, XmlRpcException{

		HashMap<String, Object> valueList = new HashMap<String, Object>();
		for (Field fld : row.getFields())
			valueList.put(fld.getName(), formatValueForWrite(fld,row.get(fld)));
		
		if (valueList.size() == 0)
			throw new OpeneERPApiException("Row doesn't have any fields to update");
		
		Object id = commands.createObject(objectName, valueList);
		
		row.put("id", id);
		row.changesApplied();
		
	}
	
	/**
	 * Calls any function on an object that returns a field collection.
	 * ie. a row is retured as [{'name' : {'type' : 'char'}]  
	 * The OpenERP function must have the signature like (self, cr, uid, *param).
	 * @param functionName function to call
	 * @param parameters Additional parameters that will be passed to the object 
	 * @return A field collection
	 * @throws XmlRpcException
	 * @throws OpeneERPApiException
	 */
	@SuppressWarnings("unchecked")
	public FieldCollection callFieldsFunction(String functionName, Object[] parameters) throws XmlRpcException, OpeneERPApiException{
		Object[] results = commands.callObjectFunction(objectName, functionName, parameters);
		
		// Go through the first row and fetch the fields
		FieldCollection fieldCol = new FieldCollection();
		if (results.length > 0){
			HashMap<String, Object> rowMap = (HashMap<String, Object>) results[0];
			for (String field : rowMap.keySet()){
				HashMap<String, Object> fldDetails = null;
				
				if (rowMap.get(field) instanceof HashMap<?, ?>){
					try{
						fldDetails = (HashMap<String, Object>) rowMap.get(field);
					}
					catch (Exception e){
						fldDetails = null;
					}
				}
				
				if (fldDetails == null)
				  fldDetails = new HashMap<String, Object>();
				
				if (!fldDetails.containsKey("name"))
					fldDetails.put("name", field);
				if (!fldDetails.containsKey("description"))
					fldDetails.put("description", field);
				
				if (!fldDetails.containsKey("type")){
					@SuppressWarnings({ "rawtypes"})
					Class type = rowMap.get(field).getClass();
					if (type == String.class){
						fldDetails.put("type", "char");
					}
					else if (type == Date.class){
						fldDetails.put("type", "date");
					}
					else if (type == Boolean.class){
						fldDetails.put("type", "boolean");
					}
					else if (type == Double.class){
						fldDetails.put("type", "float");
					}
					else if (type == Integer.class){
						fldDetails.put("type", "integer");
					}
					else fldDetails.put("type", "char");
				}
				fieldCol.add(new Field(field,fldDetails));
			}
		}
		
		return fieldCol;
	}
	
	/**
	 * Calls any function on an object.
	 * The first row is inspected to determine data fields and data types  
	 * The OpenERP function must have the signature like (self, cr, uid, *param) and return a dictionary or object.
	 * @param functionName function to call
	 * @param parameters Additional parameters that will be passed to the object 
	 * @param fieldCol An option field collection to use.  A new one will be built by inspecting the first row if it isn't specified (null).
	 * @return A row collection with the data
	 * @throws XmlRpcException
	 * @throws OpeneERPApiException
	 */
	public RowCollection callFunction(String functionName, Object[] parameters, FieldCollection fieldCol) throws XmlRpcException, OpeneERPApiException{
		Object[] results = commands.callObjectFunction(objectName, functionName, parameters);
		
		if (fieldCol == null)
			fieldCol = callFieldsFunction(functionName, parameters);
		
		RowCollection rows = new RowCollection(results, fieldCol);

		return rows;
	}
}
