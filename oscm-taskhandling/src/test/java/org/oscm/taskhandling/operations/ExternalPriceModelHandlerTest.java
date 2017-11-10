/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 05.02.2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.taskhandling.operations;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.oscm.billing.external.context.ContextKey;
import org.oscm.billing.external.context.ContextValue;
import org.oscm.billing.external.context.ContextValueString;
import org.oscm.billing.external.pricemodel.service.PriceModel;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.DomainObject;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.internal.pricemodel.external.ExternalPriceModelService;
import org.oscm.taskhandling.facade.ServiceFacade;
import org.oscm.taskhandling.payloads.ExternalPriceModelPayload;

public class ExternalPriceModelHandlerTest {

    private static UUID UUID = new UUID(0, 10000);
    private static String SUBSCRIPTION_ID = "Trial Subscription";
    private static String TENANT_ID = "89407ff7";

    ExternalPriceModelHandler handler = null;

    ExternalPriceModelService externalPriceModelServiceMock;
    DataService dataServiceMock;
    ExternalPriceModelPayload payload;
    PriceModel priceModel;
    Organization organization;
    Subscription subscription;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Before
    public void setUp() throws Exception {

        handler = new ExternalPriceModelHandler();
        handler.setServiceFacade(createServiceFacade());

        organization = new Organization();
        organization.setOrganizationId(TENANT_ID);
        organization.setKey(1000);

        subscription = new Subscription();
        subscription.setSubscriptionId(SUBSCRIPTION_ID);
        Product product = new Product();
        product.setPriceModel(new org.oscm.domobjects.PriceModel());

        subscription.setProduct(product);

        when(dataServiceMock.getReferenceByBusinessKey(any(Organization.class)))
                .thenReturn((DomainObject) organization);
        when(dataServiceMock.find(any(Subscription.class))).thenReturn(
                (DomainObject) subscription);
    }

    private ServiceFacade createServiceFacade() throws Exception {
        ServiceFacade facade = new ServiceFacade();

        externalPriceModelServiceMock = mock(ExternalPriceModelService.class);
        facade.setExternalPriceModelService(externalPriceModelServiceMock);

        dataServiceMock = mock(DataService.class);
        facade.setDataService(dataServiceMock);

        return facade;
    }

    private ExternalPriceModelPayload preparePayload() {
        ExternalPriceModelPayload externalPriceModelPayload = new ExternalPriceModelPayload();
        externalPriceModelPayload.setPriceModel(priceModel);

        return externalPriceModelPayload;
    }

    private PriceModel preparePriceModel() {
        PriceModel priceModel = new PriceModel(UUID);

        HashMap<ContextKey, ContextValue<?>> context = new HashMap<ContextKey, ContextValue<?>>();
        context.put(ContextKey.SUBSCRIPTION_ID, new ContextValueString(
                SUBSCRIPTION_ID));
        context.put(ContextKey.TENANT_ID, new ContextValueString(TENANT_ID));
        priceModel.setContext(context);

        return priceModel;
    }

    private PriceModel preparePriceModelWithMissingSubscriptionId() {
        PriceModel priceModel = new PriceModel(UUID);

        HashMap<ContextKey, ContextValue<?>> context = new HashMap<ContextKey, ContextValue<?>>();
        context.put(ContextKey.TENANT_ID, new ContextValueString(TENANT_ID));
        priceModel.setContext(context);

        return priceModel;
    }

    private PriceModel preparePriceModelWithMissingTenantId() {
        PriceModel priceModel = new PriceModel(UUID);

        HashMap<ContextKey, ContextValue<?>> context = new HashMap<ContextKey, ContextValue<?>>();
        context.put(ContextKey.SUBSCRIPTION_ID, new ContextValueString(
                SUBSCRIPTION_ID));
        priceModel.setContext(context);

        return priceModel;
    }

    @Test
    public void testExecute() throws Exception {

        // given
        priceModel = preparePriceModel();
        payload = preparePayload();
        handler.setPayload(payload);

        // when
        handler.execute();

        // then
        verify(externalPriceModelServiceMock).updateCache(priceModel);
        verify(dataServiceMock).refresh(subscription);
    }

    @Test(expected = NullPointerException.class)
    public void testExecuteWithMissingSubscriptionId() throws Exception {

        // given
        priceModel = preparePriceModelWithMissingSubscriptionId();
        payload = preparePayload();
        handler.setPayload(payload);

        // when
        handler.execute();

        // then
        // expected Exception
    }

    @Test(expected = NullPointerException.class)
    public void testExecuteWithMissingTenantId() throws Exception {

        // given
        priceModel = preparePriceModelWithMissingTenantId();
        payload = preparePayload();
        handler.setPayload(payload);

        // when
        handler.execute();

        // then
        // expected Exception
    }
}
