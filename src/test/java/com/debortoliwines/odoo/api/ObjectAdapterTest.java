package com.debortoliwines.odoo.api;

import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.HashMap;

import org.apache.xmlrpc.XmlRpcException;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;

public class ObjectAdapterTest {
	private static final String OTHER_MODEL_NAME = "otherModel";
	private static final String TEST_MODEL_NAME = "testModelName";

	boolean validated = false;
	private final class TestAdapter extends ObjectAdapter {

		public TestAdapter() throws OpeneERPApiException, XmlRpcException {
			super(null, null, null);
		}

		@Override
		synchronized void validateModelExists() throws OpeneERPApiException {
			validated = true;
		}

		@Override
		public FieldCollection getFields() throws XmlRpcException {
			return null;
		}
	}

	private final class TestCommand extends OpenERPCommand {
		boolean searchCalledOnce = false;
		boolean searchCalledTwiceOrMore = false;
		boolean readCalledOnce = false;
		boolean readCalledTwiceOrMore = false;

		private TestCommand() {
			super(null);
		}

		@Override
		public Object[] searchObject(String objectName, Object[] filter) throws XmlRpcException {
			// If already called once...
			if (searchCalledOnce) {
				searchCalledTwiceOrMore = true;
			}
			searchCalledOnce = true;

			return null;
		}

		@Override
		public Object[] readObject(String objectName, Object[] ids, String[] fields) throws XmlRpcException {
			// If already called once...
			if (readCalledOnce) {
				readCalledTwiceOrMore = true;
			}
			readCalledOnce = true;

			HashMap<String, Object> row = new HashMap<>();
			row.put("model", TEST_MODEL_NAME);
			return new Object[] { row };
		}

		@Override
		public HashMap<String, Object> getFields(String objectName, String[] filterFields) throws XmlRpcException {
			return new HashMap<>();
		}
	}

	@Test
	public void should_validate_model_exist_at_creation_or_throw() throws Exception {
		validated = false;
		new TestAdapter();
		// Use SoftAssertions instead of direct assertThat methods
		// to collect all failing assertions in one go
		SoftAssertions softAssertions = new SoftAssertions();
		softAssertions.assertThat(validated).as("Tried to validate at creation time").isTrue();

		Throwable thrown = catchThrowable(() -> new ObjectAdapter(new TestCommand(), OTHER_MODEL_NAME, null));
		softAssertions.assertThat(thrown).as("Exception thrown while trying to create adapter")
				.isInstanceOf(OpeneERPApiException.class)
				.hasMessage("Could not find model with name '" + OTHER_MODEL_NAME + "'");
		// Don't forget to call SoftAssertions global verification !
		softAssertions.assertAll();

	}

	@Test
	public void should_use_cache_when_validating() throws Exception {
		TestCommand command = new TestCommand();

		// First creation should force calls to validate
		new ObjectAdapter(command, TEST_MODEL_NAME, null);
		// Second should use cache
		new ObjectAdapter(command, TEST_MODEL_NAME, null);

		// Use SoftAssertions instead of direct assertThat methods
		// to collect all failing assertions in one go
		SoftAssertions softAssertions = new SoftAssertions();
		softAssertions.assertThat(command.searchCalledOnce).isTrue();
		softAssertions.assertThat(command.searchCalledTwiceOrMore).isFalse();
		softAssertions.assertThat(command.readCalledOnce).isTrue();
		softAssertions.assertThat(command.readCalledTwiceOrMore).isFalse();

		// Don't forget to call SoftAssertions global verification !
		softAssertions.assertAll();

	}

}
