/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 04.03.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.validator;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Calendar;
import java.util.Locale;

import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import org.oscm.ui.stubs.FacesContextStub;
import org.oscm.ui.stubs.UIInputStub;
import org.oscm.ui.stubs.UIViewRootStub;

/**
 * @author pock
 * 
 */
public class DateFromToValidatorTest {

    private DateFromToValidator validator;
    private FacesContextStub fcStub;
    private UIInputStub toDateStub;
    private UIInputStub fromDateStub;
    private UIViewRootStub vrStub;

    @Before
    public void setUp() {
        validator = new DateFromToValidator();
        fcStub = new FacesContextStub(Locale.GERMAN);
        fromDateStub = new UIInputStub("form:fromDate");
        toDateStub = new UIInputStub("form:toDate");
        vrStub = new UIViewRootStub();
        fcStub.setViewRoot(vrStub);
        vrStub.addComponent(fromDateStub.getClientId(fcStub), fromDateStub);
        vrStub.addComponent(toDateStub.getClientId(fcStub), toDateStub);
    }

    @Test(expected = ValidatorException.class)
    public void testValidateNullParameters() throws Exception {
        validator.validate(fcStub, fromDateStub, null);

        validator.validate(fcStub, toDateStub, null);
        DateFromToValidator spy = spy(validator);
        doNothing().when(spy).handleError(Matchers.any(FacesContext.class),
                Matchers.anyString(), Matchers.anyString());
    }

    @Test(expected = ValidatorException.class)
    public void testValidateFromAfterTo() throws Exception {
        Calendar cal = Calendar.getInstance();
        toDateStub.setValue(cal.getTime());
        validator.setToDate(cal.getTime());
        cal.add(Calendar.DAY_OF_MONTH, 5);
        fromDateStub.setValue(cal.getTime());
        validator.setFromDate(cal.getTime());

        validator.validate(fcStub, fromDateStub, fromDateStub.getValue());
    }

    @Test(expected = ValidatorException.class)
    public void testValidateToBeforeFrom() throws Exception {
        Calendar cal = Calendar.getInstance();
        toDateStub.setValue(cal.getTime());
        validator.setToDate(cal.getTime());
        cal.add(Calendar.DAY_OF_MONTH, 5);
        fromDateStub.setValue(cal.getTime());
        validator.setFromDate(cal.getTime());

        validator.validate(fcStub, toDateStub, toDateStub.getValue());
    }

    @Test
    public void testValidateError() throws Exception {
        // Given invalid date range
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        toDateStub.setValue(cal.getTime());
        validator.setToDate(cal.getTime());
        cal.add(Calendar.DAY_OF_MONTH, 5);
        fromDateStub.setValue(cal.getTime());
        validator.setFromDate(cal.getTime());

        DateFromToValidator spy = spy(validator);
        doNothing().when(spy).handleError(Matchers.any(FacesContext.class),
                Matchers.anyString(), Matchers.anyString());

        // when
        spy.validate(fcStub, toDateStub, toDateStub.getValue());

        // then
        verify(spy, times(1)).handleError(any(FacesContext.class), anyString(),
                anyString());
    }
}
