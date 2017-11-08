/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 10.12.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billing.application.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.UUID;
import java.util.concurrent.Callable;

import javax.ws.rs.core.MediaType;

import org.junit.Ignore;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.oscm.billingadapterservice.bean.BillingAdapterDAO;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.LocalizedBillingResource;
import org.oscm.domobjects.enums.LocalizedBillingResourceType;
import org.oscm.test.EJBTestBase;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;

public class LocalizedBillingResourceDAOIT extends EJBTestBase {

    private static final String EXTERNAL_PRICE_MODEL_EN_JSON = "{\"Charging conditions\":{\"Currency\":\"EUR\",\"Charges based on\":\"Month\","
            + "\"One-time fee\":\"1.00\",\"Recurring charge per subscription\":\"2.00\"}}";

    private static final String EXTERNAL_PRICE_MODEL_EN_HTML = "<html><chargingconditions><currency>EUR</currency><period>Month</period>"
            + "<onetimefee>1.00</onetimefee><recurringcharge>2.00</recurringcharge></chargingconditions></html>";

    private LocalizedBillingResourceDAO resourceDAO;
    private DataService ds;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.login("1");
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        container.addBean(new BillingAdapterDAO());
        ds = container.get(DataService.class);
        resourceDAO = spy(new LocalizedBillingResourceDAO());
        container.addBean(resourceDAO);
    }

    private LocalizedBillingResource createLocalizedBillingResource(UUID objID,
            String locale, String billingResourceDataType,
            byte[] billingResourceValue) throws Exception {

        LocalizedBillingResource billingResource = new LocalizedBillingResource(
                objID, locale, LocalizedBillingResourceType.PRICEMODEL_SERVICE);
        billingResource.setValue(billingResourceValue);
        billingResource.setDataType(billingResourceDataType);
        return billingResource;
    }

    private LocalizedBillingResource createLocalizedBillingResourceInDB(
            UUID objID, String locale, String billingResourceDataType,
            byte[] billingResourceValue) throws Exception {
        final LocalizedBillingResource billingResource = createLocalizedBillingResource(
                objID, locale, billingResourceDataType, billingResourceValue);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ds.persist(billingResource);
                return null;
            }
        });

        return findLocalizedBillingResourceInDB(billingResource);
    }

    private LocalizedBillingResource findLocalizedBillingResourceInDB(
            final LocalizedBillingResource localizedBillingResource)
            throws Exception {
        return runTX(new Callable<LocalizedBillingResource>() {
            @Override
            public LocalizedBillingResource call() throws Exception {
                return (LocalizedBillingResource) ds
                        .find(localizedBillingResource);
            }
        });
    }

    private LocalizedBillingResource updateLocalizedBillingResource(
            final LocalizedBillingResource changedResource) throws Exception {
        return runTX(new Callable<LocalizedBillingResource>() {
            @Override
            public LocalizedBillingResource call() throws Exception {
                LocalizedBillingResource r = resourceDAO
                        .update(changedResource);
                return r;
            }
        });
    }

    @Test
    public void updateLocalizedBillingResourceWithNewResource()
            throws Exception {
        // given
        UUID objID = UUID.randomUUID();
        LocalizedBillingResource newResource = createLocalizedBillingResource(
                objID, "en", MediaType.APPLICATION_JSON,
                EXTERNAL_PRICE_MODEL_EN_JSON.getBytes());

        // when
        LocalizedBillingResource dbResource = updateLocalizedBillingResource(
                newResource);

        // then
        assertTrue(dbResource.getKey() != 0L);
        LocalizedBillingResource newResourceInDB = findLocalizedBillingResourceInDB(
                newResource);
        assertEquals("Wrong version", 0, newResourceInDB.getVersion());
        assertEquals("Wrong locale", "en", newResourceInDB.getLocale());
        assertEquals("Wrong type",
                LocalizedBillingResourceType.PRICEMODEL_SERVICE,
                newResourceInDB.getResourceType());
        assertEquals("Wrong data type", MediaType.APPLICATION_JSON,
                newResourceInDB.getDataType());
        assertEquals("Wrong value", EXTERNAL_PRICE_MODEL_EN_JSON,
                new String(newResourceInDB.getValue()));
    }

    @Test
    public void updateLocalizedBillingResourceWithAlreadyInDB()
            throws Exception {
        // given
        UUID objID = UUID.randomUUID();
        createLocalizedBillingResourceInDB(objID, "en",
                MediaType.APPLICATION_JSON,
                EXTERNAL_PRICE_MODEL_EN_JSON.getBytes());
        LocalizedBillingResource changedResource = createLocalizedBillingResource(
                objID, "en", MediaType.APPLICATION_XML,
                EXTERNAL_PRICE_MODEL_EN_HTML.getBytes());

        // when
        LocalizedBillingResource dbResource = updateLocalizedBillingResource(
                changedResource);

        // then
        LocalizedBillingResource changedResourceInDB = findLocalizedBillingResourceInDB(
                changedResource);
        assertEquals("Wrong version", 1, changedResourceInDB.getVersion());
        assertEquals("Wrong data type", MediaType.APPLICATION_XML,
                changedResourceInDB.getDataType());
        assertEquals("Wrong value", EXTERNAL_PRICE_MODEL_EN_HTML,
                new String(changedResourceInDB.getValue()));
        assertTrue(dbResource.getKey() != 0L);
    }

    @Ignore
    public void updateLocalizedBillingResourceWithnewResourceConcurrentPersist()
            throws Exception {
        // given
        UUID objID = UUID.randomUUID();
        final LocalizedBillingResource newResource = createLocalizedBillingResource(
                objID, "en", MediaType.APPLICATION_JSON,
                EXTERNAL_PRICE_MODEL_EN_JSON.getBytes());
        final LocalizedBillingResource newResource2 = createLocalizedBillingResourceInDB(
                objID, "en", MediaType.APPLICATION_XML,
                EXTERNAL_PRICE_MODEL_EN_HTML.getBytes());

        doReturn(null).doAnswer(new Answer<LocalizedBillingResource>() {
            @Override
            public LocalizedBillingResource answer(InvocationOnMock invocation)
                    throws Exception {
                return (LocalizedBillingResource) ds.find(newResource2);
            }
        }).when(resourceDAO).get(any(LocalizedBillingResource.class));

        // when
        updateLocalizedBillingResource(newResource);

        // then
        LocalizedBillingResource newResourceInDB = findLocalizedBillingResourceInDB(
                newResource);
        assertEquals("Wrong version", 1, newResourceInDB.getVersion());
        assertEquals("Wrong locale", "en", newResourceInDB.getLocale());
        assertEquals("Wrong type",
                LocalizedBillingResourceType.PRICEMODEL_SERVICE,
                newResourceInDB.getResourceType());
        assertEquals("Wrong data type", MediaType.APPLICATION_JSON,
                newResourceInDB.getDataType());
        assertEquals("Wrong value", EXTERNAL_PRICE_MODEL_EN_JSON,
                new String(newResourceInDB.getValue()));
    }
}
