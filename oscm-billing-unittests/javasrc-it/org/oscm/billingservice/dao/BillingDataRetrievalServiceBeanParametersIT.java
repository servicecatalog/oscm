/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 08.09.2011                                                      
 *                                                                              
 *  Completion Time: 08.09.2011                                             
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.junit.Test;
import org.oscm.billingservice.business.calculation.revenue.BillingInputFactory;
import org.oscm.billingservice.business.calculation.revenue.model.PriceModelInput;
import org.oscm.billingservice.dao.model.RolePricingData;
import org.oscm.billingservice.dao.model.RolePricingDetails;
import org.oscm.billingservice.dao.model.SteppedPriceDetail;
import org.oscm.billingservice.dao.model.XParameterData;
import org.oscm.billingservice.dao.model.XParameterIdData;
import org.oscm.billingservice.dao.model.XParameterOption;
import org.oscm.billingservice.dao.model.XParameterPeriodEnumType;
import org.oscm.billingservice.dao.model.XParameterPeriodPrimitiveType;
import org.oscm.billingservice.dao.model.XParameterPeriodValue;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.DomainDataContainer;
import org.oscm.domobjects.DomainHistoryObject;
import org.oscm.domobjects.ParameterDefinitionHistory;
import org.oscm.domobjects.ParameterHistory;
import org.oscm.domobjects.ParameterOptionHistory;
import org.oscm.domobjects.PriceModelHistory;
import org.oscm.domobjects.PricedOptionHistory;
import org.oscm.domobjects.PricedParameterHistory;
import org.oscm.domobjects.PricedProductRoleHistory;
import org.oscm.domobjects.RoleDefinitionHistory;
import org.oscm.domobjects.SteppedPriceHistory;
import org.oscm.domobjects.SubscriptionHistory;
import org.oscm.domobjects.UsageLicenseHistory;
import org.oscm.domobjects.enums.ModificationType;
import org.oscm.internal.types.enumtypes.ParameterType;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.enumtypes.PricingPeriod;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.test.BigDecimalAsserts;
import org.oscm.test.EJBTestBase;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;

public class BillingDataRetrievalServiceBeanParametersIT extends EJBTestBase {

    private static final String ROLE_ID = "roleId";

    private BillingDataRetrievalServiceLocal bdr;
    private DataService ds;

    private long key = 0L;
    private long version = 0L;

    private ParameterDefinitionHistory pdh;
    private ParameterHistory ph;
    private PricedParameterHistory pph;
    private PriceModelHistory pmh;
    private SubscriptionHistory sh;
    private UsageLicenseHistory ulh;
    private PricedOptionHistory poh;
    private RoleDefinitionHistory rdh;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        container.addBean(new BillingDataRetrievalServiceBean());

