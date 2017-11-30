/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: weiser                                                       
 *                                                                              
 *  Creation Date: 06.10.2011                                                      
 *                                                                              
 *  Completion Time: 07.10.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.oscm.domobjects.PaymentType;
import org.oscm.internal.intf.AccountService;
import org.oscm.internal.intf.ConfigurationService;
import org.oscm.internal.types.enumtypes.PerformanceHint;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOOrganizationPaymentConfiguration;
import org.oscm.internal.vo.VOPaymentType;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOServicePaymentConfiguration;
import org.oscm.ui.model.CustomerPaymentTypes;
import org.oscm.ui.model.SelectedPaymentType;
import org.oscm.ui.model.ServicePaymentTypes;

/**
 * Tests for the {@link PaymentConfigurationBean}.
 * 
 * @author weiser
 */
@SuppressWarnings("boxing")
public class PaymentConfigurationBeanTest {

    private static final int NUM_OF_CUSTOMERS = 4;
    private static final int NUM_OF_SERVICES = 6;

    private PaymentConfigurationBean bean;
    private AccountService accountingService;

    private Set<VOPaymentType> paymentTypes;
    private Set<VOPaymentType> customerDefault;
    private Set<VOPaymentType> serviceDefault;
    private List<VOOrganizationPaymentConfiguration> customerConfig;
    private List<VOServicePaymentConfiguration> serviceConfig;
    private ConfigurationService configurationService;

    @Captor
    ArgumentCaptor<Set<VOPaymentType>> acCustomerDefault;
    @Captor
    ArgumentCaptor<List<VOOrganizationPaymentConfiguration>> acCustomers;
    @Captor
    ArgumentCaptor<Set<VOPaymentType>> acServiceDefault;
    @Captor
    ArgumentCaptor<List<VOServicePaymentConfiguration>> acServices;

    @Before
    public void setup() throws Exception {
        paymentTypes = getPaymentTypes(PaymentType.CREDIT_CARD,
                PaymentType.DIRECT_DEBIT, PaymentType.INVOICE);
        customerDefault = getPaymentTypes(PaymentType.CREDIT_CARD,
                PaymentType.INVOICE);
        serviceDefault = getPaymentTypes(PaymentType.DIRECT_DEBIT,
                PaymentType.INVOICE);
        customerConfig = getCustomerConfiguration();
        serviceConfig = getServiceConfiguration();

        accountingService = mock(AccountService.class);

        when(accountingService.getAvailablePaymentTypesForOrganization()).thenReturn(
                paymentTypes);
        when(accountingService.getDefaultPaymentConfiguration()).thenReturn(customerDefault);
        when(accountingService.getCustomerPaymentConfiguration()).thenReturn(customerConfig);
        when(accountingService.getDefaultServicePaymentConfiguration()).thenReturn(
                serviceDefault);
        when(
                accountingService.getServicePaymentConfiguration(PerformanceHint.ONLY_FIELDS_FOR_LISTINGS))
                .thenReturn(serviceConfig);
        when(
                Boolean.valueOf(accountingService.savePaymentConfiguration(
                        anySetOf(VOPaymentType.class),
                        anyListOf(VOOrganizationPaymentConfiguration.class),
                        anySetOf(VOPaymentType.class),
                        anyListOf(VOServicePaymentConfiguration.class))))
                .thenReturn(Boolean.TRUE);

        bean = spy(new PaymentConfigurationBean());
        doReturn(accountingService).when(bean).getAccountingService();
        // avoid exceptions when accessing JSFUtils...
        doNothing().when(bean).addInfoOrProgressMessage(anyBoolean(),
                anyString(), anyString());
        configurationService = mock(ConfigurationService.class);
        bean.setCfgService(configurationService);
        doReturn(true).when(configurationService).isPaymentInfoAvailable();
    }

    @Test
    public void getEnabledPaymentTypesForSupplier() throws Exception {
        List<SelectedPaymentType> types = bean
                .getEnabledPaymentTypesForSupplier();
        for (SelectedPaymentType pt : types) {
            assertTrue(paymentTypes.contains(pt.getPaymentType()));
        }
        verify(accountingService, times(1)).getAvailablePaymentTypesForOrganization();

        // value must be cached - service mock must not be called a second time
        bean.getEnabledPaymentTypesForSupplier();
        verifyNoMoreInteractions(accountingService);
    }

