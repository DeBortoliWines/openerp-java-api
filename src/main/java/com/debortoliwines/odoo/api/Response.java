package com.debortoliwines.odoo.api;

public class Response {

	private final boolean isSuccessful;
	private final Exception errorCause;
	private final Object responseObject;
	private final Object[] responseObjectAsArray;

	public Response(final Exception errorCause) {
		this.isSuccessful = false;
		this.errorCause = errorCause;
		this.responseObject = null;
		this.responseObjectAsArray = new Object[0];
	}

	public Response(final Object responseObject) {
		this.isSuccessful = true;
		this.errorCause = null;
		this.responseObject = responseObject;
		if (responseObject instanceof Object[]) {
			this.responseObjectAsArray = (Object[]) responseObject;
		} else {
			this.responseObjectAsArray = new Object[] { responseObject };
		}
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
		return responseObjectAsArray;
	}
}
