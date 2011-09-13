package com.debortoliwines.openerp.api;

public class ObjectAdapter {

	/***
	 * Event handler to return rows
	 * @author Pieter van der Merwe
	 *
	 */
	public static interface RowsReadListener {
		void rowsRead(final RowCollection rows);
	}
}
