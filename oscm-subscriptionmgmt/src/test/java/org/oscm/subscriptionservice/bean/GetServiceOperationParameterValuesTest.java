/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 03.02.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.subscriptionservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProductOperation;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.test.data.TechnicalProducts;
import org.oscm.types.enumtypes.OperationParameterType;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.vo.VOServiceOperationParameterValues;
import org.oscm.internal.vo.VOSubscription;
import org.oscm.internal.vo.VOTechnicalServiceOperation;

/**
 * @author weiser
 * 
 */
public class GetServiceOperationParameterValuesTest extends SubscriptionServiceMockBase {

    private static final String P1 = "P1";
    private static final String P2 = "P2";
    private static final String EN = "en";
    private static final String TEXT = "some localized text";

    private SubscriptionServiceBean bean;

    private Subscription subscription = new Subscription();
    private TechnicalProductOperation operation = new TechnicalProductOperation();
    private Map<String, List<String>> values = new HashMap<>();
    private PlatformUser user = new PlatformUser();

    @Before
    public void setup() throws Exception {
        bean = new SubscriptionServiceBean();
        
        mockAllMembers(bean);

        user.setLocale(EN);

        operation.getParameters().add(
                TechnicalProducts.createOperationParameter(P1, true,
                        OperationParameterType.REQUEST_SELECT));
        operation.getParameters().add(
                TechnicalProducts.createOperationParameter(P2, false,
                        OperationParameterType.INPUT_STRING));
        values.put(P1, Arrays.asList("1", "2", "3"));

        when(bean.manageBean.loadSubscription(anyString(), anyLong()))
                .thenReturn(subscription);
        when(
                bean.manageBean.getOperationParameterValues(
                        any(Subscription.class),
                        any(TechnicalProductOperation.class))).thenReturn(
                values);

        when(
                bean.dataManager.getReference(
                        eq(TechnicalProductOperation.class), anyLong()))
                .thenReturn(operation);
        when(bean.dataManager.getCurrentUser()).thenReturn(user);

        when(
                bean.localizer.getLocalizedTextFromDatabase(anyString(),
                        anyLong(), any(LocalizedObjectTypes.class)))
                .thenReturn(TEXT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void subscriptionNull() throws Exception {
        bean.getServiceOperationParameterValues(null,
                new VOTechnicalServiceOperation());
    }

    @Test(expected = IllegalArgumentException.class)
    public void operationNull() throws Exception {
        bean.getServiceOperationParameterValues(new VOSubscription(), null);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void subscriptionModiefied() throws Exception {
        VOSubscription sub = new VOSubscription();
        sub.setVersion(-1);

        bean.getServiceOperationParameterValues(sub,
                new VOTechnicalServiceOperation());
    }

    @Test(expected = ConcurrentModificationException.class)
    public void operationModified() throws Exception {
        VOTechnicalServiceOperation op = new VOTechnicalServiceOperation();
        op.setVersion(-1);

        bean.getServiceOperationParameterValues(new VOSubscription(), op);
    }

    @Test
    public void emptyResult() throws Exception {
        values.clear();

        List<VOServiceOperationParameterValues> result = bean
                .getServiceOperationParameterValues(new VOSubscription(),
                        new VOTechnicalServiceOperation());

        verify(bean.manageBean).getOperationParameterValues(same(subscription),
                same(operation));
        assertTrue(result.isEmpty());
    }

    @Test
    public void unmappedParameterContained() throws Exception {
        values.put("P3", new ArrayList<String>());

        List<VOServiceOperationParameterValues> result = bean
                .getServiceOperationParameterValues(new VOSubscription(),
                        new VOTechnicalServiceOperation());

        assertEquals(1, result.size());
        VOServiceOperationParameterValues param = result.get(0);
        assertEquals(P1, param.getParameterId());
    }

    @Test
    public void ok() throws Exception {
        List<VOServiceOperationParameterValues> result = bean
                .getServiceOperationParameterValues(new VOSubscription(),
                        new VOTechnicalServiceOperation());

        assertEquals(1, result.size());
        VOServiceOperationParameterValues param = result.get(0);
        assertEquals(P1, param.getParameterId());
        assertEquals(values.get(P1), result.get(0).getValues());
    }

}
