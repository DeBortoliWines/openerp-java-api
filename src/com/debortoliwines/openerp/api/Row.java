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

import com.debortoliwines.openerp.api.Field.FieldType;

/***
 * Encapsulates the HashMap row object that is returned by OpenERP
 * @author pvanderm
 *
 */
public class Row extends HashMap<String, Object> {

	private static final long serialVersionUID = -4426823347416670117L;
	private final HashMap<String, Object> openERPResult;
	private final FieldCollection fields;

	public Row (HashMap<String, Object> openERPResult, FieldCollection fields){
		this.openERPResult = openERPResult;
		this.fields = fields;
	}

	public FieldCollection getFields() {
		return fields;
	}

	@Override
	public Object get(Object key) {

		// ID is a special case.  It is always returned in a query
		if (key != null && key.toString().equals("id"))
			return openERPResult.get(key);
		
		Field fieldMeta = null;
		for (Field f : fields)
			if (f.getName().equals(key.toString())){
				fieldMeta = f;
				break;
			}
		

		if (fieldMeta == null)
			return null;

		Object value = openERPResult.get(key);
		Field.FieldType fieldType = fieldMeta.getType();

		if (fieldType != Field.FieldType.BOOLEAN && value instanceof Boolean)
			return null;

		if (value instanceof Object[] && ((Object []) value).length == 0)
			return null;

		if (fieldType == FieldType.ONE2MANY)
			return value; 

		return value;
	}
}
