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

package com.odoojava.api.helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.stream.Collectors;

import org.apache.xmlrpc.XmlRpcException;

import com.odoojava.api.Field;
import com.odoojava.api.FieldCollection;
import com.odoojava.api.Row;

/**
 * Helper class to take a row object and flatten it out.
 * one2many and many2many fields become comma separated lists
 * many2one gets broken up into two fields, and ID field and a Name field
 *  
 * Typical usage could be something like this:
 * 
 *    FlatViewFieldCollection flatViewFields = FlatViewHelper.getFields(modelName, adapter.getFields());
 *    RowCollection rows = adapter.searchAndReadObject(someFilters, new String[]{},-1,queryLimit,null);
 *
 *    for (int i = 0; i < rows.size(); i++){
 *		  Row row = rows.get(i);
 *		  
 *        for (FlatViewField fld : flatViewFields.size()){
 *			  Object rowValue = FlatViewHelper.getRowValue(row, fld);
 *			  
 *		  }
 *    }
 *    	  
 * @author Pieter van der Merwe
 *
 */
public class FlatViewHelper {
	
	/**
	 * Takes a standard field collection and flattens it out to a 
	 * @param objectName Object that the fields are for, for eg res.partner
	 * @param fields Fields that was collected by an adapter
	 * @return A new field collection that is flattened out.  It can be used by this class to extract row values
	 * @throws XmlRpcException
	 */
	public static FlatViewFieldCollection getFields(String objectName, FieldCollection fields) throws XmlRpcException {
		
		FlatViewFieldCollection flatViewFields = new FlatViewFieldCollection();
		flatViewFields.add(new FlatViewField("id", null, -1, objectName, "id", "Database ID", Integer.class));
		
		for (Field fld : fields){
		
			Field.FieldType fieldType = fld.getType();
			switch (fieldType) {
			case BINARY:
				flatViewFields.add(new FlatViewField(fld.getName(), fld, -1, objectName, fld.getName(), fld.getDescription(), Byte[].class));
				break;
			case BOOLEAN:
				flatViewFields.add(new FlatViewField(fld.getName(), fld, -1, objectName, fld.getName(), fld.getDescription(), Boolean.class));
				break;
			case INTEGER:
				flatViewFields.add(new FlatViewField(fld.getName(), fld, -1, objectName, fld.getName(), fld.getDescription(), Integer.class));
				break;
			case FLOAT:
				flatViewFields.add(new FlatViewField(fld.getName(), fld, -1, objectName, fld.getName(), fld.getDescription(), Float.class));
				break;
			case DATETIME:
			case DATE:
				flatViewFields.add(new FlatViewField(fld.getName(), fld, -1, objectName, fld.getName(), fld.getDescription(), Date.class));
				break;
			case MANY2ONE:
				flatViewFields.add(new FlatViewField(fld.getName() + "##id", fld, 0, fld.getRelation(), fld.getName() + "_id", fld.getDescription() + "/Id", Integer.class));
				flatViewFields.add(new FlatViewField(fld.getName() + "##name", fld, 1, fld.getRelation(), fld.getName() + "_name", fld.getDescription() + "/Id", String.class));
				break;
			case ONE2MANY:
			case MANY2MANY:
			case CHAR:
			case TEXT:
			default:
				flatViewFields.add(new FlatViewField(fld.getName(), fld, -1, objectName, fld.getName(), fld.getDescription(), String.class));
				break;
			}
		}
		
		return flatViewFields;
	}
	
	/**
	 * Helper function to return flat view fields from a standard field collection
	 * @param objectName Object that the fields are for, for eg res.partner
	 * @param fields Fields that was collected by an adapter
	 * @return Flattened out field names.
	 * @throws XmlRpcException
	 */
	public static String[] getFieldNames(String objectName, FieldCollection fields) throws XmlRpcException {
		
		return getFields(objectName, fields).stream()
				.map(f -> f.getName())
				.toArray(String[]::new);
	}
	
	/**
	 * Returns the original field names from a flattened out view collection 
	 * @param fields Flattened out field collection 
	 * @return Original list of fields that the flattened out collection was built on
	 */
	public static String[] getOriginalFieldNames(FlatViewFieldCollection fields){
		
		return fields.stream()
				.collect(Collectors.toMap(
						f -> f.getSourceField().getName(), 
						f -> f.getName(), 
						(f1, f2) -> f1))
				.values().toArray(new String[0]);
	}
	
	/**
	 * Gets the row value for a flattened field definition.
	 * @param row Standard row from an object adapter
	 * @param fld Flattened field to retrieve info for
	 * @return A single value for a flattened field
	 */
	public static Object getRowValue(Row row, FlatViewField fld){
		Object value;
		
		if (fld.getName().equals("id"))
			value = row.get("id");
		else value = row.get(fld.getSourceField());
		
		// handle many2one fields
		if (fld.getSourceFieldIndex() >= 0 && value != null && value instanceof Object [])
			value = ((Object[]) value)[fld.getSourceFieldIndex()];
		
		// Handle many2many and one2many fields
		if (value instanceof Object []){
			String stringValue = "";
			for(Object singleValue : (Object []) value){
				stringValue += "," + singleValue.toString();
			}
			
			value = stringValue.substring(1); 
		}
		
		return value;
	}
	
	/**
	 * Class to hold FlatViewFields
	 * @author Pieter van der Merwe
	 *
	 */
	public static class FlatViewFieldCollection extends ArrayList<FlatViewField> {

		private static final long serialVersionUID = -5920069477471978489L;

		public void SortByFieldName(final boolean idFirst){
			Collections.sort(this, new Comparator<FlatViewField>() {
				@Override
				public int compare(FlatViewField arg0, FlatViewField arg1) {
					if (idFirst && arg0.getName().equals("id"))
						return -1;
					else return arg0.getName().compareTo(arg1.getName());
				}});
		}
	}
	
	/**
	 * Holder class to store flatview field information to be used by the helper class to extract data
	 * @author Pieter van der Merwe
	 *
	 */
	public static class FlatViewField {
		private final String uniqueID;
		private final Field sourceField;
		private final int sourceFieldIndex;
		private final String relatedModel;
		private final String name;
		private final String label;
		
		@SuppressWarnings("rawtypes")
		private final Class type;
		
		/**
		 * Default constructor
		 * @param uniqueID Unique id to identify this field
		 * @param sourceField Original source field that this field is based on
		 * @param sourceFieldIndex If the source field is a many2one field and an array, the array index this field is for.
		 * @param relatedModel If this field is an relational field, the model it relates to
		 * @param name Name of the new field
		 * @param label Label of the new field
		 * @param type Class type this field hold a value for.  eg.  String.class, Integer.class etc.
		 */
		protected FlatViewField(String uniqueID, Field sourceField, int sourceFieldIndex, String relatedModel, String name, String label, Class<?> type){
			this.uniqueID = uniqueID;
			this.sourceField = sourceField;
			this.sourceFieldIndex = sourceFieldIndex;
			this.relatedModel = relatedModel;
			this.name = name;
			this.label = label;
			this.type = type;
		}

		public String getUniqueID() {
			return uniqueID;
		}

		public Field getSourceField() {
			return sourceField;
		}

		public int getSourceFieldIndex() {
			return sourceFieldIndex;
		}

		public String getRelatedModel() {
			return relatedModel;
		}

		public String getName() {
			return name;
		}

		public String getLabel() {
			return label;
		}

		@SuppressWarnings("rawtypes")
		public Class getType() {
			return type;
		}
	}
	
}
