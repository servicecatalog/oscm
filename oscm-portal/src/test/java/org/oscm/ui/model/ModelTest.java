/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.model;

import static org.oscm.test.Numbers.L1000;
import static org.oscm.test.Numbers.L2000;
import static org.oscm.test.Numbers.L3000;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import org.oscm.internal.subscriptions.POSubscription;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.vo.BaseVO;
import org.oscm.internal.vo.VOSubscription;

public class ModelTest {

    public void populate(BaseVO vo, Object[][] data) throws SecurityException,
            NoSuchMethodException, IllegalArgumentException,
            IllegalAccessException, InvocationTargetException {

        for (int i = 0; i < data.length; i++) {
            String member = (String) data[i][0];
            String methodName = "set" + member.substring(0, 1).toUpperCase()
                    + member.substring(1);
            Method m = vo.getClass().getMethod(methodName,
                    data[i][1].getClass());
            m.invoke(vo, data[i][1]);
        }
    }

    public void verify(Object model, Object[][] data) throws SecurityException,
            NoSuchMethodException, IllegalArgumentException,
            IllegalAccessException, InvocationTargetException {

        for (int i = 0; i < data.length; i++) {
            String member = (String) data[i][0];
            String methodName = "get" + member.substring(0, 1).toUpperCase()
                    + member.substring(1);
            Method m = model.getClass().getMethod(methodName, (Class[]) null);
            m.invoke(model, (Object[]) null);
            Assert.assertEquals(model.getClass().getSimpleName() + ": "
                    + data[i][0], data[i][1], m.invoke(model, (Object[]) null));
        }
    }

    @Test
    public void testSubscription() throws Exception {

        Object[][] data = { { "activationDate", L1000 },
                { "creationDate", L2000 }, { "deactivationDate", L3000 },
                { "serviceAccessInfo", "serviceAccessInfo" },
                { "serviceAccessType", ServiceAccessType.DIRECT },
                { "serviceBaseURL", "serviceBaseURL" },
                { "serviceLoginPath", "serviceLoginPath" },
                { "serviceId", "serviceId" },
                { "serviceInstanceId", "serviceInstanceId" },
                { "purchaseOrderNumber", "purchaseOrderNumber" },
                { "status", SubscriptionStatus.EXPIRED } };

        VOSubscription sub = new VOSubscription();
        populate(sub, data);

        data[0][1] = new Date(1000);
        data[1][1] = new Date(2000);
        data[2][1] = new Date(3000);
        POSubscription poSub = new POSubscription(sub);
        poSub.setStatus((SubscriptionStatus) data[data.length - 1][1]);
        verify(poSub, data);
    }
}
