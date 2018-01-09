/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *******************************************************************************/

package org.oscm.billing.external.adapter.bean;

import static org.junit.Assert.assertEquals;
import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import org.oscm.billing.external.exception.BillingException;

public class ConfigPropertiesTest {
    ConfigProperties properties;

    @Before
    public void setup() throws Exception {
        properties = Mockito.spy(new ConfigProperties(PriceModelPlugin.ID));
    }

    @Test(expected = BillingException.class)
    public void getConfigProperty_unknownKey() throws BillingException {
        // given
        String key = "key";
        properties.loadProperties();

        // when
        properties.getConfigProperty(key);
    }

    @Test
    public void loadProperties() throws BillingException {
        // when
        properties.loadProperties();

        // then
        Assert.assertNotNull(properties.getConfigProperties());
        assertEquals(
                "http://localhost:8680/oscm-file-billing/rest/priceModel",
                properties.getConfigProperty(PriceModelPlugin.PRICEMODEL_URL));
    }

}
