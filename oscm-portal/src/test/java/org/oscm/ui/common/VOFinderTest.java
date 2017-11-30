/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: pock                                        
 *                                                                              
 *  Creation Date: May 21, 2010                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.ui.common;

import static org.oscm.test.Numbers.L0;
import static org.oscm.test.Numbers.L100;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.faces.convert.ConverterException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.oscm.internal.types.enumtypes.ParameterType;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.vo.VOEventDefinition;
import org.oscm.internal.vo.VOParameter;
import org.oscm.internal.vo.VOParameterDefinition;
import org.oscm.internal.vo.VOParameterOption;
import org.oscm.internal.vo.VOPricedEvent;
import org.oscm.internal.vo.VOPricedOption;
import org.oscm.internal.vo.VOPricedParameter;

public class VOFinderTest {

    private List<VOParameterDefinition> parameterDefinitionList = new ArrayList<VOParameterDefinition>();
    private VOParameterDefinition parameterDefinition = new VOParameterDefinition(
            ParameterType.SERVICE_PARAMETER, "URL", "URL Description",
            ParameterValueType.STRING, "", null, null, false, true, null);
    private VOParameterDefinition parameterDefinitionWithOption;

    @Before
    public void setup() {
        for (int i = 0; i < 20; i++) {
            VOParameterDefinition paramDef = new VOParameterDefinition(
                    ParameterType.PLATFORM_PARAMETER, "USER" + i, "Description"
                            + i, ParameterValueType.LONG, "0", L0, L100, false,
                    true, null);
            paramDef.setKey(i + 1);
            parameterDefinitionList.add(paramDef);
        }
        parameterDefinitionList.add(10, parameterDefinition);

        String paramDefId = "SIZE";
        List<VOParameterOption> parameterOptionList = new ArrayList<VOParameterOption>();
        for (int i = 0; i < 6; i++) {
            VOParameterOption option = new VOParameterOption(paramDefId + i,
                    paramDefId + " description " + i, paramDefId);
            option.setKey(i + 1);
            parameterOptionList.add(option);
        }
        parameterDefinitionWithOption = new VOParameterDefinition(
                ParameterType.SERVICE_PARAMETER, paramDefId,
                "Size Description", ParameterValueType.ENUMERATION, "", null,
                null, false, true, parameterOptionList);
        parameterDefinitionList.add(15, parameterDefinitionWithOption);
    }

    @Test
    public void testFindByKey() throws ConverterException {
        Assert.assertSame(
                parameterDefinition,
                VOFinder.findByKey(parameterDefinitionList,
                        parameterDefinition.getKey()));
    }

    @Test
    public void testFindByKeyNull() throws ConverterException {
        Assert.assertNull(VOFinder.findByKey(null, 2000));
    }

    @Test
    public void testFindByKeyNotFound() throws ConverterException {
        Assert.assertNull(VOFinder.findByKey(parameterDefinitionList, 2000));
    }

    @Test
    public void testFindPricedParameter() {
        VOParameter parameter = null;
        VOPricedParameter pricedParameter = null;
        List<VOPricedParameter> list = new ArrayList<VOPricedParameter>();
        int key = 1;
        for (VOParameterDefinition paramDef : parameterDefinitionList) {
            VOParameter param = new VOParameter(paramDef);
            param.setKey(key++);
            VOPricedParameter pricedParam = new VOPricedParameter(paramDef);
            pricedParam.setParameterKey(param.getKey());
            if (paramDef == parameterDefinition) {
                parameter = param;
                pricedParameter = pricedParam;
            }
            list.add(pricedParam);
        }
        Assert.assertSame(pricedParameter,
                VOFinder.findPricedParameter(list, parameter));

        Assert.assertNull(VOFinder.findPricedParameter(list, new VOParameter(
                parameterDefinition)));
        Assert.assertNull(VOFinder.findPricedParameter(null, new VOParameter(
                parameterDefinition)));
        Assert.assertNull(VOFinder.findPricedParameter(list, null));
    }

    @Test
    public void testFindPricedOption() {
        List<VOPricedOption> list = new ArrayList<VOPricedOption>();
        parameterDefinitionWithOption.getParameterOptions().get(3);

        for (VOParameterOption option : parameterDefinitionWithOption
                .getParameterOptions()) {
            VOPricedOption pricedOption = new VOPricedOption();
            pricedOption.setParameterOptionKey(option.getKey());
            list.add(pricedOption);
        }
        Assert.assertSame(list.get(3), VOFinder.findPricedOption(list,
                parameterDefinitionWithOption.getParameterOptions().get(3)));

        Assert.assertNull(VOFinder.findPricedOption(list,
                new VOParameterOption("", "", "")));
        Assert.assertNull(VOFinder.findPricedOption(null,
                parameterDefinitionWithOption.getParameterOptions().get(0)));
        Assert.assertNull(VOFinder.findPricedOption(list, null));
    }

    @Test
    public void testFindPricedEvent() {
        List<VOPricedEvent> list = new ArrayList<VOPricedEvent>();
        for (int i = 0; i < 20; i++) {
            VOEventDefinition event = new VOEventDefinition();
            event.setKey(i + 1);
            event.setEventId("ID" + i);
            VOPricedEvent pricedEvent = new VOPricedEvent(event);
            pricedEvent.setEventPrice(BigDecimal.valueOf(i));
            list.add(pricedEvent);
        }
        VOEventDefinition event = new VOEventDefinition();
        event.setKey(100L);
        event.setEventId("ID");
        VOPricedEvent pricedEvent = new VOPricedEvent(event);
        pricedEvent.setEventPrice(BigDecimal.valueOf(100));
        list.add(11, pricedEvent);

        Assert.assertSame(pricedEvent, VOFinder.findPricedEvent(list, event));

        Assert.assertNull(VOFinder.findPricedEvent(list,
                new VOEventDefinition()));
        Assert.assertNull(VOFinder.findPricedEvent(null, event));
        Assert.assertNull(VOFinder.findPricedEvent(list, null));
    }
}
