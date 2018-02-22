package com.odoojava.api;

import com.odoojava.api.Response;
import com.odoojava.api.Session;
import com.odoojava.api.OdooCommand;
import java.util.Map;
import org.apache.xmlrpc.XmlRpcException;
import static org.assertj.core.api.Assertions.fail;
import org.assertj.core.api.SoftAssertions;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
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

    /**
     * Test of searchObject method, of class OdooCommand.
     */
    @Test
    public void testSearchObject_String_ObjectArr() throws Exception {
        System.out.println("searchObject");
        String objectName = "";
        Object[] filter = null;
        OdooCommand instance = null;
        Object[] expResult = null;
        Object[] result = null;
        Response response = instance.searchObject(objectName, filter);

        if (response.isSuccessful()) {
            result = response.getResponseObjectAsArray();
        }
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of searchObject method, of class OdooCommand.
     */
    @Test
    public void testSearchObject_6args() throws Exception {
        System.out.println("searchObject");
        String objectName = "";
        Object[] filter = null;
        int offset = 0;
        int limit = 0;
        String order = "";
        boolean count = false;
        OdooCommand instance = null;
        Object expResult = null;
        Object result = null;
        Response response = instance.searchObject(objectName, filter, offset, limit, order, count);

        if (response.isSuccessful()) {
            result = response.getResponseObject();
        }
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getFields method, of class OdooCommand.
     */
    @Test
    public void testGetFields() throws Exception {
        System.out.println("getFields");
        String objectName = "";
        String[] filterFields = null;
        OdooCommand instance = null;
        Map<String, Object> expResult = null;
        Map<String, Object> result = instance.getFields(objectName, filterFields);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of readObject method, of class OdooCommand.
     */
    @Test
    public void testReadObject() throws Exception {
        System.out.println("readObject");
        String objectName = "";
        Object[] ids = null;
        String[] fields = null;
        OdooCommand instance = null;
        Object[] expResult = null;
        Object[] result = instance.readObject(objectName, ids, fields);
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of writeObject method, of class OdooCommand.
     */
    @Test
    public void testWriteObject() throws Exception {
        System.out.println("writeObject");
        String objectName = "";
        int id = 0;
        Map<String, Object> valueList = null;
        OdooCommand instance = null;
        boolean expResult = false;
        boolean result = instance.writeObject(objectName, id, valueList);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of importData method, of class OdooCommand.
     */
    @Test
    public void testImportData() throws Exception {
        System.out.println("importData");
        String objectName = "";
        String[] fieldList = null;
        Object[][] rows = null;
        OdooCommand instance = null;
        Object[] expResult = null;
        Object[] result = instance.importData(objectName, fieldList, rows);
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of load method, of class OdooCommand.
     */
    @Test
    public void testLoad() throws Exception {
        System.out.println("load");
        String objectName = "";
        String[] fieldList = null;
        Object[][] rows = null;
        OdooCommand instance = null;
        Map<String, Object> expResult = null;
        Map<String, Object> result = instance.load(objectName, fieldList, rows);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of nameGet method, of class OdooCommand.
     */
    @Test
    public void testNameGet() throws Exception {
        System.out.println("nameGet");
        String objectName = "";
        Object[] ids = null;
        OdooCommand instance = null;
        Object[] expResult = null;
        Object[] result = instance.nameGet(objectName, ids);
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of unlinkObject method, of class OdooCommand.
     */
    @Test
    public void testUnlinkObject() throws Exception {
        System.out.println("unlinkObject");
        String objectName = "";
        Object[] ids = null;
        OdooCommand instance = null;
        boolean expResult = false;
        boolean result = instance.unlinkObject(objectName, ids);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of createObject method, of class OdooCommand.
     */
    @Test
    public void testCreateObject() throws Exception {
        System.out.println("createObject");
        String objectName = "";
        Map<String, Object> values = null;
        OdooCommand instance = null;
        Object expResult = null;
        Object result = instance.createObject(objectName, values);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of callObjectFunction method, of class OdooCommand.
     */
    @Test
    public void testCallObjectFunction() {
        System.out.println("callObjectFunction");
        String objectName = "";
        String functionName = "";
        Object[] parameters = null;
        OdooCommand instance = null;
        Response expResult = null;
        Response result = instance.callObjectFunction(objectName, functionName, parameters);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of executeWorkflow method, of class OdooCommand.
     */
    @Test
    public void testExecuteWorkflow() throws Exception {
        System.out.println("executeWorkflow");
        String objectName = "";
        String signal = "";
        int objectID = 0;
        OdooCommand instance = null;
        instance.executeWorkflow(objectName, signal, objectID);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
}
