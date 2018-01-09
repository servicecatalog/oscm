/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.app.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Properties;

import org.junit.Test;

import org.oscm.app.domain.Operation;
import org.oscm.app.v2_0.intf.APPlatformController;
import org.oscm.app.v2_0.service.OperationServiceBean;
import org.oscm.operation.data.OperationParameter;

/**
 * Unit tests for {@link Operation}.
 * 
 * @author stavreva
 */
public class OperationTest {

    private Operation operation = new Operation();
    private static OperationServiceBean opService = new OperationServiceBean();
    private static String USER_ID = "userId";
    private static String OP_ID = "opId";

    @Test
    public void convertPropertiesToXML_empty() {
        // given
        Properties prop = new Properties();

        // when
        String params = operation.convertPropertiesToXML(prop);

        // then
        assertFalse(params.contains("<entry key="));
    }

    @Test
    public void convertPropertiesToXML() {
        // given
        Properties prop = opService.createProperties(USER_ID, OP_ID, Arrays
                .asList(opParam("p1", "�a1\""), opParam("p2", "�a2<"),
                        opParam("p3", "\r\nabc")));

        // when
        String params = operation.convertPropertiesToXML(prop);

        // then
        assertTrue(params.contains("<entry key=\"p1\">"));
        assertTrue(params.contains("<entry key=\"p2\">"));
    }

    @Test
    public void convertXMLToProperties() {
        // given
        Properties propIn = opService.createProperties(USER_ID, OP_ID, Arrays
                .asList(opParam("p1", "�a1\""), opParam("p2", "�a2<"),
                        opParam("p3", "\r\nabc")));

        // when
        String params = operation.convertPropertiesToXML(propIn);

        // then
        Properties propOut = operation.convertXMLToProperties(params);
        assertEquals(propIn, propOut);
    }

    @Test
    public void setParametersFromProperties() {
        // given
        Properties propIn = opService.createProperties(USER_ID, OP_ID, Arrays
                .asList(opParam("p1", "�a1\""), opParam("p2", "�a2<"),
                        opParam("p3", "\r\nabc")));

        // when
        operation = new Operation();
        operation.setFromProperties(propIn);

        // then
        Properties propOut = operation.getParametersAsProperties();
        assertFalse(propOut.containsKey(APPlatformController.KEY_OPERATION_ID));
    }

    @Test
    public void setParametersFromProperties_noParams() {
        // given
        Properties propIn = new Properties();
        propIn.put(APPlatformController.KEY_OPERATION_ID, OP_ID);
        propIn.put(APPlatformController.KEY_OPERATION_USER_ID, USER_ID);

        // when
        operation = new Operation();
        operation.setFromProperties(propIn);

        // then
        Properties propOut = operation.getParametersAsProperties();
        assertTrue(propOut.isEmpty());
    }

    OperationParameter opParam(String name, String value) {
        OperationParameter opParam = new OperationParameter();
        opParam.setName(name);
        opParam.setValue(value);
        return opParam;
    }
}
