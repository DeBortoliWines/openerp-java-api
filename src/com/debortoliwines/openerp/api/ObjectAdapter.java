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

import com.debortoliwines.openerp.api.Field.FieldType;
import com.debortoliwines.openerp.api.FilterCollection.FilterOperator;

/**
 * Provides extra validation for making calls to the OpenERP server.  It converts data types, validates model names, validates filters, checks for nulls etc.
 * @author Pieter van der Merwe
 *
 */
public class ObjectAdapter {

	private final String objectName;
	private final OpenERPCommand commands;
	private final FieldCollection allFields;
	
	// Cache used to store the name_get result of an model to cater for many2many relations
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

		Object[] results = null;
		try{
			Object[] ids = commands.searchObject("ir.model", new Object[]{});
			results = commands.readObject("ir.model", ids, new String[]{"model"});
		}
		catch (XmlRpcException e){
			throw new OpeneERPApiException("Could not validate model name: ",e);
		}

		boolean found = false;
		for (Object row : results){
			@SuppressWarnings("unchecked")
			String modelName = ((HashMap<String, Object>) row).get("model").toString();
			if (modelName.equals(objectName))
				found = true;
		}

		if (found == false)
			throw new OpeneERPApiException("Could not find model with name '" + objectName + "'");

		allFields = getFields();
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

	// If you make this function public, you have to implement the search function too.
	// Prefer using the searchAndReadObject functions
	private RowCollection readObject(Object [] ids, String [] fields) throws XmlRpcException, OpeneERPApiException {

		FieldCollection fieldCol = getFields(fields);
		Object[] results = commands.readObject(objectName, ids, fields);

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
			else if (fieldName.equals("id") || (fld.getType() == FieldType.INTEGER && !(value instanceof Integer)))
				value = Integer.parseInt(value.toString());

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
			throw new OpeneERPApiException(result[2].toString());

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
		case MANY2MANY:
			value = new Object [][]{new Object[] {6,0, (Object[]) value}};
			break;
		case ONE2MANY:
		case INTEGER:
			value = (Integer) value;
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
}
