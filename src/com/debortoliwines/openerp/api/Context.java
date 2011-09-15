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

import java.util.HashMap;

/**
 * Provides the context object that is used in calls to the server.
 * @author Pieter van der Merwe
 *
 */
public class Context extends HashMap<String, Object>{

	private static final long serialVersionUID = 1L;
	final String ActiveTestTag = "active_test";
	
	/**
	 * Default constructor
	 */
	public Context(){
		super();
		
		// Default context values
		this.put(ActiveTestTag, true);
	}

	/**
	 * Retrieves the active_test context property.
	 * @return True if set.  
	 */
	public boolean getActiveTest(){
		return Boolean.getBoolean(this.get(ActiveTestTag).toString());
	}
	
	/**
	 * Sets the active_test context flag.  If true, only active items are returned by default when calling the ReadObject item.
	 * @param active_test
	 */
	public void setActiveTest(boolean active_test){
		this.remove(ActiveTestTag);
		this.put(ActiveTestTag, active_test);
	}
	
}
