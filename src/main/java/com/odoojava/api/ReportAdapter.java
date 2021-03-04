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
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
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

import javax.xml.bind.DatatypeConverter;

import org.apache.xmlrpc.XmlRpcException;

import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.odoojava.api.Field.FieldType;
import com.odoojava.api.helpers.FilterHelper;

/**
 * 
 * Main class for managing reports with the server.
 * 
 * @author Florent THOMAS
 * @param: reportListCache . Consider Object part that will be set with
 *                         name/model/type of the Odoo report
 */
public class ReportAdapter {

	private Session session;
	private Version serverVersion;
	private Object[] report;
	private String reportName;
	private String reportModel;
	private String reportMethod;
	private ObjectAdapter objectReportAdapter;

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
	 * Method listing the available report and their type Purpose is to use the list
	 * later to check the existence of the report and its type. Appropriate methods
	 * will be possible regarding the type
	 */
	private void getReportList() throws XmlRpcException, OdooApiException {
		reportListCache.clear();
		objectReportAdapter = this.session.getObjectAdapter(this.getReportModel());
		FilterCollection filters = new FilterCollection();
		String[] report_tuple = new String[] { "id", "report_name", "model", "name", "report_type" };
		RowCollection reports = objectReportAdapter.searchAndReadObject(filters, report_tuple);
		reports.forEach(report -> {
			Object[] repName = new Object[] { report.get("name"), report.get("model"), report.get("report_type"),
					report.get("id") };
			reportListCache.put(report.get("report_name").toString(), repName);
		});
	}

	/**
	 * This method is fully inspire by
	 * https://github.com/OCA/odoorpc/blob/master/odoorpc/report.py#L113 from
	 * https://github.com/sebalix
	 * 
	 * @return string representing the reportModel regarding the version
	 */
	public String getReportModel() {
		reportModel = "ir.actions.report";
		if (this.serverVersion.getMajor() < 11) {
			reportModel = "ir.actions.report.xml";
		}
		return reportModel;
	}

	public String getReportMethod() {
		/**
		 * Default value of the method for v11
		 */
		reportMethod = "render"; 
		if (this.serverVersion.getMajor() < 11) {
			reportMethod = "render_report";
		}
		return reportMethod;
	}

	/**
	 * @param reportName
	 * @param ids
	 * @return
	 * @throws Throwable
	 */
	public byte[] getPDFReportAsByte(String reportName, Object[] ids) throws Throwable {
		checkReportName(reportName);
		byte[] reportDatas;
		if (this.serverVersion.getMajor() < 11) {
			reportDatas = session.executeReportService(reportName, this.getReportMethod(), ids);
		} else {
			ArrayList<Object> reportParams = new ArrayList<Object>();
			reportParams.add( getReportID());
			reportParams.add( ids);
			Object[] result = session.call_report_jsonrpc(getReportModel(), getReportMethod(), reportParams);

			String pdf_string= (String) result[0]; 
			reportDatas = pdf_string.getBytes(StandardCharsets.ISO_8859_1);
		}
		return reportDatas;
	}

	/**
	 * 
	 * Method to prepare the report to be generated Make some usefull tests
	 * regarding the
	 * 
	 * @param reportName: can be found in Technical > report > report
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

	public Integer getReportID() {
		return Integer.valueOf(this.report[3].toString());
	}

	public String PrintReportToFileName(Object[] ids) throws IOException, XmlRpcException, OdooApiException {

		File tmp_file = File.createTempFile("odoo-" + report[1].toString() + "-", getReportType().replace("qweb-", "."),
				null);

		byte[] report_bytes;
		FileOutputStream report_stream = new FileOutputStream(tmp_file);
		try {
			report_bytes = getPDFReportAsByte(reportName, ids);

			
			report_stream.write(report_bytes);
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			report_stream.close();
		}

		return tmp_file.getAbsolutePath().toString();
	}

}
