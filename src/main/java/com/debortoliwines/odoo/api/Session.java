/*
 *   Copyright 2011-2014 De Bortoli Wines Pty Limited (Australia)
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
package com.debortoliwines.odoo.api;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.xmlrpc.XmlRpcException;

import com.debortoliwines.odoo.api.OdooXmlRpcProxy.RPCProtocol;
import com.debortoliwines.odoo.api.OdooXmlRpcProxy.RPCServices;
import java.util.Arrays;

/**
 * *
 * Manages an Odoo session by holding context and initiating all calls to the
 * server.
 *
 * @author Pieter van der Merwe
 *
 */
public class Session {

    private static final String LINE_SEPARATOR_SYSTEM_PROPERTY = "line.separator";
    private static final String LINE_SEPARATOR = System.getProperty(LINE_SEPARATOR_SYSTEM_PROPERTY);
    private String host;
    private int port;
    private String databaseName;
    private String userName;
    private String password;
    private int userID;
    private Context context = new Context();
    private static boolean connecting = false;
    private RPCProtocol protocol;

    private OdooXmlRpcProxy objectClient;
    private Version serverVersion;

    /**
     * *
     * Session constructor
     *
     * @param protocol XML-RPC protocol to use. ex http/https.
     * @param host Host name or IP address where the Odoo server is hosted
     * @param port XML-RPC port number to connect to. Typically 8069.
     * @param databaseName Database name to connect to
     * @param userName Username to log into the Odoo server
     * @param password Password to log into the Odoo server
     */
    public Session(RPCProtocol protocol, String host, int port, String databaseName, String userName, String password) {
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        this.databaseName = databaseName;
        this.userName = userName;
        this.password = password;
        this.objectClient = new OdooXmlRpcProxy(protocol, host, port, RPCServices.RPC_OBJECT);
    }

    /**
     * *
     * Session constructor. Uses default http protocol to connect.
     *
     * @param host Host name or IP address where the Odoo server is hosted
     * @param port XML-RPC port number to connect to. Typically 8069.
     * @param databaseName Database name to connect to
     * @param userName Username to log into the Odoo server
     * @param password Password to log into the Odoo server
     */
    public Session(String host, int port, String databaseName, String userName, String password) {
        this(RPCProtocol.RPC_HTTP, host, port, databaseName, userName, password);
    }

    /**
     * Returns an initialized ObjectAdapter object for ease of reference. A
     * ObjectAdapter object does type conversions and error checking before
     * making a call to the server
     *
     * @return
     */
    public ObjectAdapter getObjectAdapter(String objectName) throws XmlRpcException, OdooApiException {
        return new ObjectAdapter(this, objectName);
    }

    /**
     * *
     * Starts a session on the Odoo server and saves the UserID for use in later
     * calls
     *
     * @throws Exception upon failure to connect
     */
    public void startSession() throws Exception {

        checkDatabasePresenceSafe();

        // Synchronize all threads to login.  If you login with the same user at the same time you get concurrency
        // errors in the Odoo server (for example by running a multi threaded
        // ETL process like Kettle).
        Session.startConnecting();
        try {
            authenticate();
        } finally {
            Session.connecting = false;
        }
        getRemoteContext();

    }

    void getRemoteContext() throws XmlRpcException {
        this.context.clear();
        @SuppressWarnings("unchecked")
        HashMap<String, Object> odooContext = (HashMap<String, Object>) this.executeCommand("res.users",
                "context_get", new Object[]{});
        this.context.putAll(odooContext);

        // Standard behavior is web/gui clients.
        this.context.setActiveTest(true);
    }

    int authenticate() throws XmlRpcException, Exception {
        OdooXmlRpcProxy commonClient = new OdooXmlRpcProxy(protocol, host, port, RPCServices.RPC_COMMON);

        Object id = commonClient.execute("login", new Object[]{databaseName, userName, password});

        if (id instanceof Integer) {
            userID = (Integer) id;
        } else {
            throw new Exception("Incorrect username and/or password.  Login Failed.");
        }

        return userID;
    }

    void checkDatabasePresenceSafe() {
        // 21/07/2012 - Database listing may not be enabled (--no-database-list
        // or list_db=false).
        // Only provides additional information in any case.
        try {

            checkDatabasePresence();
        } catch (Exception e) {
            // e.printStackTrace();
        }
    }

    void checkDatabasePresence() throws XmlRpcException {
        ArrayList<String> dbList = getDatabaseList(protocol, host, port);
        if (!dbList.contains(databaseName)) {
            StringBuilder messageBuilder = new StringBuilder("Error while connecting to Odoo.  Database [")
                    .append(databaseName).append("]  was not found in the following list: ").append(LINE_SEPARATOR)
                    .append(LINE_SEPARATOR).append(String.join(LINE_SEPARATOR, dbList)).append(LINE_SEPARATOR);

            throw new IllegalStateException(messageBuilder.toString());
        }
    }

