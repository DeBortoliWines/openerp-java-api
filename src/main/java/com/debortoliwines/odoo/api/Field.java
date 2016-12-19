/*
 *   Copyright 2011, 2014 De Bortoli Wines Pty Limited (Australia)
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/***
 * Provides OpenERP field properties like data types, selection fields etc.
 * @author Pieter van der Merwe
 *
 */
public class Field {
	
	/**
	 * OpenERP field types.
	 * @author Pieter van der Merwe
	 *
	 */
	public enum FieldType {
		INTEGER, CHAR, TEXT, BINARY, BOOLEAN, FLOAT, DATETIME, DATE, MANY2ONE, ONE2MANY, MANY2MANY, SELECTION 
	}
	
	private final String name;
	private final Map<String, Object> openERPFieldData;
	
	public Field(String fieldName, Map<String, Object> openERPFieldData) {
		this.openERPFieldData = openERPFieldData;
		this.name = fieldName;
	}

	/***
	 * Any property not covered by a get function can be fetched using this function
	 * @param propertyName Name of property to fetch, for example 'name'.
	 * @return The value associated with the property if any.
	 */
	public Object getFieldProperty(String propertyName){
		Object value = null;
		
		if (openERPFieldData.containsKey(propertyName))
			value = openERPFieldData.get(propertyName);
		
		return value;
	}
	
	/**
	 * Gets field property values for every object state 
	 * @return An array of values for all states in the format [state, propvalue]
	 */
	public Object [][] getStateProperties(String propertyName){
		ArrayList<Object[]> stateValues = new ArrayList<Object[]>();
		
		@SuppressWarnings("unchecked")
		HashMap<String, Object> states = (HashMap<String, Object>) getFieldProperty("states");
		
		if (states != null){
			Object[] stateValue = new Object[2];
			for (Object stateKey : states.keySet()){
				stateValue[0] = stateKey.toString();
				
				for (Object stateProperty : (Object[]) states.get(stateKey)){
					Object[] statePropertyArr = (Object[]) stateProperty;
					if (statePropertyArr[0].toString().equals(propertyName)){
						stateValue[1] = statePropertyArr[1];
						stateValues.add(stateValue);
					}
				}
			}
		}
		
		return stateValues.toArray(new Object[0][]);
	}

	/**
	 * Get the field name 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/***
	 * Get the field description or label
	 * @return
	 */
	public String getDescription() {
		return (String) getFieldProperty("string");
	}

	/**
	 * Get the datatype of the field.  If you want the original OpenERP type, use getFieldProperty("type")
	 * @return
	 */
	public FieldType getType() {
		String fieldType = (String) getFieldProperty("type");
		
		if (fieldType.equalsIgnoreCase("char"))
			return FieldType.CHAR;
		else if (fieldType.equalsIgnoreCase("text"))
			return FieldType.TEXT;
		else if (fieldType.equalsIgnoreCase("integer"))
			return FieldType.INTEGER;
		else if (fieldType.equalsIgnoreCase("binary"))
			return FieldType.BINARY;
		else if (fieldType.equalsIgnoreCase("boolean"))
			return FieldType.BOOLEAN;
		else if (fieldType.equalsIgnoreCase("float"))
			return FieldType.FLOAT;
		else if (fieldType.equalsIgnoreCase("datetime"))
			return FieldType.DATETIME;
		else if (fieldType.equalsIgnoreCase("date"))
			return FieldType.DATE;
		else if (fieldType.equalsIgnoreCase("many2one"))
			return FieldType.MANY2ONE;
		else if (fieldType.equalsIgnoreCase("one2many"))
			return FieldType.ONE2MANY;
		else if (fieldType.equalsIgnoreCase("many2many"))
			return FieldType.MANY2MANY;
		else if (fieldType.equalsIgnoreCase("selection"))
			return FieldType.SELECTION;
		else return FieldType.CHAR;
	}

	/**
	 * Get the required property
	 * @return
	 */
	public boolean getRequired() {
		Object value = getFieldProperty("required");
		if (value == null)
			return false;
		return (Boolean) value;
	}

	/**
	 * Get the selectable property
	 * @return
	 */
	public boolean getSelectable() {
		Object value = getFieldProperty("selectable");
		if (value == null)
			return true;
		else return (Boolean) value;
	}

	/**
	 * If a field is a selection field, the list of selecton options are returned.
	 * @return
	 */
	public ArrayList<SelectionOption> getSelectionOptions(){
		if (this.getType() != FieldType.SELECTION)
			return null;

		ArrayList<SelectionOption> options = new ArrayList<SelectionOption>();
		Object values = getFieldProperty("selection");
		if (values instanceof Object[])
			for(Object val : (Object []) values){
				Object [] multiVal = (Object[]) val;
				options.add(new SelectionOption(multiVal[0].toString(), multiVal[1].toString()));
			}
		return options;
	}

	/**
	 * Get the size property
	 * @return
	 */
	public int getSize() {
		Object value = getFieldProperty("size");
		if (value == null)
			return 64;
		else return (Integer) value;
	}

	/**
	 * Get the help property
	 * @return
	 */
	public String getHelp() {
		return (String) getFieldProperty("help");
	}

	/**
	 * Get the store property
	 * @return
	 */
	public boolean getStore() {
		Object value = getFieldProperty("store");
		if (value == null)
			return true;
		return (Boolean) value;
	}

	/**
	 * Get the func_method property
	 * @return
	 */
	public boolean getFunc_method() {
		Object value = getFieldProperty("func_method");
		if (value == null)
			return false;
		return (Boolean) value;
	}

	/**
	 * Get the relation property
	 * @return
	 */
	public String getRelation() {
		Object value = getFieldProperty("relation");
		if (value == null)
			return "";
		return (String) value;
	}

	/**
	 * Get the readonly property
	 * @return
	 */
	public boolean getReadonly() {
		Object value = getFieldProperty("readonly");
		if (value == null)
			return false;
		else return (Boolean) (value instanceof Integer ? (Integer) value == 1: value);
	}
}
