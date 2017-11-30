/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 04.03.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.validator;

import org.junit.Before;
import org.junit.Test;
import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.stubs.*;

import javax.faces.validator.ValidatorException;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

/**
 * @author Mike J&auml;ger
 * 
 */
public class PasswordValidatorTest {

    private PasswordValidator validator;
    private FacesContextStub fcStub;
    private UIInputStub ucStub;
    private UIViewRootStub vrStub;
    private ResourceBundleStub rbStub;
    private String requestedResourceKey;
    private UIInputStub ucStub2;

    @Before
    public void setUp() {
        validator = new PasswordValidator();
        rbStub = new ResourceBundleStub() {

            @Override
            protected Object handleGetObject(String key) {
                requestedResourceKey = key;
                return super.handleGetObject(key);
            }

        };
        fcStub = new FacesContextStub(Locale.GERMAN);
        ((ApplicationStub) fcStub.getApplication())
                .setResourceBundleStub(rbStub);
        ucStub = new UIInputStub("newPassword");
        ucStub2 = new UIInputStub("newPassword2");
        vrStub = new UIViewRootStub();
        fcStub.setViewRoot(vrStub);
        vrStub.addComponent("newPassword", ucStub);
        vrStub.addComponent("newPassword2", ucStub2);
    }

    @Test
    public void testValidateNullParameters() throws Exception {
        validator.validate(fcStub, ucStub2, null);
    }

    @Test(expected = ValidatorException.class)
    public void testValidateNullInputFieldReference() throws Exception {
        vrStub.resetComponents();
        try {
            validator.validate(fcStub, ucStub2, "bla");
        } catch (ValidatorException e) {
            assertEquals("Wrong exception type - ",
                    BaseBean.ERROR_USER_PWD_MATCH, requestedResourceKey);
            throw e;
        }
    }

    @Test(expected = ValidatorException.class)
    public void testValidateSecondParameterNull() throws Exception {
        ucStub.setValue(null);
        try {
            validator.validate(fcStub, ucStub2, "bla");
        } catch (ValidatorException e) {
            assertEquals("Wrong exception type - ",
                    BaseBean.ERROR_USER_PWD_MATCH, requestedResourceKey);
            throw e;
        }
    }

    @Test(expected = ValidatorException.class)
    public void testValidateFirstParameterNull() throws Exception {
        ucStub2.setValue(null);
        try {
            validator.validate(fcStub, ucStub, "blabla");
        } catch (ValidatorException e) {
            assertEquals("Wrong exception type - ",
                    BaseBean.ERROR_USER_PWD_MATCH, requestedResourceKey);
            throw e;
        }
    }

    @Test(expected = ValidatorException.class)
    public void testValidateFirstParameterNullShort() throws Exception {
        ucStub2.setValue(null);
        try {
            validator.validate(fcStub, ucStub, "bla");
        } catch (ValidatorException e) {
            assertEquals("Wrong exception type - ",
                    BaseBean.ERROR_USER_PWD_LENGTH, requestedResourceKey);
            throw e;
        }
    }

    @Test(expected = ValidatorException.class)
    public void testValidateFirstParameterEmptyString() throws Exception {
        ucStub.setValue("");
        try {
            validator.validate(fcStub, ucStub, "");
        } catch (ValidatorException e) {
            assertEquals("Wrong exception type - ",
                    BaseBean.ERROR_USER_PWD_LENGTH, requestedResourceKey);
            throw e;
        }
    }

    @Test(expected = ValidatorException.class)
    public void testValidateSecondParameterEmptyString() throws Exception {
        ucStub.setValue("");
        try {
            validator.validate(fcStub, ucStub2, "secret");
        } catch (ValidatorException e) {
            assertEquals("Wrong exception type - ",
                    BaseBean.ERROR_USER_PWD_MATCH, requestedResourceKey);
            throw e;
        }
    }

    @Test(expected = ValidatorException.class)
    public void testValidateFirstParameterToLessChars() throws Exception {
        ucStub2.setValue("secret");
        try {
            validator.validate(fcStub, ucStub, "ddd");
        } catch (ValidatorException e) {
            assertEquals("Wrong exception type - ",
                    BaseBean.ERROR_USER_PWD_LENGTH, requestedResourceKey);
            throw e;
        }
    }

    @Test(expected = ValidatorException.class)
    public void testValidateSecondParameterToLessChars() throws Exception {
        ucStub.setValue("ddddd");
        try {
            validator.validate(fcStub, ucStub2, "secret");
        } catch (ValidatorException e) {
            assertEquals("Wrong exception type - ",
                    BaseBean.ERROR_USER_PWD_MATCH, requestedResourceKey);
            throw e;
        }
    }

    @Test(expected = ValidatorException.class)
    public void testValidateNoMatch() throws Exception {
        ucStub.setValue("dddddd");
        try {
            validator.validate(fcStub, ucStub2, "secret");
        } catch (ValidatorException e) {
            assertEquals("Wrong exception type - ",
                    BaseBean.ERROR_USER_PWD_MATCH, requestedResourceKey);
            throw e;
        }

    }

    @Test
    public void testValidateMatch() throws Exception {
        ucStub.setValue("secret");
        ucStub2.setValue("secret");
        validator.validate(fcStub, ucStub, "secret");
        validator.validate(fcStub, ucStub2, "secret");
    }

    @Test(expected = ValidatorException.class)
    public void testValidateEmpty() throws ValidatorException {
        try {
            validator.validate(fcStub, ucStub2, "");
        } catch (ValidatorException e) {
            assertEquals("Wrong exception type - ",
                    BaseBean.ERROR_USER_PWD_MATCH, requestedResourceKey);
            throw e;
        }
    }

    @Test(expected = ValidatorException.class)
    public void testValidateGreaterThanSixCharactersAndNotEqual()
            throws ValidatorException {
        ucStub.setValue("secret");
        try {
            validator.validate(fcStub, ucStub2, "mypassword1");
        } catch (ValidatorException e) {
            assertEquals("Wrong exception type - ",
                    BaseBean.ERROR_USER_PWD_MATCH, requestedResourceKey);
            throw e;
        }
    }

}