    @Test
    public void getNumOfPaymentColumns() throws Exception {
        int columns = bean.getNumOfPaymentColumns();
        verify(accountingService, times(1)).getAvailablePaymentTypesForOrganization();
        assertEquals(paymentTypes.size() * 2, columns);

        // test caching
        bean.getNumOfPaymentColumns();
        verifyNoMoreInteractions(accountingService);
    }

    @Test
    public void getDefaultPaymentTypes() throws Exception {
        List<SelectedPaymentType> types = bean.getDefaultPaymentTypes();

        verify(accountingService, times(1)).getAvailablePaymentTypesForOrganization();
        verify(accountingService, times(1)).getDefaultPaymentConfiguration();

        assertEquals(paymentTypes.size(), types.size());

        for (SelectedPaymentType pt : types) {
            // every payment type must be available for the supplier
            assertTrue(paymentTypes.contains(pt.getPaymentType()));
            // if it is no customer default, it must no be selected
            assertEquals(customerDefault.contains(pt.getPaymentType()),
                    pt.isSelected());
        }

        // test caching
        bean.getDefaultPaymentTypes();
        verifyNoMoreInteractions(accountingService);
    }

    @Test
    public void getCustomerPaymentTypes() throws Exception {
        List<CustomerPaymentTypes> types = bean.getCustomerPaymentTypes();

        verify(accountingService, times(1)).getAvailablePaymentTypesForOrganization();
        verify(accountingService, times(1)).getCustomerPaymentConfiguration();

        assertEquals(NUM_OF_CUSTOMERS, types.size());

        for (CustomerPaymentTypes cpt : types) {
            List<SelectedPaymentType> custTypes = cpt.getPaymentTypes();
            assertEquals(paymentTypes.size(), custTypes.size());
            Set<VOPaymentType> enabled = getForOrg(cpt.getCustomer().getKey(),
                    customerConfig);
            for (SelectedPaymentType spt : custTypes) {
                assertEquals(enabled.contains(spt.getPaymentType()),
                        spt.isSelected());
            }
        }

        // test caching
        bean.getCustomerPaymentTypes();
        verifyNoMoreInteractions(accountingService);
    }

    @Test
    public void getDefaultServicePaymentTypes() throws Exception {
        List<SelectedPaymentType> types = bean.getDefaultServicePaymentTypes();

        verify(accountingService, times(1)).getAvailablePaymentTypesForOrganization();
        verify(accountingService, times(1)).getDefaultServicePaymentConfiguration();

        assertEquals(paymentTypes.size(), types.size());

        for (SelectedPaymentType pt : types) {
            // every payment type must be available for the supplier
            assertTrue(paymentTypes.contains(pt.getPaymentType()));
            // if it is no customer default, it must no be selected
            assertEquals(serviceDefault.contains(pt.getPaymentType()),
                    pt.isSelected());
        }

        // test caching
        bean.getDefaultServicePaymentTypes();
        verifyNoMoreInteractions(accountingService);
    }

    @Test
    public void getServicePaymentTypes() throws Exception {
        List<ServicePaymentTypes> types = bean.getServicePaymentTypes();

        verify(accountingService, times(1)).getServicePaymentConfiguration(
                PerformanceHint.ONLY_FIELDS_FOR_LISTINGS);

        assertEquals(NUM_OF_SERVICES, types.size());

        for (ServicePaymentTypes spt : types) {
            List<SelectedPaymentType> svcTypes = spt.getPaymentTypes();
            assertEquals(paymentTypes.size(), svcTypes.size());
            Set<VOPaymentType> enabled = getForSvc(spt.getService().getKey(),
                    serviceConfig);
            for (SelectedPaymentType t : svcTypes) {
                assertEquals(enabled.contains(t.getPaymentType()),
                        t.isSelected());
            }
        }

        // test caching
        bean.getServicePaymentTypes();
    }

