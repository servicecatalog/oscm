/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 31.01.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.subscriptionservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.SessionContext;

import org.junit.Before;
import org.junit.Test;

import org.oscm.applicationservice.local.ApplicationServiceLocal;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.OperationParameter;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.TechnicalProductOperation;
import org.oscm.domobjects.UsageLicense;
import org.oscm.types.enumtypes.OperationParameterType;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.ValidationException;

/**
 * @author weiser
 * 
 */
public class ManageSubscriptionBeanTest {

    private static final String USER_ID = "user id";
    private static final String APPLICATION_USER_ID = "application user id";
    private static final String INSTANCE_ID = "instance id";
    private static final String OPERATION_ID = "operation id";

    private ManageSubscriptionBean bean;

    private PlatformUser currentUser;
    private Subscription subscription;
    private TechnicalProductOperation operation;
    private Map<String, List<String>> parameterValues;

    @Before
    public void setup() throws Exception {
        bean = new ManageSubscriptionBean();

        bean.appManager = mock(ApplicationServiceLocal.class);
        bean.dataManager = mock(DataService.class);
        bean.sessionCtx = mock(SessionContext.class);

        TechnicalProduct technicalProduct = new TechnicalProduct();
        Product product = new Product();
        product.setTechnicalProduct(technicalProduct);

        currentUser = new PlatformUser();
        currentUser.setUserId(USER_ID);

        UsageLicense license = new UsageLicense();
        license.setUser(currentUser);
        license.setApplicationUserId(APPLICATION_USER_ID);

        subscription = new Subscription();
        subscription.setProduct(product);
        license.setSubscription(subscription);
        subscription.getUsageLicenses().add(license);
        subscription.setProductInstanceId(INSTANCE_ID);

        operation = new TechnicalProductOperation();
        operation.setTechnicalProduct(technicalProduct);
        operation.setOperationId(OPERATION_ID);

        parameterValues = new HashMap<String, List<String>>();
        parameterValues.put("1", Arrays.asList("A", "B", "C"));
        parameterValues.put("A", Arrays.asList("1", "2", "3"));

        when(bean.dataManager.getCurrentUser()).thenReturn(currentUser);

        when(
                bean.appManager.getOperationParameterValues(anyString(),
                        any(TechnicalProductOperation.class),
                        any(Subscription.class))).thenReturn(parameterValues);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getOperationParameterValues_SubscriptionNull() throws Exception {
        bean.getOperationParameterValues(null, operation);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getOperationParameterValues_OperationNull() throws Exception {
        bean.getOperationParameterValues(subscription, null);
    }

    /*
     * Operation not part of the technical service the subscription belongs to
     */
    @Test(expected = OperationNotPermittedException.class)
    public void getOperationParameterValues_NotPermitted_Operation()
            throws Exception {
        TechnicalProduct tp = new TechnicalProduct();
        tp.setKey(5);
        operation.setTechnicalProduct(tp);

        bean.getOperationParameterValues(subscription, operation);

    }

    /*
     * user has no usage license for the subscription
     */
    @Test(expected = OperationNotPermittedException.class)
    public void getOperationParameterValues_NotPermitted_Subscription()
            throws Exception {
        subscription.getUsageLicenses().clear();

        bean.getOperationParameterValues(subscription, operation);
    }

    @Test
    public void getOperationParameterValues_ApplicationUserIdPassed()
            throws Exception {
        bean.getOperationParameterValues(subscription, operation);

        verify(bean.appManager).getOperationParameterValues(
                eq(APPLICATION_USER_ID), eq(operation), eq(subscription));
    }

    @Test
    public void getOperationParameterValues_UserIdPassed() throws Exception {
        subscription.getUsageLicenseForUser(currentUser).setApplicationUserId(
                null);

        bean.getOperationParameterValues(subscription, operation);

        verify(bean.appManager).getOperationParameterValues(eq(USER_ID),
                eq(operation), eq(subscription));
    }

    @Test
    public void getOperationParameterValues() throws Exception {
        Map<String, List<String>> result = bean.getOperationParameterValues(
                subscription, operation);

        assertSame(parameterValues, result);
    }

    @Test(expected = ValidationException.class)
    public void validateMandatoryParametersAreSet_NotContained()
            throws Exception {
        String member = "param1, param2";
        prepareOperationParameters(true, true, false);

        try {
            bean.validateMandatoryParametersAreSet(operation,
                    new HashMap<String, String>());
        } catch (ValidationException e) {
            assertEquals(ValidationException.ReasonEnum.REQUIRED, e.getReason());
            assertEquals(member, e.getMember());
            assertEquals(member, e.getMessageParams()[0]);
            throw e;
        }
    }

    @Test(expected = ValidationException.class)
    public void validateMandatoryParametersAreSet_NullAndEmptyValue()
            throws Exception {
        String member = "param1, param2";
        prepareOperationParameters(true, true, false);
        Map<String, String> map = new HashMap<String, String>();
        map.put("param1", null);
        map.put("param2", " ");

        try {
            bean.validateMandatoryParametersAreSet(operation, map);
        } catch (ValidationException e) {
            assertEquals(ValidationException.ReasonEnum.REQUIRED, e.getReason());
            assertEquals(member, e.getMember());
            assertEquals(member, e.getMessageParams()[0]);
            throw e;
        }
    }

    @Test
    public void validateMandatoryParametersAreSet() throws Exception {
        prepareOperationParameters(true, true, false);
        Map<String, String> map = new HashMap<String, String>();
        map.put("param1", "v1");
        map.put("param2", "v2");

        bean.validateMandatoryParametersAreSet(operation, map);
    }

    private void prepareOperationParameters(boolean... mandatory) {
        int index = 1;
        for (boolean b : mandatory) {
            OperationParameter op = new OperationParameter();
            op.setId("param" + (index++));
            op.setMandatory(b);
            op.setTechnicalProductOperation(operation);
            op.setType(OperationParameterType.INPUT_STRING);
            operation.getParameters().add(op);
        }
    }
}
