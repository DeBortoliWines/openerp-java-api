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

	@Override
	public void add(int index, Row element) {
		super.add(index, element);
	}

	@Override
	public boolean add(Row e) {
		return super.add(e);
	}
}
