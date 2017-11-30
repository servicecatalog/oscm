/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: cmin                                        
 *                                                                              
 *  Creation Date: 19.03.2014
 *                                                                              
 *  Completion Time: 19.03.2014 
 *
 *******************************************************************************/
package org.oscm.ui.converter;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import javax.faces.component.UIComponent;
import javax.faces.convert.ConverterException;

import org.junit.Before;
import org.junit.Test;

import org.oscm.ui.stubs.FacesContextStub;
import org.oscm.internal.types.exception.SaaSSystemException;

/**
 * JUnit tests for PercentConverter class.
 * 
 * @author cmin
 * 
 */
public class PercentConverterTest {

    private FacesContextStub context;
    private PercentConverter converter;

    @Before
    public void setup() {
        converter = new PercentConverter();
        context = new FacesContextStub(Locale.ENGLISH);
    }

    @Test(expected = SaaSSystemException.class)
    public void getAsObject_withABC() throws ConverterException {
        // given
        String str = "1abc1";
        UIComponent component = ConverterTestHelper.getComponent(false, null,
                null, "percent");

        // when
        converter.getAsObject(context, component, str);
    }

    @Test(expected = SaaSSystemException.class)
    public void getAsObject_withSpecialCharacter() throws ConverterException {
        // given
        String str = "1!@";
        UIComponent component = ConverterTestHelper.getComponent(false, null,
                null, "percent");

        // when
        converter.getAsObject(context, component, str);
    }

    @Test
    public void getAsObject_Zero() throws ConverterException {
        // given
        String str = "0%";
        UIComponent component = ConverterTestHelper.getComponent(false, null,
                null, "percent");

        // when
        Object actual = converter.getAsObject(context, component, str);

        // then
        assertEquals(new Long(0), actual);
    }

    @Test(expected = SaaSSystemException.class)
    public void getAsObject_empty() throws ConverterException {
        // given
        UIComponent component = ConverterTestHelper.getComponent(false, null,
                null, "percent");

        // when
        converter.getAsObject(context, component, "");

    }

    @Test(expected = IllegalArgumentException.class)
    public void getAsString_null() {
        // given
        UIComponent component = ConverterTestHelper.getComponent(false, null,
                null, "percent");

        // when
        converter.getAsString(context, component, null);
    }

}
