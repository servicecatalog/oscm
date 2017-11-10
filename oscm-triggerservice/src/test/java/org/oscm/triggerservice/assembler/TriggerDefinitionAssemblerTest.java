/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 08.11.2010                                                      
 *                                                                              
 *  Completion Time: 15.11.2010                                             
 *                                                                              
 *******************************************************************************/

package org.oscm.triggerservice.assembler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.oscm.domobjects.TriggerDefinition;
import org.oscm.internal.types.enumtypes.TriggerTargetType;
import org.oscm.internal.types.enumtypes.TriggerType;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOTriggerDefinition;

/**
 * Unit tests for the trigger definition assembler.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class TriggerDefinitionAssemblerTest {

    private VOTriggerDefinition voTriggerDefinition;
    private TriggerDefinition triggerDefinition;

    private static final String DISPLAY_NAME = "displayName";

    @Before
    public void setUp() {
        voTriggerDefinition = new VOTriggerDefinition();
        voTriggerDefinition.setKey(123);
        voTriggerDefinition.setSuspendProcess(false);
        voTriggerDefinition.setTarget("voTarget");
        voTriggerDefinition.setTargetType(TriggerTargetType.WEB_SERVICE);
        voTriggerDefinition.setType(TriggerType.ACTIVATE_SERVICE);
        voTriggerDefinition.setVersion(1);
        voTriggerDefinition.setName(DISPLAY_NAME);

        triggerDefinition = new TriggerDefinition();
        triggerDefinition.setKey(123);
        triggerDefinition.setTarget("doTarget");
        triggerDefinition.setTargetType(TriggerTargetType.WEB_SERVICE);
        triggerDefinition.setType(TriggerType.DEACTIVATE_SERVICE);
        triggerDefinition.setSuspendProcess(true);
        triggerDefinition.setName(DISPLAY_NAME);
    }

    @Test
    public void testConstructor() throws Exception {
        // only for coverage
        TriggerDefinitionAssembler result = new TriggerDefinitionAssembler();
        assertNotNull(result);
    }

    @Test
    public void testToTriggerDefinition() throws Exception {
        TriggerDefinition result = TriggerDefinitionAssembler
                .toTriggerDefinition(voTriggerDefinition);
        validateResult(result);
        assertEquals(0, result.getKey());
    }

    @Test(expected = ValidationException.class)
    public void testToTriggerDefinition_NoTargetType() throws Exception {
        voTriggerDefinition.setTargetType(null);
        TriggerDefinitionAssembler.toTriggerDefinition(voTriggerDefinition);
    }

    @Test(expected = ValidationException.class)
    public void testToTriggerDefinition_NoType() throws Exception {
        voTriggerDefinition.setType(null);
        TriggerDefinitionAssembler.toTriggerDefinition(voTriggerDefinition);
    }

    @Test(expected = ValidationException.class)
    public void testToTriggerDefinition_NoSuspendableType() throws Exception {
        voTriggerDefinition.setType(TriggerType.START_BILLING_RUN);
        voTriggerDefinition.setSuspendProcess(true);
        TriggerDefinitionAssembler.toTriggerDefinition(voTriggerDefinition);
    }

    @Test(expected = SaaSSystemException.class)
    public void testUpdateTriggerDefinition_NoMatchingKeys() throws Exception {
        voTriggerDefinition.setKey(122);
        TriggerDefinitionAssembler.updateTriggerDefinition(triggerDefinition,
                voTriggerDefinition);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testUpdateTriggerDefinition_ConcurringVersion()
            throws Exception {
        voTriggerDefinition.setVersion(triggerDefinition.getVersion() - 1);
        TriggerDefinitionAssembler.updateTriggerDefinition(triggerDefinition,
                voTriggerDefinition);
    }

    @Test
    public void testUpdateTriggerDefinition() throws Exception {
        TriggerDefinition result = TriggerDefinitionAssembler
                .updateTriggerDefinition(triggerDefinition, voTriggerDefinition);
        validateResult(result);
        assertEquals(123, result.getKey());
    }

    @Test
    public void testToVOTriggerDefinition_NullInput() throws Exception {
        VOTriggerDefinition result = TriggerDefinitionAssembler
                .toVOTriggerDefinition(null);
        assertNull(result);
    }

    @Test
    public void testToVOTriggerDefinition() throws Exception {
        VOTriggerDefinition result = TriggerDefinitionAssembler
                .toVOTriggerDefinition(triggerDefinition);
        assertNotNull(result);
        assertEquals(123, result.getKey());
        assertEquals(DISPLAY_NAME, result.getName());
        validateResult(result);
    }

    @Test
    public void toVOTriggerDefinition_HasTriggerProcesses() throws Exception {
        //when
        VOTriggerDefinition result = TriggerDefinitionAssembler
                .toVOTriggerDefinition(triggerDefinition, true);
        //then
        assertNotNull(result);
        assertEquals(123, result.getKey());
        assertEquals(DISPLAY_NAME, result.getName());
        assertEquals(Boolean.TRUE,
                Boolean.valueOf(result.isHasTriggerProcess()));
        validateResult(result);
    }

    @Test
    public void toVOTriggerDefinition_HasTriggerProcess_false()
            throws Exception {
       //when
        VOTriggerDefinition result = TriggerDefinitionAssembler
                .toVOTriggerDefinition(triggerDefinition, false);
       //then
        assertNotNull(result);
        assertEquals(123, result.getKey());
        assertEquals(DISPLAY_NAME, result.getName());
        assertEquals(Boolean.FALSE,
                Boolean.valueOf(result.isHasTriggerProcess()));
        validateResult(result);
    }

    @Test
    public void isOnlyNameChanged_False() throws Exception {
        VOTriggerDefinition voTriggerDefinition = new VOTriggerDefinition();
        voTriggerDefinition.setName("name1");
        voTriggerDefinition.setTarget("target");
        voTriggerDefinition.setTargetType(TriggerTargetType.WEB_SERVICE);
        voTriggerDefinition.setType(TriggerType.ACTIVATE_SERVICE);
        voTriggerDefinition.setSuspendProcess(true);

        TriggerDefinition triggerDefinition = new TriggerDefinition();
        triggerDefinition.setName("name2");
        triggerDefinition.setTarget("target");
        triggerDefinition.setTargetType(TriggerTargetType.WEB_SERVICE);
        triggerDefinition.setType(TriggerType.ACTIVATE_SERVICE);
        triggerDefinition.setSuspendProcess(false);

        boolean result = TriggerDefinitionAssembler.isOnlyNameChanged(
                voTriggerDefinition, triggerDefinition);
        assertFalse(result);
    }

    @Test
    public void isOnlyNameChanged_True() throws Exception {
        VOTriggerDefinition voTriggerDefinition = new VOTriggerDefinition();
        voTriggerDefinition.setName("name1");
        voTriggerDefinition.setTarget("target");
        voTriggerDefinition.setTargetType(TriggerTargetType.WEB_SERVICE);
        voTriggerDefinition.setType(TriggerType.ACTIVATE_SERVICE);

        TriggerDefinition triggerDefinition = new TriggerDefinition();
        triggerDefinition.setName("name2");
        triggerDefinition.setTarget("target");
        triggerDefinition.setTargetType(TriggerTargetType.WEB_SERVICE);
        triggerDefinition.setType(TriggerType.ACTIVATE_SERVICE);

        boolean result = TriggerDefinitionAssembler.isOnlyNameChanged(
                voTriggerDefinition, triggerDefinition);
        assertTrue(result);
    }

    @Test(expected = ValidationException.class)
    public void testToTriggerDefinitionNameMissing() throws Exception {
        voTriggerDefinition.setName(null);
        TriggerDefinition result = TriggerDefinitionAssembler
                .toTriggerDefinition(voTriggerDefinition);
        validateResult(result);
        Assert.fail("should fail because name is missing.");
    }

    // --------------------------------------------------------------------------------
    // internal helper methods

    /**
     * Verifies the domain object's attributes.
     * 
     * @param result
     */
    private void validateResult(TriggerDefinition result) {
        assertNotNull(result);
        assertEquals(Boolean.valueOf(false),
                Boolean.valueOf(result.isSuspendProcess()));
        assertEquals("voTarget", result.getTarget());
        assertEquals(TriggerTargetType.WEB_SERVICE, result.getTargetType());
        assertEquals(TriggerType.ACTIVATE_SERVICE, result.getType());
        assertEquals(0, result.getVersion());
        assertEquals(DISPLAY_NAME, result.getDataContainer().getName());
    }

    /**
     * Verifies the domain object's attributes.
     * 
     * @param result
     */
    private void validateResult(VOTriggerDefinition result) {
        assertNotNull(result);
        assertEquals(Boolean.valueOf(true),
                Boolean.valueOf(result.isSuspendProcess()));
        assertEquals("doTarget", result.getTarget());
        assertEquals(TriggerTargetType.WEB_SERVICE, result.getTargetType());
        assertEquals(TriggerType.DEACTIVATE_SERVICE, result.getType());
        assertEquals(0, result.getVersion());
        assertEquals(DISPLAY_NAME, result.getName());
    }
}
