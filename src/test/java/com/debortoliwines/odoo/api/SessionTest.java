package com.debortoliwines.odoo.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.fail;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import org.apache.xmlrpc.XmlRpcException;
import org.assertj.core.api.SoftAssertions;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockserver.integration.ClientAndProxy;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.RegexBody;
import org.mockserver.model.StringBody;
import org.mockserver.socket.PortFactory;
import org.mockserver.socket.SSLFactory;
import org.mockserver.verify.VerificationTimes;

import com.debortoliwines.odoo.api.DemoDbGetter.DemoDbInfoRequester;
import com.debortoliwines.odoo.api.OdooXmlRpcProxy.RPCProtocol;

public class SessionTest {
	private static final String MOCKSERVER_HOST = "localhost";
	private static final String ADMIN = "admin";
	private static final String MOCK_DATABASE_NAME = "demo_90_1453880126";
	private static final int MOCKSERVER_PORT = PortFactory.findFreePort();
	private static final String WRONG_DB = "wrong_db";
	private static final String WRONG_DB_MESSAGE_REGEXP = "[\\w\\W]*[Dd]atabase \"?" + WRONG_DB
			+ "\"? does not exist[\\w\\W]*";
	private static final String LOGIN_FAILED_MESSAGE = "Incorrect username and/or password.  Login Failed.";
	private static final String WRONG_PASSWORD = "wrong_password";
	private static final String WRONG_USERNAME = "wrong_userName";
	private static RPCProtocol protocol;
	private static String host;
	private static Integer port;
	private static String databaseName;
	private static String userName;
	private static String password;

	private Session session;

	private static ClientAndProxy proxy;
	private static ClientAndServer mockServer;

	private static boolean useMockServer = true;
	private static SSLSocketFactory previousFactory;

	@BeforeClass
	public static void startProxy() throws Exception {
		if (isUsingMockServer()) {
			previousFactory = HttpsURLConnection.getDefaultSSLSocketFactory();
			HttpsURLConnection.setDefaultSSLSocketFactory(SSLFactory.getInstance().sslContext().getSocketFactory());
			proxy = ClientAndProxy.startClientAndProxy(PortFactory.findFreePort());
			mockServer = ClientAndServer.startClientAndServer(MOCKSERVER_PORT);
		}
	}

	@Before
	public void startMockServer() throws Exception {
		if (isUsingMockServer()) {
			mockServer.reset();
			mockValidLogin();
			mockGetContext();
		}
	}

	private static void mockDeniedDatabaseListing() {
		mockServer
				.when(request().withMethod("POST").withPath("/xmlrpc/2/db")
						.withBody(new StringBody(
								"<?xml version=\"1.0\" encoding=\"UTF-8\"?><methodCall><methodName>list</methodName><params/></methodCall>")))
				.respond(response().withStatusCode(200).withBody(
						"<?xml version='1.0'?>\n<methodResponse>\n<fault>\n<value><struct>\n<member>\n<name>faultCode</name>\n<value><int>3</int></value>\n</member>\n<member>\n<name>faultString</name>\n<value><string>Access denied</string></value>\n</member>\n</struct></value>\n</fault>\n</methodResponse>\n"));
	}

	private static void mockAllowedDatabaseListing() {
		mockServer
				.when(request().withMethod("POST").withPath("/xmlrpc/2/db")
						.withBody(new StringBody(
								"<?xml version=\"1.0\" encoding=\"UTF-8\"?><methodCall><methodName>list</methodName><params/></methodCall>")))
				.respond(response().withStatusCode(200).withBody(
						"<?xml version='1.0'?>\n<methodResponse>\n<params>\n<param>\n<value>\n<array>\n<data>\n<value><string>"
								+ MOCK_DATABASE_NAME
								+ "</string></value>\n</data>\n</array>\n</value>\n</param>\n</params>\n</methodResponse>\n"));
	}

	private static void mockValidLogin() {
		mockServer
				.when(request().withMethod("POST").withPath("/xmlrpc/2/common").withBody(new StringBody(
						"<?xml version=\"1.0\" encoding=\"UTF-8\"?><methodCall><methodName>login</methodName><params><param><value>"
								+ MOCK_DATABASE_NAME + "</value></param><param><value>" + ADMIN
								+ "</value></param><param><value>" + ADMIN + "</value></param></params></methodCall>")))
				.respond(response().withStatusCode(200).withBody(
						"<?xml version='1.0'?>\n<methodResponse>\n<params>\n<param>\n<value><int>1</int></value>\n</param>\n</params>\n</methodResponse>\n"));
	}

