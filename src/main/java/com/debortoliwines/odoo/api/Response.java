package com.debortoliwines.odoo.api;

public class Response {

	private boolean isSuccessful;
	private Exception errorCause;
	private Object responseObject;

	public Response(Exception errorCause) {
		this.isSuccessful = false;
		this.errorCause = errorCause;
		this.responseObject = null;
	}

	public Response(Object responseObject) {
		this.isSuccessful = true;
		this.errorCause = null;
		this.responseObject = responseObject;
	}

	public boolean isSuccessful() {
		return isSuccessful;
	}

	public Throwable getErrorCause() {
		return errorCause;
	}

	public Object getResponseObject() {
		return responseObject;
	}

	public Object[] getResponseObjectAsArray() {
		if (responseObject instanceof Object[]) {
			return (Object[]) responseObject;
		} else {
			return new Object[] { responseObject };
		}
	}
}
