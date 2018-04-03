/*
 *   Copyright 2018, 2020 Mind And Go
 *
 *   This file is part of OdooJavaAPI.
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
package com.odoojava.api;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.xmlrpc.XmlRpcException;

import com.odoojava.api.Field.FieldType;
import com.odoojava.api.helpers.FilterHelper;

/**
 * 
 * Main class for managing reports with the server.
 * 
 * @author Florent THOMAS
 * @param: reportListCache
 *             . Consider Object part that will be set with name/model/type of
 *             the Odoo report
 */
public class ReportAdapter {

	private Session session;
	private Version serverVersion;
	private Object[] report;
	private String reportName;

	/**
	 * @
	 */
	private static final HashMap<String, Object[]> reportListCache = new HashMap<String, Object[]>();

	public ReportAdapter(Session session) throws XmlRpcException {
		super();
		this.session = session;
		this.serverVersion = session.getServerVersion();
		try {
			getReportList();
		} catch (OdooApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * Method listing the available report and their type Purpose is to use the
	 * list later to check the existence of the report and its type. Appropriate
	 * methods will be possible regarding the type
	 */
	private void getReportList() throws XmlRpcException, OdooApiException {
		reportListCache.clear();
		ObjectAdapter objectAd = this.session.getObjectAdapter("ir.actions.report.xml");
		FilterCollection filters = new FilterCollection();
		String[] report_tuple = new String[] { "report_name", "model", "name", "report_type" };
		RowCollection reports = objectAd.searchAndReadObject(filters, report_tuple);
		reports.forEach(report -> {
			Object[] repName = new Object[] { report.get("name"), report.get("model"), report.get("report_type") };
			reportListCache.put(report.get("report_name").toString(), repName);
		});
	}

	/**
	 * @param reportName
	 * @param ids
	 * @return
	 * @throws XmlRpcException
	 * @throws OdooApiException
	 */
	public byte[] getReportAsByte(String reportName, Object[] ids) throws XmlRpcException, OdooApiException {
		checkReportName(reportName);
		byte[] reportDatas = session.executeReportService(reportName, ids);
		return reportDatas;
	}

	/**
	 * 
	 * Method to prepare the report to be generated Make some usefull tests
	 * regarding the
	 * 
	 * @param reportName:
	 *            can be found in Technical > report > report
	 * @throws OdooApiException
	 * @throws XmlRpcException
	 */
	private void checkReportName(String reportName) throws OdooApiException, XmlRpcException {
		// TODO Auto-generated method stub
		// refresh
		getReportList();
		if (reportName == null) {
			throw new OdooApiException("Report Name is mandatory.  Please read the Odoo help.");
		}
		Object[] report = reportListCache.get(reportName);
		if (report == null) {
			throw new OdooApiException(
					"Your report don't seems to exist in the Odoo Database." + "Please check your configuration");
		}
		if (!Arrays.asList("qweb-pdf", "qweb-html").contains(report[2])) {
			throw new OdooApiException(
					"Your report type is obsolete. Only QWEB report are allowed." + "Please check your configuration");
		}

	}

	public void setReport(String reportName) throws OdooApiException, XmlRpcException {
		checkReportName(reportName);
		Object[] report = reportListCache.get(reportName);
		this.report = report;
		this.reportName = reportName;
	}

	public String getReportType() {
		return this.report[2].toString();
	}

	public String PrintReportToFileName(Object[] ids) throws IOException, XmlRpcException, OdooApiException {

		File tmp_file = File.createTempFile("odoo-" + report[1].toString() + "-", getReportType().replace("qweb-", "."),
				null);

		byte[] report_bytes = getReportAsByte(reportName, ids);
		FileOutputStream report_stream = new FileOutputStream(tmp_file);
		try {
			report_stream.write(report_bytes);
		} finally {
			report_stream.close();
		}

		return tmp_file.getAbsolutePath().toString();
	}
}