	private static void mockGetContext() {
		mockServer
				.when(request().withMethod("POST").withPath("/xmlrpc/2/object").withBody(new StringBody(
						"<?xml version=\"1.0\" encoding=\"UTF-8\"?><methodCall><methodName>execute</methodName><params><param><value>"
								+ MOCK_DATABASE_NAME
								+ "</value></param><param><value><i4>1</i4></value></param><param><value>" + ADMIN
								+ "</value></param><param><value>res.users</value></param><param><value>context_get</value></param></params></methodCall>")))
				.respond(response().withStatusCode(200).withBody(
						"<?xml version='1.0'?>\n<methodResponse>\n<params>\n<param>\n<value><struct>\n<member>\n<name>lang</name>\n<value><string>en_US</string></value>\n</member>\n<member>\n<name>tz</name>\n<value><string>Europe/Brussels</string></value>\n</member>\n</struct></value>\n</param>\n</params>\n</methodResponse>\n"));
	}

	private static void mockLoginWrongUsername() {
		mockServer
				.when(request().withMethod("POST").withPath("/xmlrpc/2/common")
						.withBody(new StringBody(
								"<?xml version=\"1.0\" encoding=\"UTF-8\"?><methodCall><methodName>login</methodName><params><param><value>"
										+ MOCK_DATABASE_NAME
										+ "</value></param><param><value>wrong_userName</value></param><param><value>"
										+ ADMIN + "</value></param></params></methodCall>")))
				.respond(response().withStatusCode(200).withBody(
						"<?xml version='1.0'?>\n<methodResponse>\n<params>\n<param>\n<value><boolean>0</boolean></value>\n</param>\n</params>\n</methodResponse>\n"));
	}

	private static void mockLoginWrongPassword() {
		mockServer
				.when(request().withMethod("POST").withPath("/xmlrpc/2/common")
						.withBody(new StringBody(
								"<?xml version=\"1.0\" encoding=\"UTF-8\"?><methodCall><methodName>login</methodName><params><param><value>"
										+ MOCK_DATABASE_NAME + "</value></param><param><value>" + ADMIN
										+ "</value></param><param><value>wrong_password</value></param></params></methodCall>")))
				.respond(response().withStatusCode(200).withBody(
						"<?xml version='1.0'?>\n<methodResponse>\n<params>\n<param>\n<value><boolean>0</boolean></value>\n</param>\n</params>\n</methodResponse>\n"));
	}

	private static void mockLoginWrongDb() {
		mockServer
				.when(request().withMethod("POST").withPath("/xmlrpc/2/common")
						.withBody(new StringBody(
								"<?xml version=\"1.0\" encoding=\"UTF-8\"?><methodCall><methodName>login</methodName><params><param><value>wrong_db</value></param><param><value>"
										+ ADMIN + "</value></param><param><value>" + ADMIN
										+ "</value></param></params></methodCall>")))
				.respond(response().withStatusCode(200).withBody(
						"<?xml version='1.0'?>\n<methodResponse>\n<fault>\n<value><struct>\n<member>\n<name>faultCode</name>\n<value><int>1</int></value>\n</member>\n<member>\n<name>faultString</name>\n<value><string>Traceback (most recent call last):\n  File \"/home/odoo/src/odoo/9.0/openerp/service/wsgi_server.py\", line 56, in xmlrpc_return\n    result = openerp.http.dispatch_rpc(service, method, params)\n  File \"/home/odoo/src/odoo/9.0/openerp/http.py\", line 114, in dispatch_rpc\n    result = dispatch(method, params)\n  File \"/home/odoo/src/odoo/9.0/openerp/service/common.py\", line 57, in dispatch\n    return g[exp_method_name](*params)\n  File \"/home/odoo/src/odoo/9.0/openerp/service/common.py\", line 23, in exp_login\n    res = security.login(db, login, password)\n  File \"/home/odoo/src/odoo/9.0/openerp/service/security.py\", line 8, in login\n    res_users = openerp.registry(db)['res.users']\n  File \"/home/odoo/src/odoo/9.0/openerp/__init__.py\", line 50, in registry\n    return modules.registry.RegistryManager.get(database_name)\n  File \"/home/odoo/src/odoo/9.0/openerp/modules/registry.py\", line 354, in get\n    update_module)\n  File \"/home/odoo/src/odoo/9.0/openerp/modules/registry.py\", line 371, in new\n    registry = Registry(db_name)\n  File \"/home/odoo/src/odoo/9.0/openerp/modules/registry.py\", line 63, in __init__\n    cr = self.cursor()\n  File \"/home/odoo/src/odoo/9.0/openerp/modules/registry.py\", line 278, in cursor\n    return self._db.cursor()\n  File \"/home/odoo/src/odoo/9.0/openerp/sql_db.py\", line 556, in cursor\n    return Cursor(self.__pool, self.dbname, self.dsn, serialized=serialized)\n  File \"/home/odoo/src/odoo/9.0/openerp/sql_db.py\", line 162, in __init__\n    self._cnx = pool.borrow(dsn)\n  File \"/home/odoo/src/odoo/9.0/openerp/sql_db.py\", line 445, in _locked\n    return fun(self, *args, **kwargs)\n  File \"/home/odoo/src/odoo/9.0/openerp/sql_db.py\", line 507, in borrow\n    result = psycopg2.connect(dsn=dsn, connection_factory=PsycoConnection)\n  File \"/usr/lib/python2.7/dist-packages/psycopg2/__init__.py\", line 179, in connect\n    connection_factory=connection_factory, async=async)\nOperationalError: FATAL:  database \"wrong_db\" does not exist\n\n</string></value>\n</member>\n</struct></value>\n</fault>\n</methodResponse>\n"));
	}

