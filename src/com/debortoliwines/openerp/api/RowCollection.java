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

/***
 * Row collection for OpenERP row data
 * @author Pieter van der Merwe
 *
 */
public class RowCollection extends ArrayList<Row> {

	private static final long serialVersionUID = -168965138153400087L;
	
	public RowCollection(){
	}
	
	@SuppressWarnings("unchecked")
	public RowCollection(Object [] openERPResultSet, FieldCollection fields) throws OpeneERPApiException{
		for (int i = 0; i < openERPResultSet.length; i++){
			Row row = new Row((HashMap<String, Object>) openERPResultSet[i], fields);
			this.add(row);
		}
	}
}
