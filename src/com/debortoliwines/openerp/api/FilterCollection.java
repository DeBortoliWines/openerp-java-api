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

/**
 * Helps to simplify specifying filters for search and read operations.
 * By default, the filters should be Object[][].
 * 
 * For example:
 * 		FilterCollection filters = new FilterCollection();
 *		filters.add("customer", "=", true);
 *		filters.add("active","=",true);
 *
 * @author Pieter van der Merwe
 *
 */
public class FilterCollection {

	private ArrayList<Object[]> filters = new ArrayList<Object[]>();
	
	/**
	 * Adds a filter specification to the existing list of filters
	 * @param fieldName Name of the model that should be filtered on
	 * @param comparison For example >,<,>=,<=, ilike or like
	 * @param value value that will be compared to 'fieldName' using the 'comparison'
	 */
	public void add(String fieldName, String comparison, Object value){
		Object [] filter = new Object[] {fieldName,comparison,value};
		filters.add(filter);
	}
	
	/**
	 * Clears the filter from previous filter values
	 */
	public void clear(){
		filters.clear();
	}
	
	/**
	 * Gets the filters in the Object[][] required by the XMLRPC calls to OpenERP
	 * @return
	 */
	public Object[][] getFilters(){
		return filters.toArray(new Object[filters.size()][3]);
	}
}