	private static void mockServerVersionResponse() {
		mockServer
				.when(request().withMethod("POST").withPath("/xmlrpc/2/db")
						.withBody(new StringBody(
								"<?xml version=\"1.0\" encoding=\"UTF-8\"?><methodCall><methodName>server_version</methodName><params/></methodCall>")))
				.respond(response().withStatusCode(200).withBody(
						"<?xml version='1.0'?>\n<methodResponse>\n<params>\n<param>\n<value><string>9.0e</string></value>\n</param>\n</params>\n</methodResponse>\n"));
	}

	@AfterClass
	public static void stopProxy() throws Exception {
		if (isUsingMockServer()) {
			mockServer.stop();
			proxy.dumpToLogAsJava();
			proxy.stop();
			HttpsURLConnection.setDefaultSSLSocketFactory(previousFactory);
		}
	}

	/*
	 * Initialize connection data to demo db only once per test series
	 */
	@BeforeClass
	public static void requestDemoDb() throws IOException, XmlRpcException {
		if (isUsingMockServer()) {
			SessionTest.protocol = RPCProtocol.RPC_HTTPS;
			SessionTest.host = MOCKSERVER_HOST;
			SessionTest.port = MOCKSERVER_PORT;
			SessionTest.userName = ADMIN;
			SessionTest.password = ADMIN;
			SessionTest.databaseName = MOCK_DATABASE_NAME;
		} else {
			DemoDbGetter.getDemoDb(new DemoDbConnectionDataSetter());
		}

	}

	@Before
	public void setUp() throws Exception {
		session = new Session(protocol, host, port, databaseName, userName, password);
	}

	@Test
	public void should_throw_when_parameters_are_incorrect() throws Exception {
		if (isUsingMockServer()) {
			mockLoginWrongUsername();
			mockLoginWrongPassword();
			mockLoginWrongDb();
		}

		Session badSession = new Session(RPCProtocol.RPC_HTTPS, host, port, databaseName, WRONG_USERNAME, password);
		Throwable thrown = catchThrowable(() -> badSession.startSession());

		Session badSession2 = new Session(RPCProtocol.RPC_HTTPS, host, port, databaseName, userName, WRONG_PASSWORD);
		Throwable thrown2 = catchThrowable(() -> badSession2.startSession());

		Session badSession3 = new Session(RPCProtocol.RPC_HTTPS, host, port, WRONG_DB, userName, password);
		Throwable thrown3 = catchThrowable(() -> badSession3.startSession());

		Session badSession4 = new Session(RPCProtocol.RPC_HTTPS, host, 1234, databaseName, userName, password);
		Throwable thrown4 = catchThrowable(() -> badSession4.startSession());

		// use SoftAssertions instead of direct assertThat methods
		// to collect all failing assertions in one go
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(thrown).as("Bad userName").isInstanceOf(Exception.class).hasMessage(LOGIN_FAILED_MESSAGE);
		softly.assertThat(thrown2).as("Bad password").isInstanceOf(Exception.class).hasMessage(LOGIN_FAILED_MESSAGE);
		softly.assertThat(thrown3).as("Bad db").isInstanceOf(XmlRpcException.class)
				.hasMessageMatching(WRONG_DB_MESSAGE_REGEXP);
		softly.assertThat(thrown4).as("Bad port").isInstanceOf(XmlRpcException.class);

		// Don't forget to call SoftAssertions global verification !
		softly.assertAll();
	}

