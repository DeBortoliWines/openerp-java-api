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