    private synchronized static void startConnecting() {
        while (Session.connecting) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Session.connecting = true;
    }

    /**
     * *
     * Get a list of databases available on a specific host and port with the
     * http protocol.
     *
     * @param host Host name or IP address where the Odoo server is hosted
     * @param port XML-RPC port number to connect to
     * @return A list of databases available for the Odoo instance
     * @throws XmlRpcException
     */
    public static ArrayList<String> getDatabaseList(String host, int port) throws XmlRpcException {
        return getDatabaseList(RPCProtocol.RPC_HTTP, host, port);
    }

    /**
     * *
     * Get a list of databases available on a specific host and port
     *
     * @param protocol Protocol to use when connecting to the RPC service ex.
     * http/https
     * @param host Host name or IP address where the Odoo server is hosted
     * @param port XML-RPC port number to connect to
     * @return A list of databases available for the Odoo instance
     * @throws XmlRpcException
     */
    public static ArrayList<String> getDatabaseList(RPCProtocol protocol, String host, int port)
            throws XmlRpcException {
        OdooXmlRpcProxy client = new OdooXmlRpcProxy(protocol, host, port, RPCServices.RPC_DATABASE);

        // Retrieve databases
        Object[] result = (Object[]) client.execute("list", new Object[]{});

        ArrayList<String> finalResults = new ArrayList<String>();
        for (Object res : result) {
            finalResults.add((String) res);
        }

        return finalResults;
    }

    /**
     * Executes any command on the server linked to the /xmlrpc/object service.
     * All parameters are prepended by: "databaseName,userID,password" This
     * method execute the command without the context parameter Its purpose is
     * to be used by Odoo version prior to v10 or for v10 methods that mustn't
     * use the context
     *
     * @param objectName Object or model name to execute the command on
     * @param commandName Command name to execute
     * @param parameters List of parameters for the command. For easy of use,
     * consider the OdooCommand object or ObjectAdapter
     * @return The result of the call
     * @throws XmlRpcException
     */
    public Object executeCommand(final String objectName, final String commandName, final Object[] parameters) throws XmlRpcException {
        Object[] connectionParams = new Object[]{databaseName, userID, password, objectName, commandName};

        // Combine the connection parameters and command parameters
        Object[] params = new Object[connectionParams.length
                + (parameters == null ? 0 : parameters.length)];
        System.arraycopy(connectionParams, 0, params, 0, connectionParams.length);

        if (parameters != null && parameters.length > 0) {
            System.arraycopy(parameters, 0, params, connectionParams.length, parameters.length);
        }
        return objectClient.execute("execute", params);
    }

    /**
     * Executes any command on the server linked to the /xmlrpc/object service.
     * parameters and Context are prepended .The context MUST NOT have been 
     * already passed in the parameters.
     *
     * @param objectName Object or model name to execute the command on
     * @param commandName Command name to execute
     * @param parameters List of parameters for the command. For easy of use,
     * consider the OdooCommand object or ObjectAdapter
     * @return The result of the call
     * @throws XmlRpcException
     */
    public Object executeCommandWithContext(final String objectName, final String commandName, final Object[] parameters) throws XmlRpcException {
        Object[] connectionParams = new Object[]{databaseName, userID, password, objectName, commandName};

        // Combine the parameters with the context
        Object[] params = new Object[1 + (parameters == null ? 0 : parameters.length)];
        if (parameters != null && parameters.length > 0) {
            System.arraycopy(parameters, 0, params, 0, parameters.length);
        }
        System.arraycopy(new Object[]{getContext()}, 0, params, parameters.length, 1);
        return executeCommand(objectName, commandName, params);
    }

    /**
     * Executes a workflow by sending a signal to the workflow engine for a
     * specific object. This functions calls the 'exec_workflow' method on the
     * object All parameters are prepended by: "databaseName,userID,password"
     *
     * @param objectName Object or model name to send the signal for
     * @param signal Signal name to send, for example order_confirm
     * @param objectID Specific object ID to send the signal for
     * @throws XmlRpcException
     */
    public void executeWorkflow(final String objectName, final String signal, final int objectID) throws XmlRpcException {
        Object[] params = new Object[]{databaseName, userID, password, objectName, signal, objectID};

        objectClient.execute("exec_workflow", params);
    }

    /**
     * Returns the Odoo server version for this session
     *
     * @return
     * @throws XmlRpcException
     */
    public Version getServerVersion() throws XmlRpcException {
        if (serverVersion == null) {
            // Cache server version
            serverVersion = OdooXmlRpcProxy.getServerVersion(protocol, host, port);
        }
        return serverVersion;
    }

    /**
     * Retrieves the context object for the session to set properties on
     *
     * @return
     */
    public Context getContext() {
        return context;
    }
}
