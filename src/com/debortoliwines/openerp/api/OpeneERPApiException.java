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

/**
 * Exception class for OpenERP API errors
 * @author Pieter van der Merwe
 *
 */
public class OpeneERPApiException extends Exception {

	private static final long serialVersionUID = 3148147969903379455L;

	public OpeneERPApiException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public OpeneERPApiException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public OpeneERPApiException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

}