    @Test
    public void modifyPaymentEnablement() throws Exception {
        MockitoAnnotations.initMocks(this);
        // initialize the fields
        List<SelectedPaymentType> cDef = bean.getDefaultPaymentTypes();
        List<SelectedPaymentType> sDef = bean.getDefaultServicePaymentTypes();
        List<CustomerPaymentTypes> cpt = bean.getCustomerPaymentTypes();
        List<ServicePaymentTypes> spt = bean.getServicePaymentTypes();

        // enable all on both defaults
        for (SelectedPaymentType t : cDef) {
            t.setSelected(true);
        }
        for (SelectedPaymentType t : sDef) {
            t.setSelected(true);
        }

        // disable all customer and service payment types
        for (CustomerPaymentTypes t : cpt) {
            for (SelectedPaymentType pt : t.getPaymentTypes()) {
                pt.setSelected(false);
            }
        }
        for (ServicePaymentTypes t : spt) {
            for (SelectedPaymentType pt : t.getPaymentTypes()) {
                pt.setSelected(false);
            }
        }

        // now save
        String result = bean.modifyPaymentEnablement();

        assertEquals(BaseBean.OUTCOME_SUCCESS, result);

        verify(accountingService).savePaymentConfiguration(acCustomerDefault.capture(),
                acCustomers.capture(), acServiceDefault.capture(),
                acServices.capture());

        assertEquals(paymentTypes, acCustomerDefault.getValue());
        assertEquals(paymentTypes, acServiceDefault.getValue());

        List<VOOrganizationPaymentConfiguration> cust = acCustomers.getValue();
        assertEquals(NUM_OF_CUSTOMERS, cust.size());
        for (VOOrganizationPaymentConfiguration c : cust) {
            assertTrue(c.getEnabledPaymentTypes().isEmpty());
        }

        List<VOServicePaymentConfiguration> svc = acServices.getValue();
        assertEquals(NUM_OF_SERVICES, svc.size());
        for (VOServicePaymentConfiguration s : svc) {
            assertTrue(s.getEnabledPaymentTypes().isEmpty());
        }
    }

    private static final Set<VOPaymentType> getPaymentTypes(String... types) {
        Set<VOPaymentType> paymentTypes = new HashSet<VOPaymentType>();
        for (String type : types) {
            VOPaymentType pt = new VOPaymentType();
            pt.setPaymentTypeId(type);
            paymentTypes.add(pt);
        }
        return paymentTypes;
    }

    private static final List<VOOrganizationPaymentConfiguration> getCustomerConfiguration() {
        List<VOOrganizationPaymentConfiguration> result = new ArrayList<VOOrganizationPaymentConfiguration>();
        for (int i = 0; i < NUM_OF_CUSTOMERS; i++) {
            VOOrganizationPaymentConfiguration conf = new VOOrganizationPaymentConfiguration();
            VOOrganization org = new VOOrganization();
            org.setKey(i);
            org.setOrganizationId(String.valueOf(i));
            conf.setOrganization(org);
            if (i % 2 == 0) {
                conf.setEnabledPaymentTypes(getPaymentTypes(
                        PaymentType.CREDIT_CARD, PaymentType.INVOICE));
            } else {
                conf.setEnabledPaymentTypes(getPaymentTypes(
                        PaymentType.DIRECT_DEBIT, PaymentType.INVOICE));
            }
            result.add(conf);
        }
        return result;
    }

    private static final List<VOServicePaymentConfiguration> getServiceConfiguration() {
        List<VOServicePaymentConfiguration> result = new ArrayList<VOServicePaymentConfiguration>();
        for (int i = 0; i < NUM_OF_SERVICES; i++) {
            VOServicePaymentConfiguration conf = new VOServicePaymentConfiguration();
            VOService svc = new VOService();
            svc.setKey(i);
            svc.setServiceId(String.valueOf(i));
            svc.setStatus(ServiceStatus.ACTIVE);
            conf.setService(svc);
            if (i % 2 == 0) {
                conf.setEnabledPaymentTypes(getPaymentTypes(
                        PaymentType.CREDIT_CARD, PaymentType.INVOICE));
            } else {
                conf.setEnabledPaymentTypes(getPaymentTypes(
                        PaymentType.DIRECT_DEBIT, PaymentType.INVOICE));
            }
            result.add(conf);
        }
        return result;
    }

    private static final Set<VOPaymentType> getForOrg(long key,
            List<VOOrganizationPaymentConfiguration> config) {
        Set<VOPaymentType> result = null;
        for (VOOrganizationPaymentConfiguration c : config) {
            if (c.getOrganization().getKey() == key) {
                result = c.getEnabledPaymentTypes();
                break;
            }
        }
        return result;
    }

    private static final Set<VOPaymentType> getForSvc(long key,
            List<VOServicePaymentConfiguration> config) {
        Set<VOPaymentType> result = null;
        for (VOServicePaymentConfiguration c : config) {
            if (c.getService().getKey() == key) {
                result = c.getEnabledPaymentTypes();
                break;
            }
        }
        return result;
    }
}
