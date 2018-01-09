/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Mar 6, 2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.Date;
import java.util.concurrent.Callable;

import javax.ejb.EJBException;
import javax.persistence.NoResultException;

import org.junit.Test;

import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.enums.ModificationType;
import org.oscm.domobjects.enums.RevenueShareModelType;
import org.oscm.test.BigDecimalAsserts;
import org.oscm.test.DateTimeHandling;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.CatalogEntries;
import org.oscm.test.data.Marketplaces;
import org.oscm.test.data.RevenueShareModels;
import org.oscm.test.ejb.TestContainer;

/**
 * @author tokoda
 * 
 */
public class SharesDataRetrievalServiceBeanLoadRevenueShareIT extends
        EJBTestBase {

    private final static long SERVICE_KEY = 10000L;
    private final static long MARKETPLACE_KEY = 88888L;
    private final static long OPERATOR_RS_KEY = 20000L;
    private final static long MARKETPLACE_RS_KEY = 30000L;
    private final static long BROKER_RS_KEY = 40000L;
    private final static long RESELLER_RS_KEY = 50000L;

    private DataService ds;
    private SharesDataRetrievalServiceLocal dao;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.enableInterfaceMocking(true);
        container.login("1");
        container.addBean(new DataServiceBean());
        container.addBean(new SharesDataRetrievalServiceBean());
        ds = container.get(DataService.class);
        dao = container.get(SharesDataRetrievalServiceLocal.class);
    }

    @Test
    public void loadOperatorRevenueSharePercentage_ServiceNotExists() {
        // given
        // when
        try {
            dao.loadOperatorRevenueSharePercentage(SERVICE_KEY, 0);
            fail();
        } catch (EJBException ex) {
            // then
            assertEquals(NoResultException.class, ex.getCausedByException()
                    .getClass());
        }
    }

    @Test
    public void loadOperatorRevenueSharePercentage_InclusiveModdate()
            throws Exception {
        // given
        long endPeriod = DateTimeHandling
                .calculateMillis("2013-01-01 10:00:00");
        createRevenueShareModelHistory(OPERATOR_RS_KEY, new Date(endPeriod),
                ModificationType.ADD, 0, BigDecimal.ONE,
                RevenueShareModelType.OPERATOR_REVENUE_SHARE);
        createCatalogEntryHistory(1L, new Date(endPeriod),
                ModificationType.ADD, 0, Long.valueOf(SERVICE_KEY), null, null,
                null, Long.valueOf(OPERATOR_RS_KEY));

        // when
        BigDecimal result = dao.loadOperatorRevenueSharePercentage(SERVICE_KEY,
                endPeriod);

        // then
        BigDecimalAsserts.checkEquals(BigDecimal.ONE, result);
    }

    @Test
    public void loadOperatorRevenueSharePercentage_TakeNewestForEndPeriod()
            throws Exception {
        // given
        long endPeriod = DateTimeHandling
                .calculateMillis("2013-01-01 10:00:00");
        createRevenueShareModelHistory(OPERATOR_RS_KEY,
                new Date(endPeriod - 1), ModificationType.MODIFY, 1,
                BigDecimal.ONE, RevenueShareModelType.OPERATOR_REVENUE_SHARE);
        createRevenueShareModelHistory(OPERATOR_RS_KEY,
                new Date(endPeriod - 1), ModificationType.ADD, 0,
                BigDecimal.ZERO, RevenueShareModelType.OPERATOR_REVENUE_SHARE);
        createCatalogEntryHistory(1L, new Date(endPeriod - 1),
                ModificationType.ADD, 0, Long.valueOf(SERVICE_KEY), null, null,
                null, Long.valueOf(OPERATOR_RS_KEY));

        createRevenueShareModelHistory(OPERATOR_RS_KEY,
                new Date(endPeriod + 1), ModificationType.MODIFY, 2,
                BigDecimal.TEN, RevenueShareModelType.OPERATOR_REVENUE_SHARE);

        // when
        BigDecimal result = dao.loadOperatorRevenueSharePercentage(SERVICE_KEY,
                endPeriod);

        // then
        BigDecimalAsserts.checkEquals(BigDecimal.ONE, result);
    }

    @Test
    public void loadMarketplaceRevenueSharePercentage_MpNotExists()
            throws Exception {
        // given
        // when
        BigDecimal result = loadMarketplaceRevenueSharePercentage(1, 1);

        // then
        assertNull("No result expected", result);
    }

    @Test
    public void loadMarketplaceRevenueSharePercentage_InclusiveModdate()
            throws Exception {
        // given
        String endPeriod = "2013-01-01 10:00:00";
        long endPeriodMs = DateTimeHandling.calculateMillis(endPeriod);
        createMarketplaceHistory(MARKETPLACE_KEY, endPeriod,
                ModificationType.ADD, 0, 4711L, MARKETPLACE_RS_KEY,
                BROKER_RS_KEY, RESELLER_RS_KEY);
        createRevenueShareModelHistory(MARKETPLACE_RS_KEY - 1, new Date(
                endPeriodMs), ModificationType.ADD, 0, BigDecimal.ZERO,
                RevenueShareModelType.MARKETPLACE_REVENUE_SHARE);
        createRevenueShareModelHistory(MARKETPLACE_RS_KEY,
                new Date(endPeriodMs), ModificationType.ADD, 0, BigDecimal.ONE,
                RevenueShareModelType.MARKETPLACE_REVENUE_SHARE);
        createRevenueShareModelHistory(MARKETPLACE_RS_KEY + 1, new Date(
                endPeriodMs), ModificationType.ADD, 0, BigDecimal.ZERO,
                RevenueShareModelType.MARKETPLACE_REVENUE_SHARE);

        // when
        BigDecimal result = loadMarketplaceRevenueSharePercentage(
                MARKETPLACE_KEY, endPeriodMs);

        // then
        BigDecimalAsserts.checkEquals(BigDecimal.ONE, result);
    }

    @Test
    public void loadMarketplaceRevenueSharePercentage_TakeNewestForEndPeriod()
            throws Exception {
        // given
        String endPeriod = "2013-01-01 10:00:00";
        long endPeriodMs = DateTimeHandling.calculateMillis(endPeriod);
        createMarketplaceHistory(MARKETPLACE_KEY, "2013-01-01 05:00:00",
                ModificationType.ADD, 0, 4711L, MARKETPLACE_RS_KEY,
                BROKER_RS_KEY, RESELLER_RS_KEY);
        createRevenueShareModelHistory(MARKETPLACE_RS_KEY - 1, new Date(
                endPeriodMs - 1), ModificationType.ADD, 0, BigDecimal.TEN,
                RevenueShareModelType.MARKETPLACE_REVENUE_SHARE);
        createRevenueShareModelHistory(MARKETPLACE_RS_KEY, new Date(
                endPeriodMs - 1), ModificationType.ADD, 0, BigDecimal.ZERO,
                RevenueShareModelType.MARKETPLACE_REVENUE_SHARE);
        createRevenueShareModelHistory(MARKETPLACE_RS_KEY - 1, new Date(
                endPeriodMs - 1), ModificationType.MODIFY, 1, BigDecimal.TEN,
                RevenueShareModelType.MARKETPLACE_REVENUE_SHARE);
        createRevenueShareModelHistory(MARKETPLACE_RS_KEY, new Date(
                endPeriodMs - 1), ModificationType.MODIFY, 1, BigDecimal.ONE,
                RevenueShareModelType.MARKETPLACE_REVENUE_SHARE);
        createRevenueShareModelHistory(MARKETPLACE_RS_KEY - 1, new Date(
                endPeriodMs - 1), ModificationType.MODIFY, 2, BigDecimal.TEN,
                RevenueShareModelType.MARKETPLACE_REVENUE_SHARE);
        createRevenueShareModelHistory(MARKETPLACE_RS_KEY, new Date(
                endPeriodMs + 1), ModificationType.ADD, 2, BigDecimal.TEN,
                RevenueShareModelType.MARKETPLACE_REVENUE_SHARE);

        // when
        BigDecimal result = loadMarketplaceRevenueSharePercentage(
                MARKETPLACE_KEY, endPeriodMs);

        // then
        BigDecimalAsserts.checkEquals(BigDecimal.ONE, result);
    }

    private BigDecimal loadMarketplaceRevenueSharePercentage(final long mpkey,
            final long endPeriod) throws Exception {
        return runTX(new Callable<BigDecimal>() {
            @Override
            public BigDecimal call() throws Exception {
                return dao.loadMarketplaceRevenueSharePercentage(mpkey,
                        endPeriod);
            }
        });
    }

    @Test
    public void loadBrokerRevenueSharePercentage_ServiceNotExists() {
        // given
        // when
        BigDecimal result = dao.loadBrokerRevenueSharePercentage(1, 1);

        // then
        assertNull("No result expected", result);
    }

    @Test
    public void loadBrokerRevenueSharePercentage_NoMpInCatEntry()
            throws Exception {
        // given
        long endPeriod = DateTimeHandling
                .calculateMillis("2013-01-01 10:00:00");
        createCatalogEntryHistory(1L, new Date(endPeriod),
                ModificationType.ADD, 0, Long.valueOf(SERVICE_KEY), null,
                Long.valueOf(BROKER_RS_KEY), null, null);
        createRevenueShareModelHistory(BROKER_RS_KEY, new Date(endPeriod),
                ModificationType.ADD, 0, BigDecimal.ONE,
                RevenueShareModelType.BROKER_REVENUE_SHARE);

        // when
        BigDecimal result = dao.loadBrokerRevenueSharePercentage(SERVICE_KEY,
                endPeriod);

        // then
        assertNull("No result expected", result);
    }

    @Test
    public void loadBrokerRevenueSharePercentage_InclusiveModdate()
            throws Exception {
        // given
        long endPeriod = DateTimeHandling
                .calculateMillis("2013-01-01 10:00:00");
        createCatalogEntryHistory(1L, new Date(endPeriod),
                ModificationType.ADD, 0, Long.valueOf(SERVICE_KEY),
                Long.valueOf(MARKETPLACE_KEY), Long.valueOf(BROKER_RS_KEY),
                null, null);
        createRevenueShareModelHistory(BROKER_RS_KEY - 1, new Date(endPeriod),
                ModificationType.ADD, 0, BigDecimal.ZERO,
                RevenueShareModelType.BROKER_REVENUE_SHARE);
        createRevenueShareModelHistory(BROKER_RS_KEY, new Date(endPeriod),
                ModificationType.ADD, 0, BigDecimal.ONE,
                RevenueShareModelType.BROKER_REVENUE_SHARE);
        createRevenueShareModelHistory(BROKER_RS_KEY + 1, new Date(endPeriod),
                ModificationType.ADD, 0, BigDecimal.TEN,
                RevenueShareModelType.BROKER_REVENUE_SHARE);

        // when
        BigDecimal result = dao.loadBrokerRevenueSharePercentage(SERVICE_KEY,
                endPeriod);

        // then
        BigDecimalAsserts.checkEquals(BigDecimal.ONE, result);
    }

    @Test
    public void loadBrokerRevenueSharePercentage_TakeNewestForEndPeriod()
            throws Exception {
        // given
        long endPeriod = DateTimeHandling
                .calculateMillis("2013-01-01 10:00:00");
        createCatalogEntryHistory(1L, new Date(endPeriod - 10),
                ModificationType.ADD, 0, Long.valueOf(SERVICE_KEY),
                Long.valueOf(MARKETPLACE_KEY), Long.valueOf(BROKER_RS_KEY),
                null, null);
        createRevenueShareModelHistory(BROKER_RS_KEY - 1, new Date(
                endPeriod - 1), ModificationType.ADD, 0, BigDecimal.TEN,
                RevenueShareModelType.BROKER_REVENUE_SHARE);
        createRevenueShareModelHistory(BROKER_RS_KEY, new Date(endPeriod - 1),
                ModificationType.ADD, 0, BigDecimal.ZERO,
                RevenueShareModelType.BROKER_REVENUE_SHARE);
        createRevenueShareModelHistory(BROKER_RS_KEY + 1, new Date(
                endPeriod - 1), ModificationType.ADD, 0, BigDecimal.ZERO,
                RevenueShareModelType.BROKER_REVENUE_SHARE);
        createRevenueShareModelHistory(BROKER_RS_KEY + 1, new Date(
                endPeriod - 1), ModificationType.MODIFY, 1, BigDecimal.TEN,
                RevenueShareModelType.BROKER_REVENUE_SHARE);
        createRevenueShareModelHistory(BROKER_RS_KEY, new Date(endPeriod - 1),
                ModificationType.MODIFY, 1, BigDecimal.ONE,
                RevenueShareModelType.BROKER_REVENUE_SHARE);
        createRevenueShareModelHistory(BROKER_RS_KEY + 1, new Date(
                endPeriod - 1), ModificationType.MODIFY, 2, BigDecimal.TEN,
                RevenueShareModelType.BROKER_REVENUE_SHARE);
        createRevenueShareModelHistory(BROKER_RS_KEY, new Date(endPeriod + 1),
                ModificationType.MODIFY, 2, BigDecimal.TEN,
                RevenueShareModelType.BROKER_REVENUE_SHARE);

        // when
        BigDecimal result = dao.loadBrokerRevenueSharePercentage(SERVICE_KEY,
                endPeriod);

        // then
        BigDecimalAsserts.checkEquals(BigDecimal.ONE, result);
    }

    @Test
    public void loadResellerRevenueSharePercentage_ServiceNotExists() {
        // given
        // when
        BigDecimal result = dao.loadResellerRevenueSharePercentage(1, 1);

        // then
        assertNull("No result expected", result);
    }

    @Test
    public void loadResellerRevenueSharePercentage_NoMpInCatEntry()
            throws Exception {
        // given
        long endPeriod = DateTimeHandling
                .calculateMillis("2013-01-01 10:00:00");
        createCatalogEntryHistory(1L, new Date(endPeriod),
                ModificationType.ADD, 0, Long.valueOf(SERVICE_KEY), null, null,
                Long.valueOf(RESELLER_RS_KEY), null);
        createRevenueShareModelHistory(RESELLER_RS_KEY, new Date(endPeriod),
                ModificationType.ADD, 0, BigDecimal.ONE,
                RevenueShareModelType.RESELLER_REVENUE_SHARE);

        // when
        BigDecimal result = dao.loadResellerRevenueSharePercentage(SERVICE_KEY,
                endPeriod);

        // then
        assertNull("No result expected", result);
    }

    @Test
    public void loadResellerRevenueSharePercentage_InclusiveModdate()
            throws Exception {
        // given
        long endPeriod = DateTimeHandling
                .calculateMillis("2013-01-01 10:00:00");
        createCatalogEntryHistory(1L, new Date(endPeriod),
                ModificationType.ADD, 0, Long.valueOf(SERVICE_KEY),
                Long.valueOf(MARKETPLACE_KEY), null,
                Long.valueOf(RESELLER_RS_KEY), null);
        createRevenueShareModelHistory(RESELLER_RS_KEY - 1,
                new Date(endPeriod), ModificationType.ADD, 0, BigDecimal.ZERO,
                RevenueShareModelType.RESELLER_REVENUE_SHARE);
        createRevenueShareModelHistory(RESELLER_RS_KEY, new Date(endPeriod),
                ModificationType.ADD, 0, BigDecimal.ONE,
                RevenueShareModelType.RESELLER_REVENUE_SHARE);
        createRevenueShareModelHistory(RESELLER_RS_KEY + 1,
                new Date(endPeriod), ModificationType.ADD, 0, BigDecimal.TEN,
                RevenueShareModelType.RESELLER_REVENUE_SHARE);

        // when
        BigDecimal result = dao.loadResellerRevenueSharePercentage(SERVICE_KEY,
                endPeriod);

        // then
        BigDecimalAsserts.checkEquals(BigDecimal.ONE, result);
    }

    @Test
    public void loadResellerRevenueSharePercentage_TakeNewestForEndPeriod()
            throws Exception {
        // given
        long endPeriod = DateTimeHandling
                .calculateMillis("2013-01-01 10:00:00");
        createCatalogEntryHistory(1L, new Date(endPeriod - 10),
                ModificationType.ADD, 0, Long.valueOf(SERVICE_KEY),
                Long.valueOf(MARKETPLACE_KEY), null,
                Long.valueOf(RESELLER_RS_KEY), null);
        createRevenueShareModelHistory(RESELLER_RS_KEY - 1, new Date(
                endPeriod - 1), ModificationType.ADD, 0, BigDecimal.TEN,
                RevenueShareModelType.RESELLER_REVENUE_SHARE);
        createRevenueShareModelHistory(RESELLER_RS_KEY,
                new Date(endPeriod - 1), ModificationType.ADD, 0,
                BigDecimal.ZERO, RevenueShareModelType.RESELLER_REVENUE_SHARE);
        createRevenueShareModelHistory(RESELLER_RS_KEY + 1, new Date(
                endPeriod - 1), ModificationType.ADD, 0, BigDecimal.ZERO,
                RevenueShareModelType.RESELLER_REVENUE_SHARE);
        createRevenueShareModelHistory(RESELLER_RS_KEY + 1, new Date(
                endPeriod - 1), ModificationType.MODIFY, 1, BigDecimal.TEN,
                RevenueShareModelType.RESELLER_REVENUE_SHARE);
        createRevenueShareModelHistory(RESELLER_RS_KEY,
                new Date(endPeriod - 1), ModificationType.MODIFY, 1,
                BigDecimal.ONE, RevenueShareModelType.RESELLER_REVENUE_SHARE);
        createRevenueShareModelHistory(RESELLER_RS_KEY + 1, new Date(
                endPeriod - 1), ModificationType.MODIFY, 2, BigDecimal.TEN,
                RevenueShareModelType.RESELLER_REVENUE_SHARE);
        createRevenueShareModelHistory(RESELLER_RS_KEY,
                new Date(endPeriod + 1), ModificationType.MODIFY, 2,
                BigDecimal.TEN, RevenueShareModelType.RESELLER_REVENUE_SHARE);

        // when
        BigDecimal result = dao.loadResellerRevenueSharePercentage(SERVICE_KEY,
                endPeriod);

        // then
        BigDecimalAsserts.checkEquals(BigDecimal.ONE, result);
    }

    private void createCatalogEntryHistory(final long objKey,
            final Date modDate, final ModificationType modificationType,
            final int version, final Long productobjkey,
            final Long marketplaceobjkey, final Long brokerpricemodelobjkey,
            final Long resellerpricemodelobjkey,
            final Long operatorpricemodelobjkey) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                CatalogEntries.createCatalogEntryHistory(ds, objKey, modDate,
                        version, modificationType, productobjkey,
                        marketplaceobjkey, brokerpricemodelobjkey,
                        resellerpricemodelobjkey, operatorpricemodelobjkey);
                return null;
            }
        });
    }

    private void createMarketplaceHistory(final long objKey,
            final String modDate, final ModificationType modificationType,
            final int version, final long organizationObjKey,
            final long priceModelObjKey, final long brokerPriceModelObjKey,
            final long resellerPriceModelObjKey) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Marketplaces.createMarketplaceHistory(ds, objKey, modDate,
                        version, modificationType, organizationObjKey,
                        priceModelObjKey, brokerPriceModelObjKey,
                        resellerPriceModelObjKey);
                return null;
            }
        });
    }

    private void createRevenueShareModelHistory(final long objKey,
            final Date modDate, final ModificationType modificationType,
            final int version, final BigDecimal revenueShare,
            final RevenueShareModelType revenueShareModelType) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                RevenueShareModels.createRevenueShareModelHistory(ds, objKey,
                        modDate, version, modificationType, revenueShare,
                        revenueShareModelType);
                return null;
            }
        });
    }
}
