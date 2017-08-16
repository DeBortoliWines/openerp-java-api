package com.odoojava.api;

import com.odoojava.api.Version;
import com.odoojava.api.OdooXmlRpcProxy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.net.InetSocketAddress;
import java.net.Proxy;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import org.apache.xmlrpc.client.XmlRpcSun15HttpTransport;
import org.apache.xmlrpc.client.XmlRpcSun15HttpTransportFactory;
import org.apache.xmlrpc.client.XmlRpcTransportFactory;
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.StringBody;
import org.mockserver.socket.PortFactory;
import org.mockserver.socket.SSLFactory;

import com.odoojava.api.OdooXmlRpcProxy.RPCProtocol;
import com.odoojava.api.OdooXmlRpcProxy.RPCServices;

public class OdooXmlRpcProxyTest {

	private static final String MOCK_HTTPS_PROXY_PORT = "443";
	private static final String MOCK_HTTPS_PROXY_HOST = "mock_proxy_https";
	private static final String MOCK_HTTP_PROXY_PORT = "80";
	private static final String MOCK_HTTP_PROXY_HOST = "mock_proxy_http";
	private String httpProxyHost;
	private String httpProxyPort;
	private String httpsProxyHost;
	private String httpsProxyPort;
	private String host;
	private int port;
	private RPCServices service;

	@Before
	public void setup() {
		host = "localhost";
		port = PortFactory.findFreePort();
		service = RPCServices.RPC_COMMON;
	}

	private void restoreProperties() {
		// Restore previous values
		setHttpProperties(httpProxyHost, httpProxyPort);
		setHttpsProperties(httpsProxyHost, httpsProxyPort);
	}

	private void setHttpsProperties(String httpsProxyHost, String httpsProxyPort) {
		if (httpsProxyHost != null) {
			System.setProperty("https.proxyHost", httpsProxyHost);
		} else {
			System.clearProperty("https.proxyHost");
		}
		if (httpsProxyPort != null) {
			System.setProperty("https.proxyPort", httpsProxyPort);
		} else {
			System.clearProperty("https.proxyPort");
		}
	}

	private void setHttpProperties(String httpProxyHost, String httpProxyPort) {
		if (httpProxyHost != null) {
			System.setProperty("http.proxyHost", httpProxyHost);
		} else {
			System.clearProperty("http.proxyHost");
		}
		if (httpProxyPort != null) {
			System.setProperty("http.proxyPort", httpProxyPort);
		} else {
			System.clearProperty("http.proxyPort");
		}
	}

	private void saveAndClearProperties() {
		httpProxyHost = System.getProperty("http.proxyHost");
		httpProxyPort = System.getProperty("http.proxyPort");
		httpsProxyHost = System.getProperty("https.proxyHost");
		httpsProxyPort = System.getProperty("https.proxyPort");
		System.clearProperty("http.proxyHost");
		System.clearProperty("http.proxyPort");
		System.clearProperty("https.proxyHost");
		System.clearProperty("https.proxyPort");
	}

	@Test
	public void should_use_no_proxy_if_none_available() throws Exception {
		saveAndClearProperties();
		try {
			// Use SoftAssertions instead of direct assertThat methods
			// to collect all failing assertions in one go
			SoftAssertions softAssertions = new SoftAssertions();

			OdooXmlRpcProxy noProxy = new OdooXmlRpcProxy(RPCProtocol.RPC_HTTPS, host, port, service);
			XmlRpcTransportFactory factory = noProxy.getTransportFactory();
			if (factory != null && factory instanceof XmlRpcSun15HttpTransportFactory) {
				XmlRpcSun15HttpTransportFactory xmlRpcSun15HttpTransportFactory = (XmlRpcSun15HttpTransportFactory) factory;
				XmlRpcSun15HttpTransport transport = (XmlRpcSun15HttpTransport) xmlRpcSun15HttpTransportFactory
						.getTransport();
				softAssertions.assertThat(transport.getProxy()).as("No proxy").isNull();
			}

			noProxy = new OdooXmlRpcProxy(RPCProtocol.RPC_HTTP, host, port, service);
			factory = noProxy.getTransportFactory();
			if (factory != null && factory instanceof XmlRpcSun15HttpTransportFactory) {
				XmlRpcSun15HttpTransportFactory xmlRpcSun15HttpTransportFactory = (XmlRpcSun15HttpTransportFactory) factory;
				XmlRpcSun15HttpTransport transport = (XmlRpcSun15HttpTransport) xmlRpcSun15HttpTransportFactory
						.getTransport();
				softAssertions.assertThat(transport.getProxy()).as("No proxy").isNull();
			}

			// Don't forget to call SoftAssertions global verification !
			softAssertions.assertAll();
		} finally {
			restoreProperties();
		}
	}