        ds = container.get(DataService.class);
        bdr = container.get(BillingDataRetrievalServiceLocal.class);
    }

    private PriceModelInput givenPriceModelInput(long key, long start, long end,
            PricingPeriod period) {
        PriceModelHistory history = new PriceModelHistory();
        history.setPeriod(period);
        PriceModelInput priceModelInput = new PriceModelInput(key, start, end,
                start, history, 0, false, false, false, start, false, 0, false,
                "");
        return priceModelInput;
    }

    @Test
    public void getParameterData_NoEntries() throws Exception {
        XParameterData result = runTX(new Callable<XParameterData>() {
            @Override
            public XParameterData call() throws Exception {
                PriceModelInput priceModelInput = givenPriceModelInput(1, 0, 0,
                        PricingPeriod.DAY);
                return bdr.loadParameterData(
                        BillingInputFactory.newBillingInput(0, 0),
                        priceModelInput);
            }
        });
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getCosts());
        assertEquals(PricingPeriod.DAY, result.getPeriod());
        assertTrue(result.getIdData().isEmpty());
    }

    @Test
    public void getParameterData_OneEntry() throws Exception {
        String paramId = "param1";
        ParameterValueType pvt = ParameterValueType.LONG;
        setupParameter(paramId, pvt, 30000L, "0", PricingPeriod.DAY);
        validateSingleParam(paramId, pvt);
    }

    @Test
    public void getParameterData_OneEntry_LongTypeNull() throws Exception {
        String paramId = "param1";
        ParameterValueType pvt = ParameterValueType.LONG;
        setupParameter(paramId, pvt, 30000L, null, PricingPeriod.DAY);
        validateSingleParam(paramId, pvt);
    }

    @Test
    public void getParameterData_OneEntry_IntegerTypeNull() throws Exception {
        String paramId = "param1";
        ParameterValueType pvt = ParameterValueType.INTEGER;
        setupParameter(paramId, pvt, 30000L, null, PricingPeriod.DAY);
        validateSingleParam(paramId, pvt);
    }

    @Test
    public void getParameterData_OneEntry_BooleanTypeNull() throws Exception {
        String paramId = "param1";
        ParameterValueType pvt = ParameterValueType.BOOLEAN;
        setupParameter(paramId, pvt, 30000L, null, PricingPeriod.DAY);
        validateSingleParam(paramId, pvt);
    }

    @Test
    public void getParameterData_OneEntry_StringTypeNull() throws Exception {
        String paramId = "param1";
        ParameterValueType pvt = ParameterValueType.STRING;
        setupParameter(paramId, pvt, 30000L, null, PricingPeriod.DAY);
        validateSingleParam(paramId, pvt);
    }

    @Test
    public void getParameterData_OneEntry_EnumerationTypeNull()
            throws Exception {
        String paramId = "param1";
        ParameterValueType pvt = ParameterValueType.ENUMERATION;
        setupParameter(paramId, pvt, 30000L, null, PricingPeriod.DAY);
        validateSingleParam(paramId, pvt);
    }

    @Test
    public void getParameterData_OneEntryUpdatedParamValueOutOfRange()
            throws Exception {
        String paramId = "param1";
        ParameterValueType pvt = ParameterValueType.LONG;
        setupParameter(paramId, pvt, 30000L, "0", PricingPeriod.DAY);
        updateParameter(80000L, "1234");
        validateSingleParam(paramId, pvt);
    }

    @Test
    public void getParameterData_OneEntryUpdatedPricedParamOutOfRange()
            throws Exception {
        String paramId = "param1";
        ParameterValueType pvt = ParameterValueType.LONG;
        setupParameter(paramId, pvt, 30000L, "0", PricingPeriod.DAY);
        updatePricedParameter(80000L, new BigDecimal("123.34"),
                new BigDecimal("124.45"));
        validateSingleParam(paramId, pvt);
    }

    @Test
    public void getParameterData_OneEntryUpdatedParamDefOutOfRange()
            throws Exception {
        String paramId = "param1";
        ParameterValueType pvt = ParameterValueType.LONG;
        setupParameter(paramId, pvt, 30000L, "0", PricingPeriod.DAY);
        updateParameterDefinitionHistory(80000L);
        validateSingleParam(paramId, pvt);
    }

    /**
     * Validates the expected parameter settings for the cases where only one
     * param of type long exists.
     */
    private void validateSingleParam(String paramId, ParameterValueType pvt)
            throws Exception {
        XParameterData result = runTX(new Callable<XParameterData>() {
            @Override
            public XParameterData call() throws Exception {
                PriceModelInput priceModelInput = givenPriceModelInput(
                        pmh.getObjKey(), 10000, 70000, PricingPeriod.DAY);
                return bdr.loadParameterData(BillingInputFactory
                        .newBillingInput(0, 0, sh.getObjKey()),
                        priceModelInput);
            }
        });
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getCosts());
        assertEquals(PricingPeriod.DAY, result.getPeriod());
        assertEquals(1, result.getIdData().size());
        XParameterIdData entry = result.getIdData().iterator().next();
        assertEquals(paramId, entry.getId());
        assertEquals(ParameterType.PLATFORM_PARAMETER, entry.getType());
        assertEquals(pvt, entry.getValueType());
    }

    @Test
    public void getParameterData_OneEntryValidatePeriodValue()
            throws Exception {
        String paramId = "param1";
        ParameterValueType pvt = ParameterValueType.LONG;
        setupParameter(paramId, pvt, 30000L, "0", PricingPeriod.DAY);
        XParameterData result = runTX(new Callable<XParameterData>() {
            @Override
            public XParameterData call() throws Exception {
                PriceModelInput priceModelInput = givenPriceModelInput(
                        pmh.getObjKey(), 10000, 43230000, PricingPeriod.DAY);
                return bdr.loadParameterData(BillingInputFactory
                        .newBillingInput(0, 0, sh.getObjKey()),
                        priceModelInput);
            }
        });
        assertEquals(1, result.getIdData().size());
        List<XParameterPeriodValue> periodValues = result.getIdData().iterator()
                .next().getPeriodValues();
        assertEquals(1, periodValues.size());
        XParameterPeriodValue periodValue = periodValues.get(0);
        assertEquals("0", periodValue.getValue());
        assertEquals(BigDecimal.ZERO, periodValue.getUserAssignmentCosts());
        assertEquals(BigDecimal.ZERO, periodValue.getPeriodCosts());
        assertEquals(BigDecimal.ZERO, periodValue.getTotalCosts());
        assertEquals(0, periodValue.getPeriodFactor(), 0);
        assertEquals(0.5, periodValue.getUserAssignmentFactor(), 0);
        BigDecimalAsserts.checkEquals(new BigDecimal("20.00"),
                periodValue.getPricePerSubscription(), 0);
        BigDecimalAsserts.checkEquals(new BigDecimal("30.00"),
                periodValue.getPricePerUser(), 0);
        assertEquals(30000, periodValue.getStartTime());
        assertEquals(43230000, periodValue.getEndTime());
        assertNotNull(periodValue.getKey());
        assertEquals(pph.getObjKey(), periodValue.getKey().longValue());
    }

    @Test
    public void getParameterData_OneEntryPrimitiveTypeValueChange()
            throws Exception {
        String paramId = "param1";
        ParameterValueType pvt = ParameterValueType.LONG;
        setupParameter(paramId, pvt, 5000L, "0", PricingPeriod.HOUR);
        updateParameter(40000L, "50");
        XParameterData result = runTX(new Callable<XParameterData>() {
            @Override
            public XParameterData call() throws Exception {
                PriceModelInput priceModelInput = givenPriceModelInput(
                        pmh.getObjKey(), 10000, 1840000, PricingPeriod.HOUR);
                return bdr.loadParameterData(BillingInputFactory
                        .newBillingInput(0, 0, sh.getObjKey()),
                        priceModelInput);
            }
        });
        Collection<XParameterIdData> idData = result.getIdData();
        assertEquals(1, idData.size());
        List<XParameterPeriodValue> periodValues = idData.iterator().next()
                .getPeriodValues();
        assertEquals(2, periodValues.size());
        XParameterPeriodValue pv1 = periodValues.get(0);
        assertEquals("50", pv1.getValue());
        assertEquals(40000, pv1.getStartTime());
        assertEquals(1840000, pv1.getEndTime());
        BigDecimalAsserts.checkEquals(new BigDecimal("30"),
                pv1.getPricePerUser(), 2);
        BigDecimalAsserts.checkEquals(new BigDecimal("20"),
                pv1.getPricePerSubscription(), 2);
        assertEquals(0, pv1.getPeriodFactor(), 0);
        assertEquals(0.5, pv1.getUserAssignmentFactor(), 0);
        BigDecimalAsserts.checkEquals(BigDecimal.ZERO, pv1.getPeriodCosts(), 2);
        BigDecimalAsserts.checkEquals(BigDecimal.ZERO,
                pv1.getUserAssignmentCosts(), 2);
        BigDecimalAsserts.checkEquals(BigDecimal.ZERO, pv1.getTotalCosts(), 2);
        assertEquals(null, pv1.getParameterOption());
        XParameterPeriodValue pv2 = periodValues.get(1);
        assertEquals("0", pv2.getValue());
        assertEquals(10000, pv2.getStartTime());
        assertEquals(40000, pv2.getEndTime());
        BigDecimalAsserts.checkEquals(new BigDecimal("30"),
                pv2.getPricePerUser(), 2);
        BigDecimalAsserts.checkEquals(new BigDecimal("20"),
                pv2.getPricePerSubscription(), 2);
        assertEquals(0, pv2.getPeriodFactor(), 0);
        assertEquals(0.0083, pv2.getUserAssignmentFactor(), 0.0001);
        BigDecimalAsserts.checkEquals(BigDecimal.ZERO, pv2.getPeriodCosts(), 2);
        BigDecimalAsserts.checkEquals(BigDecimal.ZERO,
                pv2.getUserAssignmentCosts(), 2);
        BigDecimalAsserts.checkEquals(BigDecimal.ZERO, pv2.getTotalCosts(), 2);
        assertEquals(null, pv2.getParameterOption());
    }

    @Test
    public void getParameterData_OneEntryPrimitiveTypeValueChange_CheckEntryCount()
            throws Exception {
        String paramId = "param1";
        ParameterValueType pvt = ParameterValueType.LONG;
        setupParameter(paramId, pvt, 5000L, "0", PricingPeriod.HOUR);
        updateParameter(40000L, "40");
        createRolePrice(1840000L, Long.valueOf(pph.getKey()), null);
        createRoleDefinitionHistory(10000L);
        createRolePrice(1840000L, Long.valueOf(pph.getKey()), null);
        updateParameter(1840000L, "60");
        XParameterData result = runTX(new Callable<XParameterData>() {
            @Override
            public XParameterData call() throws Exception {
                PriceModelInput priceModelInput = givenPriceModelInput(
                        pmh.getObjKey(), 5000, 1840000, PricingPeriod.HOUR);
                return bdr.loadParameterData(BillingInputFactory
                        .newBillingInput(0, 0, sh.getObjKey()),
                        priceModelInput);
            }
        });
        Collection<XParameterIdData> idData = result.getIdData();
        assertEquals(1, idData.size());
        List<XParameterPeriodValue> periodValues = idData.iterator().next()
                .getPeriodValues();
        assertEquals(3, periodValues.size());
        XParameterPeriodValue periodValue = periodValues.get(0);
        assertEquals("60", periodValue.getValue());
        assertEquals(1840000L, periodValue.getEndTime());
        assertEquals(1840000L, periodValue.getStartTime());
        periodValue = periodValues.get(1);
        assertEquals("40", periodValue.getValue());
        assertEquals(1840000L, periodValue.getEndTime());
        assertEquals(40000L, periodValue.getStartTime());
        assertEquals(0, periodValue.getPeriodFactor(), 0.0);
        periodValue = periodValues.get(2);
        assertEquals("0", periodValue.getValue());
        assertEquals(40000L, periodValue.getEndTime());
        assertEquals(5000L, periodValue.getStartTime());
        assertEquals(0, periodValue.getPeriodFactor(), 0);
    }

    @Test
    public void getParameterData_OneEntryPrimitiveTypeValueChange_TempParValue()
            throws Exception {
        String paramId = "param1";
        ParameterValueType pvt = ParameterValueType.LONG;
        setupParameter(paramId, pvt, 5000L, "0", PricingPeriod.HOUR);
        updateParameter(5000L, "29");
        updateParameter(40000L, "40");
        createRolePrice(1840000L, Long.valueOf(pph.getKey()), null);
        createRoleDefinitionHistory(10000L);
        createRolePrice(1840000L, Long.valueOf(pph.getKey()), null);
        updateParameter(1840000L, "60");
        XParameterData result = runTX(new Callable<XParameterData>() {
            @Override
            public XParameterData call() throws Exception {
                PriceModelInput priceModelInput = givenPriceModelInput(
                        pmh.getObjKey(), 5000, 1900000, PricingPeriod.HOUR);
                return bdr.loadParameterData(BillingInputFactory
                        .newBillingInput(0, 0, sh.getObjKey()),
                        priceModelInput);
            }
        });
        Collection<XParameterIdData> idData = result.getIdData();
        assertEquals(1, idData.size());
        List<XParameterPeriodValue> periodValues = idData.iterator().next()
                .getPeriodValues();
        assertEquals(3, periodValues.size());
        XParameterPeriodValue periodValue = periodValues.get(0);
        assertEquals("60", periodValue.getValue());
        assertEquals(1900000L, periodValue.getEndTime());
        assertEquals(1840000L, periodValue.getStartTime());
        periodValue = periodValues.get(1);
        assertEquals("40", periodValue.getValue());
        assertEquals(1840000L, periodValue.getEndTime());
        assertEquals(40000L, periodValue.getStartTime());
        assertEquals(0, periodValue.getPeriodFactor(), 0.0);
        periodValue = periodValues.get(2);
        assertEquals("29", periodValue.getValue());
        assertEquals(40000L, periodValue.getEndTime());
        assertEquals(5000L, periodValue.getStartTime());
        assertEquals(0, periodValue.getPeriodFactor(), 0);
    }

    @Test
    public void getParameterData_OneEntryPrimitiveTypeValueUnchanged_CheckEntryCount()
            throws Exception {
        String paramId = "param1";
        ParameterValueType pvt = ParameterValueType.LONG;
        setupParameter(paramId, pvt, 5000L, "40", PricingPeriod.HOUR);
        updateParameter(40000L, "40");
        createRolePrice(1840000L, Long.valueOf(pph.getKey()), null);
        createRoleDefinitionHistory(10000L);
        createRolePrice(1840000L, Long.valueOf(pph.getKey()), null);
        updateParameter(1840000L, "40");
        XParameterData result = runTX(new Callable<XParameterData>() {
            @Override
            public XParameterData call() throws Exception {
                PriceModelInput priceModelInput = givenPriceModelInput(
                        pmh.getObjKey(), 5000, 1840000, PricingPeriod.HOUR);
                return bdr.loadParameterData(BillingInputFactory
                        .newBillingInput(0, 0, sh.getObjKey()),
                        priceModelInput);
            }
        });
        Collection<XParameterIdData> idData = result.getIdData();
        assertEquals(1, idData.size());
        List<XParameterPeriodValue> periodValues = idData.iterator().next()
                .getPeriodValues();
        assertEquals(1, periodValues.size());
        XParameterPeriodValue periodValue = periodValues.get(0);
        assertEquals("40", periodValue.getValue());
        assertEquals(1840000L, periodValue.getEndTime());
        assertEquals(5000L, periodValue.getStartTime());
        assertEquals(0, periodValue.getPeriodFactor(), 0);
    }

    @Test
    public void getParameterData_TwoPrimitiveTypesValueUnchanged_CheckEntryCount()
            throws Exception {
        String paramId1 = "param1";
        String paramId2 = "param2";
        ParameterValueType pvt = ParameterValueType.LONG;
        setupParameter(paramId1, pvt, 5000L, "40", PricingPeriod.HOUR);
        setupParameter(paramId2, pvt, 5000L, "40", PricingPeriod.HOUR);
        updateParameter(40000L, "40");
        createRolePrice(1840000L, Long.valueOf(pph.getKey()), null);
        createRoleDefinitionHistory(10000L);
        createRolePrice(1840000L, Long.valueOf(pph.getKey()), null);
        updateParameter(1840000L, "40");
        XParameterData result = runTX(new Callable<XParameterData>() {
            @Override
            public XParameterData call() throws Exception {
                PriceModelInput priceModelInput = givenPriceModelInput(
                        pmh.getObjKey(), 5000, 1840000, PricingPeriod.HOUR);
                return bdr.loadParameterData(BillingInputFactory
                        .newBillingInput(0, 0, sh.getObjKey()),
                        priceModelInput);
            }
        });
        Collection<XParameterIdData> idData = result.getIdData();
        assertEquals(2, idData.size());
        List<XParameterPeriodValue> periodValues = idData.iterator().next()
                .getPeriodValues();
        assertEquals(1, periodValues.size());
        XParameterPeriodValue periodValue = periodValues.get(0);
        assertEquals("40", periodValue.getValue());
        assertEquals(1840000L, periodValue.getEndTime());
        assertEquals(5000L, periodValue.getStartTime());
        assertEquals(0, periodValue.getPeriodFactor(), 0);
        periodValues = idData.iterator().next().getPeriodValues();
        assertEquals(1, periodValues.size());
        periodValue = periodValues.get(0);
        assertEquals("40", periodValue.getValue());
        assertEquals(1840000L, periodValue.getEndTime());
        assertEquals(5000L, periodValue.getStartTime());
        assertEquals(0, periodValue.getPeriodFactor(), 0);
    }

    @Test
    public void getParameterData_OneEntryPrimitiveTypeDeleted_CheckEntryCount()
            throws Exception {
        String paramId = "param1";
        ParameterValueType pvt = ParameterValueType.LONG;
        setupParameter(paramId, pvt, 5000L, "0", PricingPeriod.HOUR);
        updateParameter(40000L, "40");
        createRolePrice(1840000L, Long.valueOf(pph.getKey()), null);
        createRoleDefinitionHistory(10000L);
        createRolePrice(1840000L, Long.valueOf(pph.getKey()), null);
        deleteParameter(1840000L, "40");
        XParameterData result = runTX(new Callable<XParameterData>() {
            @Override
            public XParameterData call() throws Exception {
                PriceModelInput priceModelInput = givenPriceModelInput(
                        pmh.getObjKey(), 5000, 1840000, PricingPeriod.HOUR);
                return bdr.loadParameterData(BillingInputFactory
                        .newBillingInput(0, 0, sh.getObjKey()),
                        priceModelInput);
            }
        });
        Collection<XParameterIdData> idData = result.getIdData();
        assertEquals(1, idData.size());
        List<XParameterPeriodValue> periodValues = idData.iterator().next()
                .getPeriodValues();
        assertEquals(2, periodValues.size());
        XParameterPeriodValue periodValue = periodValues.get(0);
        assertEquals("40", periodValue.getValue());
        assertEquals(1840000L, periodValue.getEndTime());
        assertEquals(40000L, periodValue.getStartTime());
        assertEquals(0, periodValue.getPeriodFactor(), 0);
        periodValue = periodValues.get(1);
        assertEquals("0", periodValue.getValue());
        assertEquals(40000L, periodValue.getEndTime());
        assertEquals(5000L, periodValue.getStartTime());
        assertEquals(0, periodValue.getPeriodFactor(), 0);
    }

    @Test
    public void getParameterData_TwoParams() throws Exception {
        String paramId1 = "param1";
        String paramId2 = "param2";
        ParameterValueType pvt = ParameterValueType.LONG;
        setupParameter(paramId1, pvt, 30000L, "0", PricingPeriod.DAY);
        setupParameter(paramId2, pvt, 30000L, "0", PricingPeriod.DAY);
        XParameterData result = runTX(new Callable<XParameterData>() {
            @Override
            public XParameterData call() throws Exception {
                PriceModelInput priceModelInput = givenPriceModelInput(
                        pmh.getObjKey(), 10000, 70000, PricingPeriod.HOUR);
                return bdr.loadParameterData(BillingInputFactory
                        .newBillingInput(0, 0, sh.getObjKey()),
                        priceModelInput);
            }
        });
        assertEquals(2, result.getIdData().size());
        Iterator<XParameterIdData> iterator = result.getIdData().iterator();
        Set<String> idsToBeContained = new HashSet<>();
        idsToBeContained.add(paramId1);
        idsToBeContained.add(paramId2);
        while (iterator.hasNext()) {
            idsToBeContained.remove(iterator.next().getId());
        }
        assertTrue(idsToBeContained.isEmpty());
    }

    @Test
    public void getParameterData_OneParamVerifyUsageCosts() throws Exception {
        setupParameter("paramId1", ParameterValueType.LONG, 30000L, "0",
                PricingPeriod.HOUR);
        addUsageLicenseHistory(930000);
        XParameterData result = runTX(new Callable<XParameterData>() {
            @Override
            public XParameterData call() throws Exception {
                PriceModelInput priceModelInput = givenPriceModelInput(
                        pmh.getObjKey(), 10000, 1830000, PricingPeriod.HOUR);
                return bdr.loadParameterData(BillingInputFactory
                        .newBillingInput(0, 0, sh.getObjKey()),
                        priceModelInput);
            }
        });
        Iterator<XParameterIdData> iterator = result.getIdData().iterator();
        assertEquals(0.75, iterator.next().getPeriodValues().get(0)
                .getUserAssignmentFactor(), 0);
    }

    @Test
    public void getParameterData_primitiveTypeRoleCosts() throws Exception {
        setupParameter("paramId1", ParameterValueType.LONG, 1317190847297L,
                "25", PricingPeriod.HOUR);
        createRolePrice(1317190847297L, Long.valueOf(pph.getObjKey()), null);
        updateParameter(1317191030646L, "35");
        updateParameter(1317191230646L, "35");
        XParameterData result = runTX(new Callable<XParameterData>() {
            @Override
            public XParameterData call() throws Exception {
                PriceModelInput priceModelInput = givenPriceModelInput(
                        pmh.getObjKey(), 1317190847297L, 1317191307633L,
                        PricingPeriod.HOUR);
                return bdr.loadParameterData(BillingInputFactory
                        .newBillingInput(0, 0, sh.getObjKey()),
                        priceModelInput);
            }
        });
        XParameterIdData idData = result.getIdData().iterator().next();
        List<XParameterPeriodValue> periodValues = idData.getPeriodValues();
        assertEquals(2, periodValues.size());
        XParameterPeriodValue xParameterPeriodValue = periodValues.get(0);
        assertTrue(
                xParameterPeriodValue instanceof XParameterPeriodPrimitiveType);
        XParameterPeriodPrimitiveType periodValue = (XParameterPeriodPrimitiveType) xParameterPeriodValue;
        assertEquals("35", periodValue.getValue());
        RolePricingData rolePrices = periodValue.getRolePrices();
        Set<Long> containerKeys = rolePrices.getContainerKeys();
        assertEquals(1, containerKeys.size());
        Collection<RolePricingDetails> allRolePrices = rolePrices
                .getAllRolePrices(containerKeys.iterator().next());
        assertEquals(1, allRolePrices.size());
        RolePricingDetails pricingDetails = allRolePrices.iterator().next();
        assertEquals(0.07694, pricingDetails.getFactor(), 0.00001);
        // assertEquals(rolePrices.)
        periodValue = (XParameterPeriodPrimitiveType) periodValues.get(1);
        assertEquals("25", periodValue.getValue());
        rolePrices = periodValue.getRolePrices();
        containerKeys = rolePrices.getContainerKeys();
        assertEquals(1, containerKeys.size());
        allRolePrices = rolePrices
                .getAllRolePrices(containerKeys.iterator().next());
        assertEquals(1, allRolePrices.size());
        pricingDetails = allRolePrices.iterator().next();
        assertEquals(0.05093, pricingDetails.getFactor(), 0.00001);
    }

    @Test
    public void getParameterData_OneParamVerifyUsageCostsAndValueChange()
            throws Exception {
        setupParameter("paramId1", ParameterValueType.LONG, 30000L, "0",
                PricingPeriod.HOUR);
        addUsageLicenseHistory(255000);
        updateParameter(480000L, "50");
        XParameterData result = runTX(new Callable<XParameterData>() {
            @Override
            public XParameterData call() throws Exception {
                PriceModelInput priceModelInput = givenPriceModelInput(
                        pmh.getObjKey(), 10000, 1830000, PricingPeriod.HOUR);
                return bdr.loadParameterData(BillingInputFactory
                        .newBillingInput(0, 0, sh.getObjKey()),
                        priceModelInput);
            }
        });
        Iterator<XParameterIdData> iterator = result.getIdData().iterator();
        assertEquals(1, result.getIdData().size());
        List<XParameterPeriodValue> periodValues = iterator.next()
                .getPeriodValues();
        assertEquals(2, periodValues.size());
        XParameterPeriodValue periodValue = periodValues.get(0);
        assertEquals(1830000, periodValue.getEndTime());
        assertEquals(480000, periodValue.getStartTime());
        assertEquals(0.75, periodValue.getUserAssignmentFactor(), 0);
        periodValue = periodValues.get(1);
        assertEquals(480000, periodValue.getEndTime());
        assertEquals(30000, periodValue.getStartTime());
        assertEquals(0.1875, periodValue.getUserAssignmentFactor(), 0);
    }

    /**
     * Create a parameter of type long with basic period HOUR, where the
     * parameter is
     * <ul>
     * <li>created with value 10 at 1324512050000ms</li>
     * <li>updated with value 40 at 1324513850000ms</li>
     * <li>updated with value 40 at 1324514750000ms</li>
     * <li>deleted with value 40 at 1324515650000ms</li>
     * </ul>
     * Furthermore one user is assigned the entire period, another user is
     * assigned as follows
     * <ul>
     * <li>assigned for user at time 1324512850000ms</li>
     * <li>de-assigned for user at time 1324512900000ms</li>
     * <li>assigned for user at time 1324513000000ms</li>
     * </ul>
     * The entire period is from 1324512050000ms to 1324515750000ms.
     * 
     * @throws Exception
     */
    @Test
    public void getParameterData_OneParamWithValueChange_CheckUserFactors()
            throws Exception {
        setupParameter("paramId1", ParameterValueType.LONG, 1324512050000L,
                "10", PricingPeriod.HOUR);
        addUsageLicenseHistory(1324512850000L);
        deleteUsageLicenseHistory(1324512900000L);
        addUsageLicenseHistory(1324513000000L);
        updateParameter(1324513850000L, "40");
        updateParameter(1324514750000L, "40");
        deleteParameter(1324515650000L, "40");
        XParameterData result = runTX(new Callable<XParameterData>() {
            @Override
            public XParameterData call() throws Exception {
                PriceModelInput priceModelInput = givenPriceModelInput(
                        pmh.getObjKey(), 1324512050000L, 1324515750000L,
                        PricingPeriod.HOUR);
                return bdr.loadParameterData(BillingInputFactory
                        .newBillingInput(0, 0, sh.getObjKey()),
                        priceModelInput);
            }
        });
        assertEquals(1, result.getIdData().size());
        XParameterIdData idData = result.getIdData().iterator().next();
        List<XParameterPeriodValue> periodValues = idData.getPeriodValues();
        assertEquals(2, periodValues.size());
        XParameterPeriodValue periodValue = periodValues.get(0);
        assertEquals(1, periodValue.getUserAssignmentFactor(), 0);
        periodValue = periodValues.get(1);
        assertEquals(0.75, periodValue.getUserAssignmentFactor(), 0);
    }

    /**
     * Create a parameter of type long with basic period HOUR, where the
     * parameter is
     * <ul>
     * <li>created with value 10 at 50.000ms</li>
     * <li>updated with value null at 1.850.000ms</li>
     * <li>updated with value 40 at 2.750.000ms</li>
     * <li>updated with value null at 3.550.000ms</li>
     * </ul>
     * Furthermore one user is assigned the entire period. The entire period is
     * from 50.000ms to 3.650.000ms.
     * 
     * @throws Exception
     */
    @Test
    public void getParameterData_OneParamWithValueNullChange()
            throws Exception {
        setupParameter("paramId1", ParameterValueType.LONG, 50000L, "10",
                PricingPeriod.HOUR);
        updateParameter(1850000L, null);
        updateParameter(2750000L, "40");
        updateParameter(3550000L, null);
        addUsageLicenseHistory(850000);
        deleteUsageLicenseHistory(900000L);
        addUsageLicenseHistory(1000000);
        XParameterData result = runTX(new Callable<XParameterData>() {
            @Override
            public XParameterData call() throws Exception {
                PriceModelInput priceModelInput = givenPriceModelInput(
                        pmh.getObjKey(), 50000, 3650000L, PricingPeriod.HOUR);
                return bdr.loadParameterData(BillingInputFactory
                        .newBillingInput(0, 0, sh.getObjKey()),
                        priceModelInput);
            }
        });

        assertEquals(1, result.getIdData().size());
        XParameterIdData idData = result.getIdData().iterator().next();
        List<XParameterPeriodValue> periodValues = idData.getPeriodValues();
        assertEquals(2, periodValues.size());
        XParameterPeriodValue periodValue = periodValues.get(0);
        assertEquals("40", periodValue.getValue());
        assertEquals(2750000, periodValue.getStartTime());
        assertEquals(3550000, periodValue.getEndTime());
        assertEquals(0, periodValue.getPeriodFactor(), 0);
        assertEquals(0.44444, periodValue.getUserAssignmentFactor(), 0.00001);
        periodValue = periodValues.get(1);
        assertEquals("10", periodValue.getValue());
        assertEquals(50000, periodValue.getStartTime());
        assertEquals(1850000, periodValue.getEndTime());
        assertEquals(0, periodValue.getPeriodFactor(), 0);
        assertEquals(0.75, periodValue.getUserAssignmentFactor(), 0);
    }

    @Test
    public void getParameterData_verifySteppedPrices() throws Exception {
        setupParameter("paramId1", ParameterValueType.LONG, 30000L, "0",
                PricingPeriod.HOUR);
        createSteppedPrices(30000);
        XParameterData result = runTX(new Callable<XParameterData>() {
            @Override
            public XParameterData call() throws Exception {
                PriceModelInput priceModelInput = givenPriceModelInput(
                        pmh.getObjKey(), 10000, 1830000, PricingPeriod.HOUR);
                return bdr.loadParameterData(BillingInputFactory
                        .newBillingInput(0, 0, sh.getObjKey()),
                        priceModelInput);

            }
        });
        Iterator<XParameterIdData> iterator = result.getIdData().iterator();
        SteppedPriceDetail steppedPrices = iterator.next().getPeriodValues()
                .get(0).getSteppedPricesForParameter();
        BigDecimalAsserts.checkEquals(BigDecimal.ZERO,
                steppedPrices.getNormalizedCost());
        assertEquals(1, steppedPrices.getPriceData().size());
        BigDecimalAsserts.checkEquals(new BigDecimal("40"),
                steppedPrices.getPriceData().get(0).getBasePrice());
    }

    @Test
    public void getParameterData_verifyRoleCostsOnPrimitiveParam()
            throws Exception {
        setupParameter("paramId1", ParameterValueType.LONG, 30000L, "0",
                PricingPeriod.HOUR);
        createRolePrice(30000, Long.valueOf(pph.getObjKey()), null);
        XParameterData result = runTX(new Callable<XParameterData>() {
            @Override
            public XParameterData call() throws Exception {
                PriceModelInput priceModelInput = givenPriceModelInput(
                        pmh.getObjKey(), 10000, 1830000, PricingPeriod.HOUR);
                return bdr.loadParameterData(BillingInputFactory
                        .newBillingInput(0, 0, sh.getObjKey()),
                        priceModelInput);
            }
        });
        Iterator<XParameterIdData> iterator = result.getIdData().iterator();
        RolePricingData rolePricingData = iterator.next().getPeriodValues()
                .get(0).getRolePrices();
        Set<Long> containerKeys = rolePricingData.getContainerKeys();
        assertEquals(1, containerKeys.size());
        Map<Long, RolePricingDetails> prices = rolePricingData
                .getRolePricesForContainerKey(Long.valueOf(pph.getObjKey()));
        RolePricingDetails rolePricingDetails = prices
                .get(Long.valueOf(rdh.getObjKey()));
        BigDecimal pricePerUser = rolePricingDetails.getPricePerUser();
        BigDecimalAsserts.checkEquals(new BigDecimal("99.99"), pricePerUser);
        assertEquals(ROLE_ID, rolePricingDetails.getRoleId());
    }

    @Test
    public void getParameterData_enumType() throws Exception {
        setupParameter("paramId1", ParameterValueType.ENUMERATION, 30000L, "0",
                PricingPeriod.HOUR);
        createRolePrice(30000, null, Long.valueOf(poh.getObjKey()));
        XParameterData result = runTX(new Callable<XParameterData>() {
            @Override
            public XParameterData call() throws Exception {
                PriceModelInput priceModelInput = givenPriceModelInput(
                        pmh.getObjKey(), 10000, 1830000, PricingPeriod.HOUR);
                return bdr.loadParameterData(BillingInputFactory
                        .newBillingInput(0, 0, sh.getObjKey()),
                        priceModelInput);
            }
        });
        XParameterIdData idData = result.getIdData().iterator().next();
        List<XParameterPeriodValue> periodValues = idData.getPeriodValues();
        assertEquals(1, periodValues.size());
        XParameterPeriodValue xParameterPeriodValue = periodValues.get(0);
        assertTrue(xParameterPeriodValue instanceof XParameterPeriodEnumType);
        XParameterPeriodEnumType enumType = (XParameterPeriodEnumType) xParameterPeriodValue;
        assertEquals(0, enumType.getPeriodFactor(), 0);
        assertEquals(0.5, enumType.getUserAssignmentFactor(), 0);
        assertEquals(30000, enumType.getStartTime());
        assertEquals(1830000, enumType.getEndTime());
        BigDecimalAsserts.checkEquals(new BigDecimal("200"),
                enumType.getPricePerSubscription());
        BigDecimalAsserts.checkEquals(new BigDecimal("300"),
                enumType.getPricePerUser());
        assertEquals(poh.getObjKey(), enumType.getKey().longValue());
        XParameterOption parameterOption = enumType.getParameterOption();
        assertEquals(ph.getValue(), parameterOption.getId());
        RolePricingData rolePrices = parameterOption.getRolePrices();
        assertNotNull(rolePrices);
        Map<Long, RolePricingDetails> rolePricesForOption = rolePrices
                .getRolePricesForContainerKey(Long.valueOf(poh.getObjKey()));
        RolePricingDetails rolePricingDetails = rolePricesForOption
                .get(Long.valueOf(rdh.getObjKey()));
        BigDecimal pricePerUser = rolePricingDetails.getPricePerUser();
        BigDecimalAsserts.checkEquals(new BigDecimal("99.99"), pricePerUser);
        assertEquals(ROLE_ID, rolePricingDetails.getRoleId());
    }

    @Test
    public void getParameterData_enumTypeRoleCosts() throws Exception {
        setupParameter("paramId1", ParameterValueType.ENUMERATION,
                1317190847297L, "option1", PricingPeriod.HOUR);
        createRolePrice(1317190847297L, null, Long.valueOf(poh.getObjKey()));
        PricedOptionHistory option = createParameterOption(1317190847297L,
                "option2");
        createRolePrice(1317190847297L, null, Long.valueOf(option.getObjKey()));
        updateParameter(1317191030646L, "option2");
        updateParameter(1317191230646L, "option2");
        XParameterData result = runTX(new Callable<XParameterData>() {
            @Override
            public XParameterData call() throws Exception {
                PriceModelInput priceModelInput = givenPriceModelInput(
                        pmh.getObjKey(), 1317190847297L, 1317191307633L,
                        PricingPeriod.HOUR);
                return bdr.loadParameterData(BillingInputFactory
                        .newBillingInput(0, 0, sh.getObjKey()),
                        priceModelInput);
            }
        });
        XParameterIdData idData = result.getIdData().iterator().next();
        List<XParameterPeriodValue> periodValues = idData.getPeriodValues();
        assertEquals(2, periodValues.size());
        XParameterPeriodValue xParameterPeriodValue = periodValues.get(0);
        assertTrue(xParameterPeriodValue instanceof XParameterPeriodEnumType);
        XParameterPeriodEnumType enumType = (XParameterPeriodEnumType) xParameterPeriodValue;
        assertEquals("option2", enumType.getParameterOption().getId());
        RolePricingData rolePrices = enumType.getRolePrices();
        Collection<RolePricingDetails> allRolePrices = rolePrices
                .getAllRolePrices(enumType.getKey());
        assertEquals(1, allRolePrices.size());
        RolePricingDetails pricingDetails = allRolePrices.iterator().next();
        assertEquals(0.07694, pricingDetails.getFactor(), 0.00001);
        // assertEquals(rolePrices.)
        enumType = (XParameterPeriodEnumType) periodValues.get(1);
        assertEquals("option1", enumType.getParameterOption().getId());
        rolePrices = enumType.getRolePrices();
        allRolePrices = rolePrices.getAllRolePrices(enumType.getKey());
        assertEquals(1, allRolePrices.size());
        pricingDetails = allRolePrices.iterator().next();
        assertEquals(0.05093, pricingDetails.getFactor(), 0.00001);
    }

    /**
     * Create a parameter of type long with basic period HOUR, where the
     * parameter is
     * <ul>
     * <li>created with value option1 at 50.000ms</li>
     * <li>updated with value option2 at 1.850.000ms</li>
     * <li>updated with value option2 at 2.750.000ms</li>
     * <li>deleted with value option2 at 3.650.000ms</li>
     * </ul>
     * Furthermore one user is assigned the entire period, another user is
     * assigned as follows
     * <ul>
     * <li>assigned for user at time 850.000ms</li>
     * <li>de-assigned for user at time 900.000ms</li>
     * <li>assigned for user at time 1.000.000ms</li>
     * </ul>
     * The entire period is from 50.000ms to 3.750.000ms.
     * 
     * @throws Exception
     */
    @Test
    public void getParameterData_OneEnumParamWithValueChange_CheckUserFactors_NoRolePricesDefined()
            throws Exception {
        setupParameter("paramId1", ParameterValueType.ENUMERATION, 50000,
                "option1", PricingPeriod.HOUR);
        createParameterOption(50000, "option2");
        addUsageLicenseHistory(850000);
        deleteUsageLicenseHistory(900000L);
        addUsageLicenseHistory(1000000);
        updateParameter(1850000L, "option2");
        updateParameter(2750000L, "option2");
        deleteParameter(3650000L, "option2");
        XParameterData result = runTX(new Callable<XParameterData>() {
            @Override
            public XParameterData call() throws Exception {
                PriceModelInput priceModelInput = givenPriceModelInput(
                        pmh.getObjKey(), 50000, 3750000L, PricingPeriod.HOUR);
                return bdr.loadParameterData(BillingInputFactory
                        .newBillingInput(0, 0, sh.getObjKey()),
                        priceModelInput);
            }
        });
        assertEquals(1, result.getIdData().size());
        XParameterIdData idData = result.getIdData().iterator().next();
        List<XParameterPeriodValue> periodValues = idData.getPeriodValues();
        assertEquals(2, periodValues.size());
        XParameterPeriodValue periodValue = periodValues.get(0);
        assertEquals(1, periodValue.getUserAssignmentFactor(), 0);
        periodValue = periodValues.get(1);
        assertEquals(0.75, periodValue.getUserAssignmentFactor(), 0);
    }

    private void setupParameter(final String paramId,
            final ParameterValueType pvt, final long modDate,
            final String value, final PricingPeriod pricingPeriod)
            throws Exception {
        runTX(new Callable<Void>() {
            private ParameterOptionHistory paramOptHist;

            @Override
            public Void call() throws Exception {
                Date date = new Date(modDate);
                // create pricemodelhistory and subscriptionhistory
                if (pmh == null) {
                    pmh = new PriceModelHistory();
                    initDOH(pmh, date);
                    pmh.setPeriod(pricingPeriod);
                    // period will be ignored by parameter evaluation
                    pmh.setOneTimeFee(BigDecimal.ZERO);
                    pmh.setPricePerPeriod(BigDecimal.ZERO);
                    pmh.setPricePerUserAssignment(BigDecimal.ZERO);
                    pmh.setProductObjKey(++key);
                    pmh.getDataContainer().setType(PriceModelType.PRO_RATA);
                    ds.persist(pmh);
                }

                if (sh == null) {
                    sh = new SubscriptionHistory();
                    initDOH(sh, date);
                    sh.getDataContainer()
                            .setCreationDate(Long.valueOf(date.getTime()));
                    sh.getDataContainer().setStatus(SubscriptionStatus.ACTIVE);
                    sh.getDataContainer()
                            .setSubscriptionId(String.valueOf(key));
                    sh.setCutOffDay(1);
                    ds.persist(sh);
                }

                pdh = new ParameterDefinitionHistory();
                initDOH(pdh, date);
                pdh.getDataContainer().setParameterId(paramId);
                pdh.getDataContainer()
                        .setParameterType(ParameterType.PLATFORM_PARAMETER);
                pdh.getDataContainer().setValueType(pvt);
                ds.persist(pdh);

                ph = new ParameterHistory();
                initDOH(ph, date);
                ph.setParameterDefinitionObjKey(pdh.getObjKey());
                ph.getDataContainer().setValue(value);
                ds.persist(ph);

                pph = new PricedParameterHistory();
                initDOH(pph, date);
                pph.setPriceModelObjKey(pmh.getObjKey());
                pph.setParameterObjKey(ph.getObjKey());
                pph.setPricePerSubscription(new BigDecimal("20"));
                pph.setPricePerUser(new BigDecimal("30"));
                ds.persist(pph);

                if (pvt == ParameterValueType.ENUMERATION) {
                    paramOptHist = new ParameterOptionHistory();
                    initDOH(paramOptHist, date);
                    paramOptHist.setParameterDefObjKey(++key);
                    String optionId = (ph.getValue() != null) ? ph.getValue()
                            : "optionId";
                    paramOptHist.getDataContainer().setOptionId(optionId);
                    ds.persist(paramOptHist);
                    // create option if required
                    poh = new PricedOptionHistory();
                    initDOH(poh, date);
                    poh.setParameterOptionObjKey(paramOptHist.getObjKey());
                    poh.setPricedParameterObjKey(pph.getObjKey());
                    poh.setPricePerSubscription(new BigDecimal("200"));
                    poh.setPricePerUser(new BigDecimal("300"));
                    ds.persist(poh);
                }

                rdh = new RoleDefinitionHistory();
                initDOH(rdh, date);
                rdh.getDataContainer().setRoleId(ROLE_ID);
                ds.persist(rdh);

                ulh = new UsageLicenseHistory();
                initDOH(ulh, date);
                ulh.setSubscriptionObjKey(sh.getObjKey());
                ulh.setUserObjKey(++key);
                ulh.getDataContainer().setAssignmentDate(date.getTime());
                ulh.setRoleDefinitionObjKey(Long.valueOf(rdh.getObjKey()));
                ds.persist(ulh);

                return null;
            }
        });
    }

    private PricedOptionHistory createParameterOption(final long modDate,
            final String optionId) throws Exception {
        return runTX(new Callable<PricedOptionHistory>() {
            @Override
            public PricedOptionHistory call() throws Exception {
                ParameterOptionHistory lparamOptHist = new ParameterOptionHistory();
                initDOH(lparamOptHist, new Date(modDate));
                lparamOptHist.setParameterDefObjKey(++key);
                lparamOptHist.getDataContainer().setOptionId(optionId);
                ds.persist(lparamOptHist);
                // create option if required
                PricedOptionHistory lpoh = new PricedOptionHistory();
                initDOH(lpoh, new Date(modDate));
                lpoh.setParameterOptionObjKey(lparamOptHist.getObjKey());
                lpoh.setPricedParameterObjKey(pph.getObjKey());
                lpoh.setPricePerSubscription(new BigDecimal("200"));
                lpoh.setPricePerUser(new BigDecimal("300"));
                ds.persist(lpoh);
                return lpoh;
            }
        });
    }

    private void initDOH(DomainHistoryObject<? extends DomainDataContainer> doh,
            Date date) {
        doh.setObjVersion(++version);
        doh.setModdate(date);
        doh.setInvocationDate(date);
        doh.setObjKey(++key);
        doh.setModtype(ModificationType.ADD);
        doh.setModuser("user");
    }

    private void createRoleDefinitionHistory(final long modDate)
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                rdh = new RoleDefinitionHistory();
                initDOH(rdh, new Date(modDate));
                rdh.getDataContainer().setRoleId(ROLE_ID);
                ds.persist(rdh);
                return null;
            }
        });
    }

    private void updateParameter(final long modDate, final String value)
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ParameterHistory lph = new ParameterHistory();
                lph.setObjVersion(++version);
                lph.setModdate(new Date(modDate));
                lph.setInvocationDate(lph.getModdate());
                lph.setObjKey(ph.getObjKey());
                lph.setModtype(ModificationType.MODIFY);
                lph.setModuser("user");
                lph.setParameterDefinitionObjKey(pdh.getObjKey());
                lph.getDataContainer().setValue(value);
                ds.persist(lph);
                return null;
            }
        });
    }

    private void deleteParameter(final long modDate, final String value)
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ParameterHistory lph = new ParameterHistory();
                lph.setObjVersion(++version);
                lph.setModdate(new Date(modDate));
                lph.setInvocationDate(lph.getModdate());
                lph.setObjKey(ph.getObjKey());
                lph.setModtype(ModificationType.DELETE);
                lph.setModuser("user");
                lph.setParameterDefinitionObjKey(pdh.getObjKey());
                lph.getDataContainer().setValue(value);
                ds.persist(lph);
                return null;
            }
        });
    }

    private void updatePricedParameter(final long modDate,
            final BigDecimal pricePerUser,
            final BigDecimal pricePerSubscription) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                PricedParameterHistory lpph = new PricedParameterHistory();
                lpph.setObjVersion(++version);
                lpph.setModdate(new Date(modDate));
                lpph.setInvocationDate(lpph.getModdate());
                lpph.setPriceModelObjKey(pph.getPriceModelObjKey());
                lpph.setParameterObjKey(pph.getParameterObjKey());
                lpph.setModtype(ModificationType.MODIFY);
                lpph.setPricePerSubscription(pricePerSubscription);
                lpph.setPricePerUser(pricePerUser);
                lpph.setModuser("user");
                ds.persist(lpph);
                return null;
            }
        });
    }

    private void updateParameterDefinitionHistory(final long modDate)
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ParameterDefinitionHistory lpdh = new ParameterDefinitionHistory();
                lpdh.setObjVersion(++version);
                lpdh.getDataContainer().setParameterId(pdh.getParameterId());
                lpdh.setModdate(new Date(modDate));
                lpdh.setInvocationDate(lpdh.getModdate());
                lpdh.setObjKey(pdh.getObjKey());
                lpdh.setModtype(ModificationType.MODIFY);
                lpdh.setModuser("user");
                lpdh.getDataContainer()
                        .setParameterType(ParameterType.PLATFORM_PARAMETER);
                lpdh.getDataContainer().setValueType(pdh.getValueType());
                ds.persist(lpdh);
                return null;
            }
        });
    }

    private void addUsageLicenseHistory(final long modDate) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                UsageLicenseHistory lulh = new UsageLicenseHistory();
                lulh.setObjVersion(++version);
                lulh.setModdate(new Date(modDate));
                lulh.setInvocationDate(ulh.getModdate());
                lulh.setObjKey(++key);
                lulh.setModtype(ModificationType.ADD);
                lulh.setModuser("user");
                lulh.setSubscriptionObjKey(sh.getObjKey());
                lulh.setUserObjKey(++key);
                lulh.getDataContainer().setAssignmentDate(modDate);
                lulh.setRoleDefinitionObjKey(Long.valueOf(rdh.getObjKey()));
                ds.persist(lulh);
                ulh = lulh;
                return null;
            }
        });
    }

    private void deleteUsageLicenseHistory(final long modDate)
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                UsageLicenseHistory lulh = new UsageLicenseHistory();
                lulh.setObjVersion(++version);
                lulh.setModdate(new Date(modDate));
                lulh.setInvocationDate(ulh.getModdate());
                lulh.setObjKey(ulh.getObjKey());
                lulh.setModtype(ModificationType.DELETE);
                lulh.setModuser("user");
                lulh.setSubscriptionObjKey(sh.getObjKey());
                lulh.setUserObjKey(ulh.getUserObjKey());
                lulh.getDataContainer().setAssignmentDate(modDate);
                lulh.setRoleDefinitionObjKey(Long.valueOf(rdh.getObjKey()));
                ds.persist(lulh);
                return null;
            }
        });
    }

    private void createSteppedPrices(final long modDate) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                SteppedPriceHistory sph = new SteppedPriceHistory();
                sph.setObjVersion(++version);
                sph.setModdate(new Date(modDate));
                sph.setInvocationDate(ulh.getModdate());
                sph.setObjKey(++key);
                sph.setModtype(ModificationType.ADD);
                sph.setModuser("user");
                sph.setPricedParameterObjKey(Long.valueOf(pph.getObjKey()));
                sph.getDataContainer().setPrice(new BigDecimal("40"));
                ds.persist(sph);
                return null;
            }
        });
    }

    private void createRolePrice(final long modDate, final Long parameterObjKey,
            final Long optionObjKey) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                PricedProductRoleHistory pprh = new PricedProductRoleHistory();
                pprh.setObjVersion(++version);
                pprh.setModdate(new Date(modDate));
                pprh.setInvocationDate(ulh.getModdate());
                pprh.setObjKey(++key);
                pprh.setModtype(ModificationType.ADD);
                pprh.setModuser("user");
                pprh.setPricedParameterObjKey(parameterObjKey);
                pprh.setPricedOptionObjKey(optionObjKey);
                pprh.setPriceModelObjKey(Long.valueOf(pmh.getObjKey()));
                pprh.setPricePerUser(new BigDecimal("99.99"));
                pprh.setRoleDefinitionObjKey(rdh.getObjKey());
                ds.persist(pprh);
                return null;
            }
        });
    }

}
