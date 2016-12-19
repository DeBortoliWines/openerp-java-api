package com.debortoliwines.odoo.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;

public class ResponseTest {
	private static final String EXCEPTION_MESSAGE = "Exception message";
	private Exception errorCause;
	private Response response;
	private Object responseObject;

	private void setupErrorCase() {
		errorCause = new Exception(EXCEPTION_MESSAGE);
		response = new Response(errorCause);
	}

	private void setupSingleSuccessCase() {
		responseObject = new Object();
		response = new Response(responseObject);
	}

	private void setupArraySuccessCase() {
		responseObject = new Object[] { "item1", "item2" };
		response = new Response(responseObject);
	}

	@Test
	public void should_be_marked_as_failed_for_exception() throws Exception {
		setupErrorCase();
		assertThat(response.isSuccessful()).as("Is sccuessful").isFalse();
	}

	@Test
	public void should_provide_cause_for_exception() throws Exception {
		setupErrorCase();
		assertThat(response.getErrorCause()).as("Error cause").isSameAs(errorCause);
	}

	@Test
	public void should_be_marked_as_successful_when_successful() throws Exception {
		setupSingleSuccessCase();
		assertThat(response.isSuccessful()).as("Is sccuessful").isTrue();
	}

	@Test
	public void should_provide_actual_response_object_when_successful() throws Exception {
		setupSingleSuccessCase();

		// Use SoftAssertions instead of direct assertThat methods
		// to collect all failing assertions in one go
		SoftAssertions softAssertions = new SoftAssertions();
		softAssertions.assertThat(response.getResponseObject()).as("Response object").isSameAs(responseObject);
		softAssertions.assertThat(response.getResponseObjectAsArray()).as("Response object as array")
				.isInstanceOf(Object[].class).containsExactly(responseObject);

		setupArraySuccessCase();

		softAssertions.assertThat(response.getResponseObject()).as("Response array").isSameAs(responseObject);
		softAssertions.assertThat(response.getResponseObjectAsArray()).as("Response array as array")
				.isSameAs(responseObject);

		// Don't forget to call SoftAssertions global verification !
		softAssertions.assertAll();
	}
}
