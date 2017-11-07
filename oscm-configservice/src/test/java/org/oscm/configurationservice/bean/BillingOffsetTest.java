/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: brandstetter                                                     
 *                                                                              
 *  Creation Date: 10.12.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.configurationservice.bean;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import org.junit.Before;
import org.junit.Test;

import org.oscm.types.constants.Configuration;
import org.oscm.internal.types.enumtypes.ConfigurationKey;

/**
 * @author baumann
 * 
 */
public class BillingOffsetTest {

    final static long DAY_IN_MS = 24 * 3600 * 1000;
    final static long HOUR_IN_MS = 3600 * 1000;
    final static long MINUTES_IN_MS = 60 * 1000;

    private ConfigurationServiceBean configServiceBean;

    @Before
    public void setup() throws Exception {
        configServiceBean = spy(new ConfigurationServiceBean());
    }

    @Test
    public void getBillingRunOffsetInMs_negativeOffset() throws Exception {
        // given
        long expectedDays = 0;
        doReturn(Long.valueOf(-15 * MINUTES_IN_MS)).when(configServiceBean)
                .getLongConfigurationSetting(
                        ConfigurationKey.TIMER_INTERVAL_BILLING_OFFSET,
                        Configuration.GLOBAL_CONTEXT);

        // when
        long result = configServiceBean.getBillingRunOffsetInMs();

        // then
        assertEquals(expectedDays, result);
    }

    @Test
    public void getBillingRunOffsetInMs_noOffset() throws Exception {
        // given
        long expectedDays = 0;
        doReturn(Long.valueOf(expectedDays)).when(configServiceBean)
                .getLongConfigurationSetting(
                        ConfigurationKey.TIMER_INTERVAL_BILLING_OFFSET,
                        Configuration.GLOBAL_CONTEXT);

        // when
        long result = configServiceBean.getBillingRunOffsetInMs();

        // then
        assertEquals(expectedDays, result);
    }

    @Test
    public void getBillingRunOffsetInMs_minutes() throws Exception {
        // given
        long expectedDays = 0;
        doReturn(Long.valueOf(15 * MINUTES_IN_MS)).when(configServiceBean)
                .getLongConfigurationSetting(
                        ConfigurationKey.TIMER_INTERVAL_BILLING_OFFSET,
                        Configuration.GLOBAL_CONTEXT);

        // when
        long result = configServiceBean.getBillingRunOffsetInMs();

        // then
        assertEquals(expectedDays, result);
    }

    @Test
    public void getBillingRunOffsetInMs_default() throws Exception {
        // given
        long expectedDays = 4 * DAY_IN_MS;
        doReturn(Long.valueOf(expectedDays)).when(configServiceBean)
                .getLongConfigurationSetting(
                        ConfigurationKey.TIMER_INTERVAL_BILLING_OFFSET,
                        Configuration.GLOBAL_CONTEXT);

        // when
        long result = configServiceBean.getBillingRunOffsetInMs();

        // then
        assertEquals(expectedDays, result);
    }

    @Test
    public void getBillingRunOffsetInMs_eod() throws Exception {
        // given
        long expectedDays = 15 * DAY_IN_MS;
        doReturn(Long.valueOf(expectedDays)).when(configServiceBean)
                .getLongConfigurationSetting(
                        ConfigurationKey.TIMER_INTERVAL_BILLING_OFFSET,
                        Configuration.GLOBAL_CONTEXT);

        // when
        long result = configServiceBean.getBillingRunOffsetInMs();

        // then
        assertEquals(expectedDays, result);
    }

    @Test
    public void getBillingRunStartTimeInMs_negativeOffset() throws Exception {
        // given
        long expectedStartTime = 0;
        long billingOffset = -4 * DAY_IN_MS - 5 * HOUR_IN_MS;
        doReturn(Long.valueOf(billingOffset)).when(configServiceBean)
                .getLongConfigurationSetting(
                        ConfigurationKey.TIMER_INTERVAL_BILLING_OFFSET,
                        Configuration.GLOBAL_CONTEXT);

        // when
        long result = configServiceBean.getBillingRunStartTimeInMs();

        // then
        assertEquals(expectedStartTime, result);
    }

    @Test
    public void getBillingRunStartTimeInMs_noOffset() throws Exception {
        // given
        long expectedStartTime = 0;
        long billingOffset = 4 * DAY_IN_MS + expectedStartTime;
        doReturn(Long.valueOf(billingOffset)).when(configServiceBean)
                .getLongConfigurationSetting(
                        ConfigurationKey.TIMER_INTERVAL_BILLING_OFFSET,
                        Configuration.GLOBAL_CONTEXT);

        // when
        long result = configServiceBean.getBillingRunStartTimeInMs();

        // then
        assertEquals(expectedStartTime, result);
    }

