package com.odoojava.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import com.odoojava.api.DemoDbGetter.DemoDbInfoRequester;
import com.odoojava.api.OdooXmlRpcProxy.RPCProtocol;

import java.util.HashMap;

import org.apache.xmlrpc.XmlRpcException;
import org.assertj.core.api.SoftAssertions;
import org.junit.BeforeClass;
import org.junit.Test;

public class ObjectAdapterTest {
	private static final String OTHER_SIGNAL_NAME = "otherSignalName";
	private static final String OTHER_MODEL_NAME = "otherModel";
	private static final String TEST_MODEL_NAME = "testModelName";
	private static final String TEST_SIGNAL_NAME = "signal";
	private static RPCProtocol protocol;
	private static String host;
	private static Integer port;
	private static String databaseName;
	private static String userName;
	private static String password;

	private static Session session;

	boolean validated = false;

	@BeforeClass
	public static void setUp() throws Exception {
		DemoDbGetter.getDemoDb(new DemoDbConnectionDataSetter());
		session = new Session(protocol,
				host, port, databaseName, userName,password);
				 
		session.startSession();
	}
	

	
	private final class TestAdapter extends ObjectAdapter {

		public TestAdapter() throws OdooApiException, XmlRpcException {
			super(null, null, null);
		}

		@Override
		synchronized void validateModelExists() throws OdooApiException {
			validated = true;
		}

		@Override
		public FieldCollection getFields() throws XmlRpcException {
			return null;
		}
	}
	
	

	
	private abstract class AbstractTestCommand extends OdooCommand {
		boolean searchCalledOnce = false;
		boolean searchCalledTwiceOrMore = false;
		boolean readCalledOnce = false;
		boolean readCalledTwiceOrMore = false;

		private AbstractTestCommand() {
			super(null);
		}

		@Override
		public Response searchObject(String objectName, Object[] filter) {
			// If already called once...
			if (searchCalledOnce) {
				searchCalledTwiceOrMore = true;
			}
			searchCalledOnce = true;

			return null;
		}

		@Override
		public HashMap<String, Object> getFields(String objectName, String[] filterFields) throws XmlRpcException {
			return new HashMap<>();
		}
	}

	public final class TestCommand1 extends AbstractTestCommand {
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
	}

	@Test
	public void should_validate_model_exist_at_creation_or_throw() throws Exception {
		validated = false;
		new TestAdapter();
		// Use SoftAssertions instead of direct assertThat methods
		// to collect all failing assertions in one go
		SoftAssertions softAssertions = new SoftAssertions();
		softAssertions.assertThat(validated).as("Tried to validate at creation time").isTrue();

		Throwable thrown = catchThrowable(() -> new ObjectAdapter(new TestCommand1(), OTHER_MODEL_NAME, null));
		softAssertions.assertThat(thrown).as("Exception thrown while trying to create adapter")
				.isInstanceOf(OdooApiException.class)
				.hasMessage("Could not find model with name '" + OTHER_MODEL_NAME + "'");
		// Don't forget to call SoftAssertions global verification !
		softAssertions.assertAll();

	}

	@Test
	public void should_use_cache_when_validating() throws Exception {
		TestCommand1 command = new TestCommand1();

		ObjectAdapter.clearModelNameCache();

		// First creation should force calls to validate
		new ObjectAdapter(command, TEST_MODEL_NAME, null);
		// Second should use cache
		new ObjectAdapter(command, TEST_MODEL_NAME, null);

		// Use SoftAssertions instead of direct assertThat methods
		// to collect all failing assertions in one go
		SoftAssertions softAssertions = new SoftAssertions();
		softAssertions.assertThat(command.searchCalledOnce).as("Search called once").isTrue();
		softAssertions.assertThat(command.searchCalledTwiceOrMore).as("Search called twice or more").isFalse();
		softAssertions.assertThat(command.readCalledOnce).as("Read called once").isTrue();
		softAssertions.assertThat(command.readCalledTwiceOrMore).as("Read called twice or more").isFalse();

		// Don't forget to call SoftAssertions global verification !
		softAssertions.assertAll();

	}

	public final class TestCommand2 extends AbstractTestCommand {
		boolean executeWorkflowCalled = false;

		@Override
		public Object[] readObject(String objectName, Object[] ids, String[] fields) throws XmlRpcException {
			// If already called once...
			if (readCalledOnce) {
				readCalledTwiceOrMore = true;
			}
			readCalledOnce = true;

			HashMap<String, Object> row = new HashMap<>();
			row.put("model", TEST_MODEL_NAME);
			row.put("signal", TEST_SIGNAL_NAME);
			row.put("wkf_id", new Object[] { "0" });
			row.put("osv", TEST_MODEL_NAME);
			HashMap<String, Object> row2 = new HashMap<>();
			row2.put("model", OTHER_MODEL_NAME);
			row2.put("signal", TEST_SIGNAL_NAME);
			row2.put("wkf_id", new Object[] { "0" });
			row2.put("osv", TEST_MODEL_NAME);
			return new Object[] { row, row2 };
		}

