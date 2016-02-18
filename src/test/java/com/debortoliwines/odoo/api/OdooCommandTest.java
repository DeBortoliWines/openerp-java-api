package com.debortoliwines.odoo.api;

import org.apache.xmlrpc.XmlRpcException;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;

public class OdooCommandTest {

	private static final XmlRpcException XML_RPC_EXCEPTION = new XmlRpcException("failed");

	@Test
	public void should_return_failed_response_if_exception() throws Exception {
		String unusedHost = null;
		int unusedPort = 0;
		String unusedDatabaseName = null;
		String unusedUserName = null;
		String unusedPassword = null;
		Session session = new Session(unusedHost, unusedPort, unusedDatabaseName, unusedUserName, unusedPassword) {
			@Override
			public Object executeCommand(String objectName, String commandName, Object[] parameters)
					throws XmlRpcException {
				throw XML_RPC_EXCEPTION;
			}
		};
		OdooCommand command = new OdooCommand(session);

		Response response = command.callObjectFunction(null, null, null);
		// Use SoftAssertions instead of direct assertThat methods
		// to collect all failing assertions in one go
		SoftAssertions softAssertions = new SoftAssertions();
		softAssertions.assertThat(response.isSuccessful()).as("Is successful").isFalse();
		softAssertions.assertThat(response.getErrorCause()).as("Error cause").isSameAs(XML_RPC_EXCEPTION);

		// Don't forget to call SoftAssertions global verification !
		softAssertions.assertAll();
	}
}