    @Test
    public void getBillingRunStartTimeInMs_default() throws Exception {
        // given
        long expectedStartTime = 0;
        long billingOffset = 4 * DAY_IN_MS + expectedStartTime;
        doReturn(Long.valueOf(billingOffset)).when(configServiceBean)
                .getLongConfigurationSetting(
                        ConfigurationKey.TIMER_INTERVAL_BILLING_OFFSET,
                        Configuration.GLOBAL_CONTEXT);

        // when
        long result = configServiceBean.getBillingRunStartTimeInMs();

        // then
        assertEquals(expectedStartTime, result);
    }

    @Test
    public void getBillingRunStartTimeInMs_eod() throws Exception {
        // given
        long expectedStartTime = DAY_IN_MS - 1;
        long billingOffset = 4 * DAY_IN_MS + expectedStartTime;
        doReturn(Long.valueOf(billingOffset)).when(configServiceBean)
                .getLongConfigurationSetting(
                        ConfigurationKey.TIMER_INTERVAL_BILLING_OFFSET,
                        Configuration.GLOBAL_CONTEXT);

        // when
        long result = configServiceBean.getBillingRunStartTimeInMs();

        // then
        assertEquals(expectedStartTime, result);
    }

    @Test
    public void getConfiguredDaysInMs_negativeOffset() throws Exception {
        // given
        long expectedDays = 0;

        // when
        long result = configServiceBean.getConfiguredDaysInMs(-4L);

        // then
        assertEquals(expectedDays, result);
    }

    @Test
    public void getConfiguredDaysInMs_noOffset() throws Exception {
        // given
        long expectedDays = 0;

        // when
        long result = configServiceBean.getConfiguredDaysInMs(0L);

        // then
        assertEquals(expectedDays, result);
    }

    @Test
    public void getConfiguredDaysInMs_default() throws Exception {
        // given
        long expectedDays = 4 * DAY_IN_MS;

        // when
        long result = configServiceBean.getConfiguredDaysInMs(expectedDays);

        // then
        assertEquals(expectedDays, result);
    }

    @Test
    public void getConfiguredDaysInMs_eod() {
        // given
        long expectedDays = 4 * DAY_IN_MS;

        // when
        long result = configServiceBean.getConfiguredDaysInMs(expectedDays
                + DAY_IN_MS - 1);

        // then
        assertEquals(expectedDays, result);
    }

    @Test
    public void getConfiguredTimerValueInMs_negativeOffset() throws Exception {
        // given
        long expectedDays = 0;
        doReturn(Long.valueOf(-4L)).when(configServiceBean)
                .getLongConfigurationSetting(
                        ConfigurationKey.TIMER_INTERVAL_BILLING_OFFSET,
                        Configuration.GLOBAL_CONTEXT);

        // when
        long result = configServiceBean.getConfiguredBillingOffsetInMs();

        // then
        assertEquals(expectedDays, result);
    }

    @Test
    public void getConfiguredTimerValueInMs_nullValue() throws Exception {
        // given
        doReturn(Long.valueOf(0)).when(configServiceBean)
                .getLongConfigurationSetting(
                        ConfigurationKey.TIMER_INTERVAL_BILLING_OFFSET,
                        Configuration.GLOBAL_CONTEXT);

        // when
        long result = configServiceBean.getConfiguredBillingOffsetInMs();

        // then
        assertEquals(0, result);
    }

    @Test
    public void getConfiguredTimerValueInMs_default() throws Exception {
        // given
        long expectedDays = 4 * DAY_IN_MS;
        doReturn(Long.valueOf(expectedDays)).when(configServiceBean)
                .getLongConfigurationSetting(
                        ConfigurationKey.TIMER_INTERVAL_BILLING_OFFSET,
                        Configuration.GLOBAL_CONTEXT);

        // when
        long result = configServiceBean.getConfiguredBillingOffsetInMs();

        // then
        assertEquals(expectedDays, result);
    }

    @Test
    public void getConfiguredTimerValueInMs_eod() throws Exception {
        // given
        long expectedValue = 15 * DAY_IN_MS + DAY_IN_MS - 1;
        doReturn(Long.valueOf(expectedValue)).when(configServiceBean)
                .getLongConfigurationSetting(
                        ConfigurationKey.TIMER_INTERVAL_BILLING_OFFSET,
                        Configuration.GLOBAL_CONTEXT);

        // when
        long result = configServiceBean.getConfiguredBillingOffsetInMs();

        // then
        assertEquals(expectedValue, result);
    }

    @Test
    public void getConfiguredTimerValueInMs_moreThan28days() throws Exception {
        // given
        long expectedDays = 28 * DAY_IN_MS;
        long billingOffset = expectedDays + 3 * DAY_IN_MS;
        doReturn(Long.valueOf(billingOffset)).when(configServiceBean)
                .getLongConfigurationSetting(
                        ConfigurationKey.TIMER_INTERVAL_BILLING_OFFSET,
                        Configuration.GLOBAL_CONTEXT);

        // when
        long result = configServiceBean.getConfiguredBillingOffsetInMs();

        // then
        assertEquals(expectedDays, result);
    }
}
