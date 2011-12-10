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
}
