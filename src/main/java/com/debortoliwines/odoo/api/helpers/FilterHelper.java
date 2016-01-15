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

package com.debortoliwines.odoo.api.helpers;

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