	@Test
	public void should_contain_tags_in_context_after_start() throws Exception {
		// Need to be authenticated to be allowed to fetch the remote context
		session.authenticate();
		session.getRemoteContext();
		assertThat(session.getContext()).as("Session context").containsEntry(Context.ActiveTestTag, true)
				.containsKeys(Context.LangTag, Context.TimezoneTag);
	}

	@Test
	public void should_provide_userId_after_authenticate() throws Exception {
		int userId = session.authenticate();
		assertThat(userId).as("User ID").isNotZero();
	}

	@Test
	public void shoudl_return_database_list_or_throw() throws Exception {
		// use SoftAssertions instead of direct assertThat methods
		// to collect all failing assertions in one go
		SoftAssertions softly = new SoftAssertions();

		if (isUsingMockServer()) {
			mockAllowedDatabaseListing();
		}

		try {
			ArrayList<String> databaseList = Session.getDatabaseList(protocol, host, port);
			softly.assertThat(databaseList).as("List of databases on server " + host).contains(databaseName);
		} catch (XmlRpcException e) {
			if (isUsingMockServer()) {
				softly.assertThat(e).as("Database listing allowed, no exception should have been throwed").isNull();
			} else {
				// Listing is denied on demo server
				softly.assertThat(e).as("XmlRpcException thrown when access is denied")
						.isInstanceOf(XmlRpcException.class).hasMessage("Access denied");
			}
		}

		if (isUsingMockServer()) {
			mockServer.reset();
			mockDeniedDatabaseListing();
		}

		Throwable thrown = catchThrowable(() -> Session.getDatabaseList(protocol, host, port));
		softly.assertThat(thrown).as("XmlRpcException thrown when access is denied").isInstanceOf(XmlRpcException.class)
				.hasMessage("Access denied");

		// Don't forget to call SoftAssertions global verification !
		softly.assertAll();
	}

	private static boolean isUsingMockServer() {
		return useMockServer;
	}

	@Test
	public void should_find_database_in_listing_or_throw() throws Exception {
		// use SoftAssertions instead of direct assertThat methods
		// to collect all failing assertions in one go
		SoftAssertions softly = new SoftAssertions();

		// Listing denied
		if (isUsingMockServer()) {
			mockDeniedDatabaseListing();
		}

		Throwable thrown = catchThrowable(() -> session.checkDatabasePresence());
		softly.assertThat(thrown).as("Exception thrown when db listing disabled").isInstanceOf(Exception.class)
				.hasMessage("Access denied");

		// Listing not allowed on demo server
		if (isUsingMockServer()) {

			// Listing allowed and db found
			mockServer.reset();
			mockAllowedDatabaseListing();

			thrown = catchThrowable(() -> session.checkDatabasePresence());

			softly.assertThat(thrown).as("Exception thrown when db listing enabled and db should be found").isNull();

			// Listing allowed and db not found
			session = new Session(protocol, host, port, WRONG_DB, userName, password);
			thrown = catchThrowable(() -> session.checkDatabasePresence());
			softly.assertThat(thrown).as("Exception thrown when db listing enabled but db not found")
					.isInstanceOf(Exception.class)
					.hasMessage("Error while connecting to Odoo.  Database [" + WRONG_DB
							+ "]  was not found in the following list: " + System.getProperty("line.separator")
							+ System.getProperty("line.separator") + MOCK_DATABASE_NAME
							+ System.getProperty("line.separator"));

		}

		// Don't forget to call SoftAssertions global verification !
		softly.assertAll();
	}

	@Test
	public void should_return_server_version() throws Exception {
		if (isUsingMockServer()) {
			mockServerVersionResponse();
		}
		Version version = session.getServerVersion();
		assertThat(version).as("Server version").isNotNull();

	}

	static boolean checkedDatabasePresence = false, authenticated = false, fetchedRemoteContext = false;

	@Test
	public void should_authenticate_and_fetch_remote_context_on_session_start() throws Exception {
		session = new Session(protocol, host, port, databaseName, userName, password) {
			@Override
			int authenticate() throws XmlRpcException, Exception {
				authenticated = true;
				return 1;
			}

			@Override
			void checkDatabasePresenceSafe() {
				checkedDatabasePresence = true;
			}

			@Override
			void getRemoteContext() throws XmlRpcException {
				fetchedRemoteContext = true;
			}
		};
		session.startSession();
		// use SoftAssertions instead of direct assertThat methods
		// to collect all failing assertions in one go
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(checkedDatabasePresence).as("Checked for database presence").isTrue();
		softly.assertThat(authenticated).as("Authenticated").isTrue();
		softly.assertThat(fetchedRemoteContext).as("Fetched remote context").isTrue();
		// Don't forget to call SoftAssertions global verification !
		softly.assertAll();
	}

