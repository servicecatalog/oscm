/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Oct 22, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.dao;

import static org.junit.Assert.assertEquals;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Callable;

import org.junit.Test;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.enums.ModificationType;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.PriceModels;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;

/**
 * @author kulle
 * 
 */
public class BillingDataRetrievalServiceBeanPriceModelIT extends EJBTestBase {

    private static final long PRICE_MODEL_KEY0 = 181000;
    private static final long PRICE_MODEL_KEY1 = 181001;
    private static final long PRICE_MODEL_KEY2 = 181002;
    private static final long PRICE_MODEL_KEY3 = 181003;
    private static final long PRICE_MODEL_KEY4 = 181004;
    private static final long PRICE_MODEL_KEY5 = 181005;
    private static final long PRICE_MODEL_KEY6 = 181006;
    private BillingDataRetrievalServiceLocal bdr;
    private DataService ds;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        container.addBean(new BillingDataRetrievalServiceBean());
        ds = container.get(DataService.class);
        bdr = container.get(BillingDataRetrievalServiceLocal.class);
    }

    private void givenPriceModelHistories() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                PriceModels.createPriceModelHistory(ds, PRICE_MODEL_KEY0,
                        "2013-02-11 00:00:00,000", 0, ModificationType.ADD,
                        PriceModelType.PER_UNIT, 182000);
                PriceModels.createPriceModelHistory(ds, PRICE_MODEL_KEY1,
                        "2013-03-01 00:00:00,000", 0, ModificationType.ADD,
                        PriceModelType.PER_UNIT, 182001);
                PriceModels.createPriceModelHistory(ds, PRICE_MODEL_KEY1,
                        "2013-03-07 06:00:00,000", 1, ModificationType.MODIFY,
                        PriceModelType.PER_UNIT, 182001);
                PriceModels.createPriceModelHistory(ds, PRICE_MODEL_KEY2,
                        "2013-03-08 11:00:00,000", 0, ModificationType.ADD,
                        PriceModelType.FREE_OF_CHARGE, 182002);
                PriceModels.createPriceModelHistory(ds, PRICE_MODEL_KEY6,
                        "2013-03-11 17:23:00,000", 0, ModificationType.ADD,
                        PriceModelType.PRO_RATA, 182006, false);
                PriceModels.createPriceModelHistory(ds, PRICE_MODEL_KEY1,
                        "2013-03-11 18:00:00,000", 2, ModificationType.DELETE,
                        PriceModelType.PER_UNIT, 182001);
                PriceModels.createPriceModelHistory(ds, PRICE_MODEL_KEY3,
                        "2013-03-11 18:00:00,000", 0, ModificationType.ADD,
                        PriceModelType.FREE_OF_CHARGE, 182003);
                PriceModels.createPriceModelHistory(ds, PRICE_MODEL_KEY6,
                        "2013-03-11 18:02:00,000", 1, ModificationType.MODIFY,
                        PriceModelType.PRO_RATA, 182006, true);
                PriceModels.createPriceModelHistory(ds, PRICE_MODEL_KEY4,
                        "2013-03-28 12:00:00,000", 0, ModificationType.ADD,
                        PriceModelType.PRO_RATA, 182004);
                PriceModels.createPriceModelHistory(ds, PRICE_MODEL_KEY3,
                        "2013-03-31 10:00:00,000", 1, ModificationType.DELETE,
                        PriceModelType.FREE_OF_CHARGE, 182003);
                PriceModels.createPriceModelHistory(ds, PRICE_MODEL_KEY5,
                        "2013-03-31 10:00:00,000", 1, ModificationType.ADD,
                        PriceModelType.PRO_RATA, 182005);
                PriceModels.createPriceModelHistory(ds, PRICE_MODEL_KEY6,
                        "2013-03-31 11:12:00,000", 2, ModificationType.MODIFY,
                        PriceModelType.PRO_RATA, 182006, true);
                PriceModels.createPriceModelHistory(ds, PRICE_MODEL_KEY6,
                        "2013-03-31 11:13:00,000", 3, ModificationType.DELETE,
                        PriceModelType.PRO_RATA, 182006, true);
                return null;
            }
        });
    }

    @Test
    public void getPriceModelStartDate() throws Exception {
        // given
        givenPriceModelHistories();

        // when
        Date priceModel1StartDate = runTX(new Callable<Date>() {
            @Override
            public Date call() throws Exception {
                return bdr.loadPriceModelStartDate(PRICE_MODEL_KEY1);
            }
        });

        Date priceModel3StartDate = runTX(new Callable<Date>() {
            @Override
            public Date call() throws Exception {
                return bdr.loadPriceModelStartDate(PRICE_MODEL_KEY3);
            }
        });

        Date priceModel5StartDate = runTX(new Callable<Date>() {
            @Override
            public Date call() throws Exception {
                return bdr.loadPriceModelStartDate(PRICE_MODEL_KEY5);
            }
        });

        Date priceModel6StartDate = runTX(new Callable<Date>() {
            @Override
            public Date call() throws Exception {
                return bdr.loadPriceModelStartDate(PRICE_MODEL_KEY6);
            }
        });

        // then
        assertEquals("Wrong start date",
                new SimpleDateFormat(PriceModels.DATE_PATTERN).parse(
                        "2013-03-01 00:00:00,000"),
                priceModel1StartDate);

        assertEquals("Wrong start date",
                new SimpleDateFormat(PriceModels.DATE_PATTERN).parse(
                        "2013-03-11 18:00:00,000"),
                priceModel3StartDate);

        assertEquals("Wrong start date",
                new SimpleDateFormat(PriceModels.DATE_PATTERN).parse(
                        "2013-03-31 10:00:00,000"),
                priceModel5StartDate);

        assertEquals("Wrong start date",
                new SimpleDateFormat(PriceModels.DATE_PATTERN).parse(
                        "2013-03-11 18:02:00,000"),
                priceModel6StartDate);
    }

}
