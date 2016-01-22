package com.debortoliwines.odoo.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.xmlrpc.XmlRpcException;
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.debortoliwines.odoo.api.DemoDbGetter.DemoDbInfoRequester;
import com.debortoliwines.odoo.api.OpenERPXmlRpcProxy.RPCProtocol;

public class SessionTest {
	private static final String WRONG_DB = "wrong_db";
	private static final String WRONG_DB_MESSAGE_REGEXP = "[\\w\\W]*[Dd]atabase \"?" + WRONG_DB
			+ "\"? does not exist[\\w\\W]*";// ".*[Dd]atabase
																														// \"?"
																														// +
																														// WRONG_DB
																														// +
																														// "\"?
																														// does
																														// not
																														// exist";
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

	/*
	 * Initialize connection data to demo db only once per test series
	 * 
	 * Tries to re-use previous connection data first.
	 * 
	 * @throws IOException
	 * 
	 * @throws XmlRpcException
	 */
	@BeforeClass
	public static void requestDemoDb() throws IOException, XmlRpcException {
		DemoDbGetter.getDemoDb(new DemoDbConnectionDataSetter());
	}

	@Before
	public void setUp() throws Exception {
		session = new Session(protocol, host, port, databaseName, userName, password);
	}

	@Test
	public void should_throw_when_parameters_are_incorrect() throws Exception {
		Session badSession = new Session(RPCProtocol.RPC_HTTPS, host, port, databaseName, WRONG_USERNAME, password);
		Throwable thrown = catchThrowable(() -> badSession.startSession());

		Session badSession2 = new Session(RPCProtocol.RPC_HTTPS, host, port, databaseName, userName, WRONG_PASSWORD);
		Throwable thrown2 = catchThrowable(() -> badSession2.startSession());

		Session badSession3 = new Session(RPCProtocol.RPC_HTTPS, host, port, WRONG_DB, userName, password);
		Throwable thrown3 = catchThrowable(() -> badSession3.startSession());

		Session badSession4 = new Session(RPCProtocol.RPC_HTTPS, host, 1234, databaseName, userName, password);
		Throwable thrown4 = catchThrowable(() -> badSession4.startSession());

		Session badSession5 = new Session(RPCProtocol.RPC_HTTPS, "localhost", port, databaseName, userName,
				password);
		Throwable thrown5 = catchThrowable(() -> badSession5.startSession());

		// use SoftAssertions instead of direct assertThat methods
		// to collect all failing assertions in one go
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(thrown).as("Bad userName").isInstanceOf(Exception.class).hasMessage(LOGIN_FAILED_MESSAGE);
		softly.assertThat(thrown2).as("Bad password").isInstanceOf(Exception.class).hasMessage(LOGIN_FAILED_MESSAGE);
		softly.assertThat(thrown3).as("Bad db").isInstanceOf(Exception.class)
				.hasMessageMatching(WRONG_DB_MESSAGE_REGEXP);
		softly.assertThat(thrown4).as("Bad port").isInstanceOf(XmlRpcException.class);
		softly.assertThat(thrown5).as("Bad host").isInstanceOf(XmlRpcException.class);
		// Don't forget to call SoftAssertions global verification !
		softly.assertAll();
	}

	@Test
	public void should_contain_tags_in_context_after_start() throws Exception {
		session.startSession();
		assertThat(session.getContext()).as("Session context").containsEntry(Context.ActiveTestTag, true)
				.containsKeys(Context.LangTag, Context.TimezoneTag);
	}

	@Test
	public void should_provide_userId_after_authenticate() throws Exception {
		int userId = session.authenticate();
		assertThat(userId).as("User ID").isNotZero();
	}

	@Test
	public void shoudl_return_database_list() throws Exception {
		// Either a list of available databases is returned, or the odoo server
		// returns an error with a message instead of a fault code, leading to a
		// ClassCastException
		try {
			ArrayList<String> databaseList = session.getDatabaseList(host, port);
			assertThat(databaseList).as("List of databases on server " + host).contains(databaseName);
		} catch (ClassCastException e) {
			assertThat(e)
					.as("ClassCastException thrown by XmlRpc when trying to read faultCode and Odoo returned a message")
					.hasMessage("java.lang.String cannot be cast to java.lang.Integer");
		} catch (XmlRpcException e) {
			assertThat(e).as("XmlRpcException thrown when access is denied").hasMessage("Access denied");
		}
	}

	@Test
	public void shoudl_return_server_version() throws Exception {
		Version version = session.getServerVersion();
		assertThat(version).as("Server version").isNotNull();
		assertThat(version.getMajor()).isGreaterThanOrEqualTo(8);
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
