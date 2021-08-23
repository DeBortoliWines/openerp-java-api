package com.odoojava.api;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.apache.xmlrpc.XmlRpcException;
import org.junit.BeforeClass;
import org.junit.Test;
import com.odoojava.api.DemoDbGetter.DemoDbInfoRequester;
import com.odoojava.api.OdooXmlRpcProxy.RPCProtocol;

public class ReportAdapterTest {

	private static final String REPORT_NAME = "reportName";

	private static Session session;
	private static RPCProtocol protocol;
	private static String host;
	private static int port;
	private static String databaseName;
	private static String password;
	private static String userName;
	private ReportAdapter reportAdapter;

	/*
	 * Initialize connection data to demo db only once per test series
	 */

	@BeforeClass
	public static void setUp() throws Exception {
		DemoDbGetter.getDemoDb(new DemoDbConnectionDataSetter());
		session = new Session(protocol, host, port, databaseName, userName, password);

		session.startSession();
	}

	@Test(expected = com.odoojava.api.OdooApiException.class)
	public void testBadReportAdapter() throws OdooApiException, XmlRpcException {
		reportAdapter = session.getReportAdapter("account.invoice.bad.value");
	}

	@Test
	public void testGoodReportAdapter() {
		try {
			reportAdapter = session.getReportAdapter("account.report_invoice");
		} catch (OdooApiException | XmlRpcException e) {
			fail("Your report doesn't exists!");
		}
	}

	@Test
	public void testPrintReportAdapter() throws IOException, XmlRpcException, OdooApiException {
		testGoodReportAdapter();
		String filePath = reportAdapter.PrintReportToFileName(new Object[] { 1 });
		File report = new File(filePath);
		assert (report.exists());
		assert (report.length() > 0);
	}

	private static class DemoDbConnectionDataSetter implements DemoDbInfoRequester {
		@Override
		public void setProtocol(RPCProtocol protocol) {
			ReportAdapterTest.protocol = protocol;
		}

		@Override
		public void setHost(String host) {
			ReportAdapterTest.host = host;
		}

		@Override
		public void setPort(Integer port) {
			ReportAdapterTest.port = port;
		}

		@Override
		public void setDatabaseName(String databaseName) {
			ReportAdapterTest.databaseName = databaseName;
		}

		@Override
		public void setUserName(String userName) {
			ReportAdapterTest.userName = userName;
		}

		@Override
		public void setPassword(String password) {
			ReportAdapterTest.password = password;
		}
	}
}