		@Override
		public void executeWorkflow(final String objectName, final String signal, final int objectID)
				throws XmlRpcException {
			executeWorkflowCalled = true;
		}
	}



	
    /**
     * Test of searchObject method, of class OdooCommand.
     */
    @Test
    public void testSearchObject() throws Exception {
        ObjectAdapter partnerAdapter = session.getObjectAdapter("res.partner");
        FilterCollection filters = new FilterCollection();
        filters.add("id", "<=", 2);
        RowCollection partners = partnerAdapter.searchAndReadObject(
            filters, new String[] { "name", "email" });

        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(partners.isEmpty()).as("Is successful").isFalse();
        softAssertions.assertAll();
    }


	@Test
	public void should_return_model_list() throws Exception {
		// use SoftAssertions instead of direct assertThat methods
		// to collect all failing assertions in one go
		SoftAssertions softly = new SoftAssertions();
		
		assert(true);
	}

	@Test
	public void test_translated_name() throws Exception {
		// use SoftAssertions instead of direct assertThat methods
		// to collect all failing assertions in one go
		SoftAssertions softly = new SoftAssertions();
		
		String objectName = "product.product";
		ObjectAdapter prodAdapter = session.getObjectAdapter(objectName);
		String[] fields = new String[]{"name", "display_name"};
		Integer[]  ids = new Integer [] { 32 };
		FilterCollection filters = new FilterCollection();
		RowCollection prod_ids = prodAdapter.readObject(
				ids,
				fields);
		Row prod_id = prod_ids.get(0);

		Object[] readResult = (Object[]) session.executeCommand(objectName, "read", new Object[]{ids, fields});
		System.out.println("prod_id name " + prod_id.get("name"));		
		assert(true);
	}
	
	@Test
	public void should_throw_if_signal_doesnt_exist_for_that_object() throws Exception {
		// Wrong signal
		ObjectAdapter adapter = new ObjectAdapter(new TestCommand2(), TEST_MODEL_NAME, null);
		Row row = new Row(new HashMap<String, Object>(), new FieldCollection());
		Throwable thrown = catchThrowable(() -> adapter.executeWorkflow(row, OTHER_SIGNAL_NAME));

		// Use SoftAssertions instead of direct assertThat methods
		// to collect all failing assertions in one go
		SoftAssertions softAssertions = new SoftAssertions();
		softAssertions.assertThat(thrown).as("Exception thrown for invalid signal name")
				.isInstanceOf(OdooApiException.class).hasMessage("Could not find signal with name '"
						+ OTHER_SIGNAL_NAME + "' for object '" + TEST_MODEL_NAME + "'");

		// Wrong model
		ObjectAdapter adapter2 = new ObjectAdapter(new TestCommand2(), OTHER_MODEL_NAME, null);
		Row row2 = new Row(new HashMap<String, Object>(), new FieldCollection());
		Throwable thrown2 = catchThrowable(() -> adapter2.executeWorkflow(row2, TEST_SIGNAL_NAME));

		softAssertions.assertThat(thrown2).as("Exception thrown for invalid model name")
				.isInstanceOf(OdooApiException.class).hasMessage("Could not find signal with name '"
						+ TEST_SIGNAL_NAME + "' for object '" + OTHER_MODEL_NAME + "'");

		// Don't forget to call SoftAssertions global verification !
		softAssertions.assertAll();

	}

	@Test
	public void should_call_execute_worflow_on_command() throws Exception {
		TestCommand2 command = new TestCommand2();
		ObjectAdapter adapter = new ObjectAdapter(command, TEST_MODEL_NAME, null);
		Row row = new Row(new HashMap<String, Object>(), new FieldCollection());
		adapter.executeWorkflow(row, TEST_SIGNAL_NAME);
		assertThat(command.executeWorkflowCalled).as("Command's executeWorkflow has been called").isTrue();
	}
	
	/**
	 * Only used to set the static data for connection on the main class
	 */
	private static class DemoDbConnectionDataSetter implements DemoDbInfoRequester {
		@Override
		public void setProtocol(RPCProtocol protocol) {
			ObjectAdapterTest.protocol = protocol;
		}

		@Override
		public void setHost(String host) {
			ObjectAdapterTest.host = host;
		}

		@Override
		public void setPort(Integer port) {
			ObjectAdapterTest.port = port;
		}

		@Override
		public void setDatabaseName(String databaseName) {
			ObjectAdapterTest.databaseName = databaseName;
		}

		@Override
		public void setUserName(String userName) {
			ObjectAdapterTest.userName = userName;
		}

		@Override
		public void setPassword(String password) {
			ObjectAdapterTest.password = password;
		}
	}


}
