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
 * By default, the filters should be Object[].
 * 
 * For example:
 * 		FilterCollection filters = new FilterCollection();
 *		filters.add("customer", "=", true);
 *		filters.add("active","=",true);
 *
 * To use a logical operator OR (type = many2one or type = many2many):
 * 		FilterCollection filters = new FilterCollection();
 *		filters.add(FilterOperator.OR);
 *		filters.add("type", "=", "many2one");
 *		filters.add("type", "=", "many2many");
 *
 * @author Pieter van der Merwe
 *
 */
public class FilterCollection {

	private ArrayList<Object> filters = new ArrayList<Object>();
	
	public enum FilterOperator{
		AND,
		OR,
		NOT
	}
	
	/**
	 * Adds a filter specification to the existing list of filters
	 * @param fieldName Name of the model that should be filtered on
	 * @param comparison For example =, !=, >, >=, <, <=, like, ilike, in, not in, child_of, parent_left, parent_right
	 * @param value value that will be compared to 'fieldName' using the 'comparison'
	 */
	public void add(String fieldName, String comparison, Object value){
		Object [] filter = new Object[] {fieldName,comparison,value};
		filters.add(filter);
	}
	
	/**
	 * Adds logical operators for filters
	 *
	 * From the OpenERP code:
	 *  Domain criteria can be combined using 3 logical operators than can be added between tuples:  '**&**' (logical AND, default), '**|**' (logical OR), '**!**' (logical NOT).
     *  These are **prefix** operators and the arity of the '**&**' and '**|**' operator is 2, while the arity of the '**!**' is just 1.
     *  Be very careful about this when you combine them the first time.
     *
     *  Here is an example of searching for Partners named *ABC* from Belgium and Germany whose language is not english ::
     *
     *      [('name','=','ABC'),'!',('language.code','=','en_US'),'|',('country_id.code','=','be'),('country_id.code','=','de'))
     *
     *  The '&' is omitted as it is the default, and of course we could have used '!=' for the language, but what this domain really represents is::
     *
     *      (name is 'ABC' AND (language is NOT english) AND (country is Belgium OR Germany))
	 * @param operator
	 */
	public void add(FilterOperator operator){
		switch (operator) {
		case OR:
			filters.add("|");
			break;
		case NOT:
			filters.add("!");
			break;
		default:
			break;
		}
	}
	
	/**
	 * Clears the filter from previous filter values
	 */
	public void clear(){
		filters.clear();
	}
	
	/**
	 * Gets the filters in a Object[] required by the XMLRPC calls to OpenERP
	 * @return
	 */
	public Object[] getFilters(){
		return filters.toArray(new Object[filters.size()]);
	}
}