	@Test
	public void should_use_https_proxy_for_https_when_available_http_proxy_otherwise() throws Exception {
		saveAndClearProperties();
		try {
			// Use SoftAssertions instead of direct assertThat methods
			// to collect all failing assertions in one go
			SoftAssertions softAssertions = new SoftAssertions();

			setHttpProperties(MOCK_HTTP_PROXY_HOST, MOCK_HTTP_PROXY_PORT);

			OdooXmlRpcProxy usingHttpProxy = new OdooXmlRpcProxy(RPCProtocol.RPC_HTTPS, host, port, service);
			XmlRpcTransportFactory factory = usingHttpProxy.getTransportFactory();
			if (factory != null && factory instanceof XmlRpcSun15HttpTransportFactory) {
				XmlRpcSun15HttpTransportFactory xmlRpcSun15HttpTransportFactory = (XmlRpcSun15HttpTransportFactory) factory;
				XmlRpcSun15HttpTransport transport = (XmlRpcSun15HttpTransport) xmlRpcSun15HttpTransportFactory
						.getTransport();
				Proxy proxy = new Proxy(Proxy.Type.HTTP,
						new InetSocketAddress(MOCK_HTTP_PROXY_HOST, Integer.parseInt(MOCK_HTTP_PROXY_PORT, 10)));
				softAssertions.assertThat(transport.getProxy()).as("Mock proxy for http").isEqualTo(proxy);
			}

			setHttpsProperties(MOCK_HTTPS_PROXY_HOST, MOCK_HTTPS_PROXY_PORT);

			OdooXmlRpcProxy usingHttpsProxy = new OdooXmlRpcProxy(RPCProtocol.RPC_HTTPS, host, port, service);
			factory = usingHttpsProxy.getTransportFactory();
			if (factory != null && factory instanceof XmlRpcSun15HttpTransportFactory) {
				XmlRpcSun15HttpTransportFactory xmlRpcSun15HttpTransportFactory = (XmlRpcSun15HttpTransportFactory) factory;
				XmlRpcSun15HttpTransport transport = (XmlRpcSun15HttpTransport) xmlRpcSun15HttpTransportFactory
						.getTransport();
				Proxy proxy = new Proxy(Proxy.Type.HTTP,
						new InetSocketAddress(MOCK_HTTPS_PROXY_HOST, Integer.parseInt(MOCK_HTTPS_PROXY_PORT, 10)));
				softAssertions.assertThat(transport.getProxy()).as("Mock proxy for https").isEqualTo(proxy);
			}

			// Don't forget to call SoftAssertions global verification !
			softAssertions.assertAll();
		} finally {
			restoreProperties();
		}
	}

	@Test
	public void should_use_http_proxy_for_http_when_available_nothing_else() throws Exception {
		saveAndClearProperties();
		try {
			// Use SoftAssertions instead of direct assertThat methods
			// to collect all failing assertions in one go
			SoftAssertions softAssertions = new SoftAssertions();

			setHttpsProperties(MOCK_HTTPS_PROXY_HOST, MOCK_HTTPS_PROXY_PORT);

			OdooXmlRpcProxy usingHttpsProxy = new OdooXmlRpcProxy(RPCProtocol.RPC_HTTP, host, port, service);
			XmlRpcTransportFactory factory = usingHttpsProxy.getTransportFactory();
			if (factory != null && factory instanceof XmlRpcSun15HttpTransportFactory) {
				XmlRpcSun15HttpTransportFactory xmlRpcSun15HttpTransportFactory = (XmlRpcSun15HttpTransportFactory) factory;
				XmlRpcSun15HttpTransport transport = (XmlRpcSun15HttpTransport) xmlRpcSun15HttpTransportFactory
						.getTransport();
				softAssertions.assertThat(transport.getProxy()).as("No proxy").isNull();
			}

			setHttpProperties(MOCK_HTTP_PROXY_HOST, MOCK_HTTP_PROXY_PORT);

			OdooXmlRpcProxy usingHttpProxy = new OdooXmlRpcProxy(RPCProtocol.RPC_HTTP, host, port, service);
			factory = usingHttpProxy.getTransportFactory();
			if (factory != null && factory instanceof XmlRpcSun15HttpTransportFactory) {
				XmlRpcSun15HttpTransportFactory xmlRpcSun15HttpTransportFactory = (XmlRpcSun15HttpTransportFactory) factory;
				XmlRpcSun15HttpTransport transport = (XmlRpcSun15HttpTransport) xmlRpcSun15HttpTransportFactory
						.getTransport();
				Proxy proxy = new Proxy(Proxy.Type.HTTP,
						new InetSocketAddress(MOCK_HTTP_PROXY_HOST, Integer.parseInt(MOCK_HTTP_PROXY_PORT, 10)));
				softAssertions.assertThat(transport.getProxy()).as("Mock proxy for http").isEqualTo(proxy);
			}

			// Don't forget to call SoftAssertions global verification !
			softAssertions.assertAll();
		} finally {
			restoreProperties();
		}
	}

	@Test
	public void should_return_server_version() throws Exception {
		// Make sure SSL works by adding MockServer CA certificate to context
		SSLSocketFactory previousFactory = HttpsURLConnection.getDefaultSSLSocketFactory();
		HttpsURLConnection.setDefaultSSLSocketFactory(SSLFactory.getInstance().sslContext().getSocketFactory());
		ClientAndServer mockServer = ClientAndServer.startClientAndServer(port);
		try {
			// Given: the server expects a request of its version
			mockServer
					.when(request().withMethod("POST").withPath("/xmlrpc/2/db")
							.withBody(new StringBody(
									"<?xml version=\"1.0\" encoding=\"UTF-8\"?><methodCall><methodName>server_version</methodName><params/></methodCall>")))
					.respond(response().withStatusCode(200).withBody(
							"<?xml version='1.0'?>\n<methodResponse>\n<params>\n<param>\n<value><string>9.0e</string></value>\n</param>\n</params>\n</methodResponse>\n"));

			// When: Server version is requested
			Version version = OdooXmlRpcProxy.getServerVersion(RPCProtocol.RPC_HTTPS, host, port);

			// Then: the server version is returned
			assertThat(version).as("Server version").isNotNull().hasToString("9.0e");
		} finally {
			mockServer.stop();
			HttpsURLConnection.setDefaultSSLSocketFactory(previousFactory);
		}

	}
}
