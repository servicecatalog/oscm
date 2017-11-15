/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 30.08.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.subscriptionservice.bean;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import javax.ejb.SessionContext;

import org.junit.Before;
import org.junit.Test;

import org.oscm.domobjects.Parameter;
import org.oscm.domobjects.ParameterDefinition;
import org.oscm.domobjects.ParameterSet;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.types.enumtypes.PlatformParameterIdentifiers;
import org.oscm.internal.types.enumtypes.ParameterType;
import org.oscm.internal.types.exception.ServiceParameterException;

/**
 * @author weiser
 * 
 */
public class SubscriptionServiceVerifyNamedUserTest {

    private SubscriptionServiceBean bean;

    private Subscription sub;
    private Parameter param;

    @Before
    public void setup() {
        bean = new SubscriptionServiceBean();
        bean.sessionCtx = mock(SessionContext.class);

        sub = new Subscription();
        sub.setSubscriptionId("subscriptionId");

        Product prod = new Product();
        prod.setParameterSet(new ParameterSet());

        ParameterDefinition pd = new ParameterDefinition();
        pd.setParameterId(PlatformParameterIdentifiers.NAMED_USER);
        pd.setParameterType(ParameterType.PLATFORM_PARAMETER);

        param = new Parameter();
        param.setParameterDefinition(pd);
        param.setParameterSet(prod.getParameterSet());

        prod.getParameterSet().getParameters().add(param);
        sub.setProduct(prod);
    }

    @Test
    public void verifyParameterNamedUser_Null() throws Exception {
        bean.verifyParameterNamedUser(null);

        verifyZeroInteractions(bean.sessionCtx);
    }

    @Test
    public void verifyParameterNamedUser_ParameterSetNull() throws Exception {
        sub.getProduct().setParameterSet(null);

        bean.verifyParameterNamedUser(sub);

        verifyZeroInteractions(bean.sessionCtx);
    }

    @Test
    public void verifyParameterNamedUser_ParameterSetEmpty() throws Exception {
        sub.getProduct().setParameterSet(new ParameterSet());

        bean.verifyParameterNamedUser(sub);

        verifyZeroInteractions(bean.sessionCtx);
    }

    @Test
    public void verifyParameterNamedUser_ParamNullValue() throws Exception {
        bean.verifyParameterNamedUser(sub);

        verifyZeroInteractions(bean.sessionCtx);
    }

    @Test
    public void verifyParameterNamedUser_ParamEmptyValue() throws Exception {
        param.setValue("");

        bean.verifyParameterNamedUser(sub);

        verifyZeroInteractions(bean.sessionCtx);
    }

    @Test
    public void verifyParameterNamedUser_NonIntegerValue() throws Exception {
        param.setValue("abc");

        bean.verifyParameterNamedUser(sub);

        verifyZeroInteractions(bean.sessionCtx);
    }

    @Test
    public void verifyParameterNamedUser_DifferentParamType() throws Exception {
        param.getParameterDefinition().setParameterType(
                ParameterType.SERVICE_PARAMETER);

        bean.verifyParameterNamedUser(sub);

        verifyZeroInteractions(bean.sessionCtx);
    }

    @Test
    public void verifyParameterNamedUser_DifferentParamId() throws Exception {
        param.getParameterDefinition().setParameterId("parameterId");

        bean.verifyParameterNamedUser(sub);

        verifyZeroInteractions(bean.sessionCtx);
    }

    @Test
    public void verifyParameterNamedUser() throws Exception {
        param.setValue("5");

        bean.verifyParameterNamedUser(sub);

        verifyZeroInteractions(bean.sessionCtx);
    }

    @Test(expected = ServiceParameterException.class)
    public void verifyParameterNamedUser_Negative() throws Exception {
        // given: one user allowed, two added
        param.setValue("1");
        sub.addUser(new PlatformUser(), null);
        sub.addUser(new PlatformUser(), null);

        try {
            bean.verifyParameterNamedUser(sub);
        } catch (ServiceParameterException e) {
            verify(bean.sessionCtx, times(1)).setRollbackOnly();
            assertEquals(e.getMessageKey(),
                    "ex.ServiceParameterException.PLATFORM_PARAMETER.NAMED_USER");
            String[] mp = e.getMessageParams();
            assertEquals(param.getValue(), mp[0]);
            assertEquals(sub.getSubscriptionId(), mp[1]);
            throw e;
        }
    }

}
