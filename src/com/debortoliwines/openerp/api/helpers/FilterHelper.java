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

package com.debortoliwines.openerp.api.helpers;

/**
 * Filter helper to assist is common tasks and store some hardcoded values
 * @author Pieter van der Merwe
 *
 */
public class FilterHelper {
	/**
	 * Returns a list of supported operators supported by the API
	 * @return
	 */
	public static String [] getOperators(){
		return new String [] {"", "NOT", "OR"};
	}
	/**
	 * Returns a list of comparators supported by the API
	 * @return
	 */
	public static String [] getComparators(){
		return new String [] {"=", "!=", ">", ">=", "<", "<=", "like", "ilike", "is null", "is not null", "in", "not in", "child_of", "parent_left", "parent_right"};
	}
	
	/**
	 * This function is used when you build a comma separated list of filter values where you can't use an Object [].  Passing an Object []
	 * is the preferred way to pass values to filters if you are going to use a list.  But if you are forced, you can use a CSV format.  
	 * Use this function to ensure that commas in normal strings are escaped.
	 * for example:
	 * String list = FilterHelper.csvEncodeString(val1) + "," FilterHelper.csvEncodeString(val2)
	 * filters.add("name", "in", list);
	 * @param value String value to escape commas in
	 * @return
	 */
	public static String csvEncodeString(String value){
	  return value.replaceAll("(?!\\\\),", "\\\\,");
	}
	
	/**
	 * Used to decode a value that was encoded with the csvEncodeString function.
	 * @param value Value to be decoded.
	 * @return
	 */
	public static String csvDecodeString(String value){
    return value.replaceAll("\\\\(?=,)", "");
  }
}
