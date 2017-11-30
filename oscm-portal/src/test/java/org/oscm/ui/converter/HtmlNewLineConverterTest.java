/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Lorenz Goebel                                         
 *                                                                              
 *  Creation Date: 14.11.2012                                                      
 *                                                                              
 *  Completion Time: 14.11.2012                                    
 *                                                                              
 *******************************************************************************/
package org.oscm.ui.converter;

import java.util.Locale;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.ConverterException;

import org.junit.Assert;

import org.junit.Before;
import org.junit.Test;

import org.oscm.ui.stubs.FacesContextStub;

/**
 * Test for the new line converter.
 * 
 * @author goebel
 */
public class HtmlNewLineConverterTest {
    FacesContext context;
    HtmlNewLineConverter converter;

    UIComponent component;

    @Before
    public void setup() {
        converter = new HtmlNewLineConverter();
        context = new FacesContextStub(Locale.ENGLISH);
        component = ConverterTestHelper.getComponent(false, null, null, "text");
    }

    /**
     * Test without conversion.
     */
    @Test
    public void getAsObject_noLineBreak() throws ConverterException {

        String value = "abc";
        String expected = "abc";

        String actual = (String) converter.getAsObject(context, component,
                value);

        Assert.assertEquals(expected, actual);
    }

    /**
     * Test no server conversion for new lines to HTML br tags.
     */
    @Test
    public void getAsObject_lineBreak() throws ConverterException {

        String value = "test\ntext2\ntext3";
        String expected = value;

        String actual = (String) converter.getAsObject(context, component,
                value);

        Assert.assertEquals(expected, actual);
    }

    /**
     * Test back to client conversion without lines breaks.
     */
    @Test
    public void getAsString_noLineBreak() throws ConverterException {

        String value = "abc";
        String expected = "abc";

        String actual = converter.getAsString(context, component, value);

        Assert.assertEquals(expected, actual);
    }

    /**
     * Test back to client converting new lines to HTML br tags.
     */
    @Test
    public void getAsString_lineBreak() throws ConverterException {

        String value = "test\ntext2\ntext3";
        String expected = "test<br />text2<br />text3";

        String actual = converter.getAsString(context, component, value);

        Assert.assertEquals(expected, actual);
    }

}