	@Test
	public void should_send_given_parameters() throws Exception {
		// Check parameters given are sent in order
		if (isUsingMockServer()) {
			mockServer
					.when(request().withMethod("POST").withPath("/xmlrpc/2/object")
							.withBody(new RegexBody(".*res.users.*context_get.*")))
					.respond(response().withStatusCode(200).withBody(
							"<?xml version='1.0'?>\n<methodResponse>\n<params>\n<param>\n<value><int>1</int></value>\n</param>\n</params>\n</methodResponse>\n"));
		} else {
			session.startSession();
		}
		Object[] parameters = new Object[] { "parameter1" };
		session.executeCommand("res.users", "context_get", parameters);
		if (isUsingMockServer()) {
			mockServer.verify(request().withBody(new RegexBody(".*parameter1.*")), VerificationTimes.once());
		}

		// Check empty array is working
		parameters = new Object[] {};
		session.executeCommand("res.users", "context_get", parameters);
		if (isUsingMockServer()) {
			mockServer.verify(request().withBody(new RegexBody(".*res.users.*context_get.*")),
					VerificationTimes.exactly(2));
		}

		// Check null is working
		parameters = null;
		session.executeCommand("res.users", "context_get", parameters);
		if (isUsingMockServer()) {
			mockServer.verify(request().withBody(new RegexBody(".*res.users.*context_get.*")),
					VerificationTimes.exactly(3));
		}
	}

	@Test
	public void should_call_method_execute_on_executeCommand() throws Exception {
		if (isUsingMockServer()) {
			mockServer
					.when(request().withPath("/xmlrpc/2/object")
							.withBody(RegexBody.regex(".*<methodName>execute</methodName>.*")))
					.respond(response().withStatusCode(200).withBody(
							"<?xml version='1.0'?>\n<methodResponse>\n<params>\n<param>\n<value><int>1</int></value>\n</param>\n</params>\n</methodResponse>\n"));
		} else {
			session.startSession();
		}
		session.executeCommand("res.users", "context_get", null);
		if (isUsingMockServer()) {
			mockServer.verify(request().withBody(new RegexBody(".*<methodName>execute</methodName>.*")),
					VerificationTimes.once());
		}
	}

	@Test
	public void should_call_method_exec_workflow_on_executeCommand() throws Exception {
		if (isUsingMockServer()) {
			mockServer
					.when(request().withPath("/xmlrpc/2/object")
							.withBody(RegexBody.regex(".*<methodName>exec_workflow</methodName>.*")))
					.respond(response().withStatusCode(200).withBody(
							"<?xml version='1.0'?>\n<methodResponse>\n<params>\n<param>\n<value><int>1</int></value>\n</param>\n</params>\n</methodResponse>\n"));
		} else {
			session.startSession();
		}
		session.executeWorkflow("account.invoice", "invoice_open", 42424242);
		if (isUsingMockServer()) {
			mockServer.verify(request().withBody(new RegexBody(".*<methodName>exec_workflow</methodName>.*")),
					VerificationTimes.once());
		}
	}

	@Test
	public void should_default_protocol_to_http() throws Exception {
		session = new Session(host, port, databaseName, userName, password);
		Field protocolField = Session.class.getDeclaredField("protocol");
		if (protocolField != null) {
			protocolField.setAccessible(true);
		} else {
			fail("protocol field not found in class " + Session.class.getName());
		}
		assertThat(protocolField.get(session)).isEqualTo(RPCProtocol.RPC_HTTP);
	}

	/**
	 * Only used to set the static data for connection on the main class
	 */
	private static class DemoDbConnectionDataSetter implements DemoDbInfoRequester {
		@Override
		public void setProtocol(RPCProtocol protocol) {
			SessionTest.protocol = protocol;
		}

		@Override
		public void setHost(String host) {
			SessionTest.host = host;
		}

		@Override
		public void setPort(Integer port) {
			SessionTest.port = port;
		}

		@Override
		public void setDatabaseName(String databaseName) {
			SessionTest.databaseName = databaseName;
		}

		@Override
		public void setUserName(String userName) {
			SessionTest.userName = userName;
		}

		@Override
		public void setPassword(String password) {
			SessionTest.password = password;
		}
	}
}
