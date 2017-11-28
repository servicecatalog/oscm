/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import static org.oscm.serviceprovisioningservice.bean.ServiceProvisioningServiceBean.DEFAULT_PRICE_VALUE;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Event;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.Parameter;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.PriceModel;
import org.oscm.domobjects.PricedEvent;
import org.oscm.domobjects.PricedOption;
import org.oscm.domobjects.PricedParameter;
import org.oscm.domobjects.PricedProductRole;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.RoleDefinition;
import org.oscm.domobjects.SteppedPrice;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.SupportedCurrency;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.serviceprovisioningservice.auditlog.PriceModelAuditLogCollector;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.enumtypes.PricingPeriod;
import org.oscm.internal.types.enumtypes.ServiceType;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOPriceModel;
import org.oscm.internal.vo.VOPricedEvent;
import org.oscm.internal.vo.VOPricedOption;
import org.oscm.internal.vo.VOPricedParameter;
import org.oscm.internal.vo.VOPricedRole;
import org.oscm.internal.vo.VORoleDefinition;
import org.oscm.internal.vo.VOSteppedPrice;

@SuppressWarnings("boxing")
public class ServiceProvisioningCollectorTest {
    private ServiceProvisioningServiceBean service;
    private final static int FREEPERIOD_1 = 100;
    private final static PricingPeriod PRICINGPERIOD_1 = PricingPeriod.DAY;

    @Before
    public void before() {
        service = spy(new ServiceProvisioningServiceBean());
        service.priceModelAudit = mock(PriceModelAuditLogCollector.class);
        service.dm = mock(DataService.class);
        doReturn(new PlatformUser()).when(service.dm).getCurrentUser();
    }

    @Test
    public void updatePricedEvent() throws Exception {
        // given
        VOPricedEvent voPricedEvent = new VOPricedEvent();
        PricedEvent pricedEvent = new PricedEvent();
        pricedEvent.setEventPrice(BigDecimal.valueOf(34));
        Event event = new Event();
        PriceModel priceModel = new PriceModel();

        // when
        service.updatePricedEvent(voPricedEvent, pricedEvent, event, priceModel);

        // then
        verify(service.priceModelAudit).editEventPrice(service.dm, pricedEvent,
                BigDecimal.valueOf(34));
    }

    @Test
    public void createPricedEvent() throws Exception {
        // given
        VOPricedEvent voPricedEvent = new VOPricedEvent();
        voPricedEvent.setEventPrice(BigDecimal.valueOf(34));
        Event event = new Event();
        PriceModel priceModel = new PriceModel();

        // when
        PricedEvent pricedEvent = service.createPricedEvent(voPricedEvent,
                event, priceModel);

        // then
        verify(service.priceModelAudit).editEventPrice(service.dm, pricedEvent,
                DEFAULT_PRICE_VALUE);
    }

    @Test
    public void createSteppedPrice_forPricedEvent() throws Exception {
        testCreateSteppedPrice(null, new PricedEvent(), null);
    }

    private void testCreateSteppedPrice(PriceModel priceModel,
            PricedEvent pricedEvent, PricedParameter pricedParameter)
            throws Exception {
        // given
        VOSteppedPrice voSteppedPrice = new VOSteppedPrice();

        // when
        SteppedPrice steppedPrice = service.createSteppedPrice(voSteppedPrice,
                priceModel, pricedEvent, pricedParameter);

        // then
        int eventSteppedPriceCalled = pricedEvent != null ? 1 : 0;
        verify(service.priceModelAudit, times(eventSteppedPriceCalled))
                .insertEventSteppedPrice(service.dm, steppedPrice);

        int parameterSteppedPriceCalled = pricedParameter != null ? 1 : 0;
        verify(service.priceModelAudit, times(parameterSteppedPriceCalled))
                .insertParameterSteppedPrice(service.dm, steppedPrice);

        int edituserSteppedPriceCalled = priceModel != null ? 1 : 0;
        verify(service.priceModelAudit, times(edituserSteppedPriceCalled))
                .insertUserSteppedPrice(service.dm, steppedPrice);
    }

    @Test
    public void createSteppedPrice_forPricedParameter() throws Exception {
        testCreateSteppedPrice(null, null, new PricedParameter());
    }

    @Test
    public void createSteppedPrice_forPriceModel() throws Exception {
        testCreateSteppedPrice(new PriceModel(), null, null);
    }

    @Test
    public void removeSteppedPrices_forPricedEvent() {
        long voPriceModelKey = 654; // persisted pricemodel
        testRemoveSteppedPrices(voPriceModelKey, null, new PricedEvent(), null);
    }

    private void testRemoveSteppedPrices(long voPriceModelKey,
            PriceModel priceModel, PricedEvent pricedEvent,
            PricedParameter pricedParameter) {
        // given
        Collection<SteppedPrice> steppedPrices = new ArrayList<SteppedPrice>();
        SteppedPrice steppedPrice = new SteppedPrice();
        steppedPrices.add(steppedPrice);

        // when
        service.removeSteppedPrices(voPriceModelKey, steppedPrices,
                pricedEvent, pricedParameter, priceModel);

        // then
        int eventSteppedPriceCalled = pricedEvent != null
                && voPriceModelKey > 0 ? 1 : 0;
        verify(service.priceModelAudit, times(eventSteppedPriceCalled))
                .removeEventSteppedPrice(service.dm, steppedPrice);

        int parameterSteppedPriceCalled = pricedParameter != null
                && voPriceModelKey > 0 ? 1 : 0;
        verify(service.priceModelAudit, times(parameterSteppedPriceCalled))
                .removeParameterSteppedPrice(service.dm, steppedPrice);

        int edituserSteppedPriceCalled = priceModel != null
                && voPriceModelKey > 0 ? 1 : 0;
        verify(service.priceModelAudit, times(edituserSteppedPriceCalled))
                .removeUserSteppedPrice(service.dm, steppedPrice);
    }

    @Test
    public void removeSteppedPrices_forPricedParameter() {
        long voPriceModelKey = 654; // persisted pricemodel
        testRemoveSteppedPrices(voPriceModelKey, null, null,
                new PricedParameter());
    }

    @Test
    public void removeSteppedPrices_forPriceModel() {
        long voPriceModelKey = 654; // persisted pricemodel
        testRemoveSteppedPrices(voPriceModelKey, new PriceModel(), null, null);
    }

    @Test
    public void removeSteppedPrices_doNotLogRemoval() {
        long voPriceModelKey = 0; // new, transient pricemodel
        testRemoveSteppedPrices(voPriceModelKey, new PriceModel(), null, null);
    }

    @Test
    public void updateSteppedPrice_forPricedEvent() throws Exception {
        testUpdateSteppedPrice(null, new PricedEvent(), null);
    }

    private void testUpdateSteppedPrice(PriceModel priceModel,
            PricedEvent pricedEvent, PricedParameter pricedParameter)
            throws Exception {
        // given
        VOSteppedPrice voSteppedPrice = new VOSteppedPrice();
        SteppedPrice steppedPrice = new SteppedPrice();
        BigDecimal oldPrice = BigDecimal.valueOf(34);
        steppedPrice.setPrice(oldPrice);
        Long oldLimit = Long.valueOf(35);
        steppedPrice.setLimit(oldLimit);

        // when
        service.updateSteppedPrice(voSteppedPrice, steppedPrice, priceModel,
                pricedEvent, pricedParameter);

        // then
        int eventSteppedPriceCalled = pricedEvent != null ? 1 : 0;
        verify(service.priceModelAudit, times(eventSteppedPriceCalled))
                .editEventSteppedPrice(service.dm, steppedPrice, oldPrice,
                        oldLimit);

        int parameterSteppedPriceCalled = pricedParameter != null ? 1 : 0;
        verify(service.priceModelAudit, times(parameterSteppedPriceCalled))
                .editParameterSteppedPrice(service.dm, steppedPrice, oldPrice,
                        oldLimit);

        int editUserSteppedPriceCalled = priceModel != null ? 1 : 0;
        verify(service.priceModelAudit, times(editUserSteppedPriceCalled))
                .editUserSteppedPrice(service.dm, steppedPrice, oldPrice,
                        oldLimit);
    }

    @Test
    public void updateSteppedPrice_forPricedParameter() throws Exception {
        testUpdateSteppedPrice(null, null, new PricedParameter());
    }

    @Test
    public void updateSteppedPrice_forPriceModel() throws Exception {
        testUpdateSteppedPrice(new PriceModel(), null, null);
    }

    @Test
    public void deletePriceModel() {
        // given
        Product product = new Product();
        PriceModel priceModel = new PriceModel();
        product.setPriceModel(priceModel);

        // when
        service.deletePriceModelForCustomer(product);

        // then
        verify(service.priceModelAudit)
                .deletePriceModel(service.dm, priceModel);
    }

    @Test
    public void createPricedParameter() throws Exception {
        // given
        VOPricedParameter voPricedParameter = new VOPricedParameter();
        Parameter parameter = new Parameter();
        PriceModel priceModel = new PriceModel();

        // when
        PricedParameter pricedParameter = service.createPricedParameter(
                voPricedParameter, parameter, priceModel);

        // then
        verify(service.priceModelAudit).editParameterSubscriptionPrice(
                service.dm, pricedParameter, DEFAULT_PRICE_VALUE);
        verify(service.priceModelAudit).editParameterUserPrice(service.dm,
                pricedParameter, DEFAULT_PRICE_VALUE);
    }

    @Test
    public void createPricedParameter_withOptions() throws Exception {
        // given
        VOPricedParameter voPricedParameter = new VOPricedParameter();
        List<VOPricedOption> voPricedOptions = new ArrayList<VOPricedOption>();
        voPricedOptions.add(new VOPricedOption());
        voPricedParameter.setPricedOptions(voPricedOptions);

        Parameter parameter = new Parameter();
        PriceModel priceModel = new PriceModel();

        // when
        PricedParameter pricedParameter = service.createPricedParameter(
                voPricedParameter, parameter, priceModel);

        // then
        PricedOption pricedOption = pricedParameter.getPricedOptionList()
                .get(0);
        verify(service.priceModelAudit).editParameterOptionSubscriptionPrice(
                service.dm, pricedOption, DEFAULT_PRICE_VALUE);
        verify(service.priceModelAudit).editParameterOptionUserPrice(
                service.dm, pricedOption, DEFAULT_PRICE_VALUE);
    }

    @Test
    public void updatePricedParameter() throws Exception {
        // given
        VOPricedParameter voPricedParameter = new VOPricedParameter();
        PricedParameter pricedParameter = new PricedParameter();
        BigDecimal oldSubPrice = BigDecimal.valueOf(34);
        pricedParameter.setPricePerSubscription(oldSubPrice);
        BigDecimal oldUserPrice = BigDecimal.valueOf(35);
        pricedParameter.setPricePerUser(oldUserPrice);

        // when
        service.updatePricedParameter(voPricedParameter, pricedParameter);

        // then
        verify(service.priceModelAudit).editParameterSubscriptionPrice(
                service.dm, pricedParameter, oldSubPrice);
        verify(service.priceModelAudit).editParameterUserPrice(service.dm,
                pricedParameter, oldUserPrice);
    }

    @Test
    public void createPricedOption() {
        // given
        VOPricedOption voPricedOption = new VOPricedOption();
        PricedParameter pricedParameter = new PricedParameter();

        // when
        PricedOption pricedOption = service.createPricedOption(voPricedOption,
                pricedParameter);

        // then
        verify(service.priceModelAudit).editParameterOptionSubscriptionPrice(
                service.dm, pricedOption, DEFAULT_PRICE_VALUE);
        verify(service.priceModelAudit).editParameterOptionUserPrice(
                service.dm, pricedOption, DEFAULT_PRICE_VALUE);
    }

    @Test
    public void updatePricedOption() throws Exception {
        // given
        VOPricedOption voPricedOption = new VOPricedOption();
        PricedOption pricedOption = new PricedOption();

        BigDecimal oldPOSubPrice = BigDecimal.valueOf(34);
        pricedOption.setPricePerSubscription(oldPOSubPrice);
        BigDecimal oldPOUserPrice = BigDecimal.valueOf(35);
        pricedOption.setPricePerUser(oldPOUserPrice);

        // when
        service.updatePricedOption(voPricedOption, pricedOption);

        // then
        verify(service.priceModelAudit).editParameterOptionSubscriptionPrice(
                service.dm, pricedOption, oldPOSubPrice);
        verify(service.priceModelAudit).editParameterOptionUserPrice(
                service.dm, pricedOption, oldPOUserPrice);
    }

    @Test
    public void updatePricedProductRole_forPricedParameter() throws Exception {
        testUpdatePricedProductRole(null, new PricedParameter());
    }

    @Test
    public void updatePricedProductRole_forPricedParameter_LogNotRequired()
            throws ValidationException, ConcurrentModificationException {
        // given
        long voPriceModeKey = 0;
        VOPricedRole pricedProductRole = new VOPricedRole();
        PricedProductRole pprToUpdate = new PricedProductRole();
        BigDecimal oldPricePerUser = BigDecimal.valueOf(34);
        pprToUpdate.setPricePerUser(oldPricePerUser);

        RoleDefinition roleDefinition = new RoleDefinition();
        Organization targetCustomer = new Organization();
        Subscription subscription = new Subscription();

        // when
        service.updatePricedProductRole(voPriceModeKey, pricedProductRole,
                pprToUpdate, null, roleDefinition, new PricedParameter(),
                targetCustomer, subscription, false);

        // then

        verify(service.priceModelAudit, times(0)).editParameterUserRolePrice(
                service.dm, voPriceModeKey, pprToUpdate, oldPricePerUser);
    }

    private void testUpdatePricedProductRole(PriceModel priceModel,
            PricedParameter pricedParameter) throws ValidationException,
            ConcurrentModificationException {

        // given
        long voPriceModeKey = 1;
        VOPricedRole pricedProductRole = new VOPricedRole();
        PricedProductRole pprToUpdate = new PricedProductRole();
        BigDecimal oldPricePerUser = BigDecimal.valueOf(34);
        pprToUpdate.setPricePerUser(oldPricePerUser);

        RoleDefinition roleDefinition = new RoleDefinition();
        Organization targetCustomer = new Organization();
        Subscription subscription = new Subscription();

        // when
        service.updatePricedProductRole(voPriceModeKey, pricedProductRole,
                pprToUpdate, priceModel, roleDefinition, pricedParameter,
                targetCustomer, subscription, true);

        // then
        int userRolePriceCalled = priceModel != null ? 1 : 0;
        verify(service.priceModelAudit, times(userRolePriceCalled))
                .editServiceRolePrice(service.dm, voPriceModeKey, priceModel,
                        pprToUpdate, oldPricePerUser, targetCustomer,
                        subscription);

        int parameterUserRolePriceCalled = pricedParameter != null ? 1 : 0;
        verify(service.priceModelAudit, times(parameterUserRolePriceCalled))
                .editParameterUserRolePrice(service.dm, voPriceModeKey,
                        pprToUpdate, oldPricePerUser);

        int parameterOptionUserRolePriceCalled = (pricedParameter == null && priceModel == null) ? 1
                : 0;
        verify(service.priceModelAudit,
                times(parameterOptionUserRolePriceCalled))
                .editParameterOptionUserRolePrice(service.dm, pprToUpdate,
                        oldPricePerUser);
    }

    @Test
    public void updatePricedProductRole_forPriceModel() throws Exception {
        testUpdatePricedProductRole(new PriceModel(), null);
    }

    @Test
    public void updatePricedProductRole_forParameterOption() throws Exception {
        testUpdatePricedProductRole(null, null);
    }

    @Test
    public void createPricedProductRole_forPriceModel() throws Exception {
        testCreatePricedProductRole(new PriceModel(), null, null);
    }

    @Test
    public void createPricedProductRole_Bug10521() throws Exception {
        testCreatePricedProductRole_Bug10521(null, null, new PricedOption(),
                prepareOldPricedProductRoles(BigDecimal.valueOf(1)),
                BigDecimal.valueOf(1));
    }

    @Test
    public void createPricedProductRole_Bug10521_NullOldPricedProductRoles()
            throws Exception {
        testCreatePricedProductRole_Bug10521(null, null, new PricedOption(),
                null, DEFAULT_PRICE_VALUE);
    }

    @Test
    public void createPricedProductRole_Bug10521_EmptyOldPricedProductRoles()
            throws Exception {
        testCreatePricedProductRole_Bug10521(null, null, new PricedOption(),
                new ArrayList<PricedProductRole>(), DEFAULT_PRICE_VALUE);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void validateAndSetRolePricesForParam_NoTemplate() throws Exception {
        // given
        long voPriceModelKey = 0;
        boolean priceModelCreatedInTransaction = true;
        PriceModel priceModel = new PriceModel();
        VOPricedParameter voPP = new VOPricedParameter();
        PricedParameter pp = new PricedParameter();
        Organization targetCustomer = new Organization();
        Subscription subscription = new Subscription();
        boolean isTemplateExistsForCustomer = false;
        prepareDataForvalidateAndSetRolePricesForParam(priceModel, voPP, pp);
        // when
        service.validateAndSetRolePricesForParam(voPriceModelKey, priceModel,
                voPP, pp, priceModelCreatedInTransaction, targetCustomer,
                subscription, isTemplateExistsForCustomer);
        // then
        verify(service, times(1)).setRoleSpecificPrices(eq(voPriceModelKey),
                (PriceModel) isNull(), (PricedParameter) isNull(),
                eq(pp.getPricedOptionList().get(0)),
                eq(voPP.getPricedOptions().get(0).getRoleSpecificUserPrices()),
                anyBoolean(), eq(targetCustomer), eq(subscription),
                (List<PricedProductRole>) isNull());
    }

    @Test
    public void validateAndSetRolePricesForParam_TemplateExists()
            throws Exception {
        // given
        long voPriceModelKey = 0;
        boolean priceModelCreatedInTransaction = true;
        PriceModel priceModel = new PriceModel();
        VOPricedParameter voPP = new VOPricedParameter();
        PricedParameter pp = new PricedParameter();
        Organization targetCustomer = new Organization();
        Subscription subscription = new Subscription();
        boolean isTemplateExistsForCustomer = true;
        prepareDataForvalidateAndSetRolePricesForParam(priceModel, voPP, pp);
        // when
        service.validateAndSetRolePricesForParam(voPriceModelKey, priceModel,
                voPP, pp, priceModelCreatedInTransaction, targetCustomer,
                subscription, isTemplateExistsForCustomer);
        // then
        verify(service, times(1)).setRoleSpecificPrices(
                eq(voPriceModelKey),
                (PriceModel) isNull(),
                (PricedParameter) isNull(),
                eq(pp.getPricedOptionList().get(0)),
                eq(voPP.getPricedOptions().get(0).getRoleSpecificUserPrices()),
                anyBoolean(),
                eq(targetCustomer),
                eq(subscription),
                eq(priceModel.getSelectedParameters().get(0)
                        .getPricedOptionList().get(0)
                        .getRoleSpecificUserPrices()));
    }

    private void prepareDataForvalidateAndSetRolePricesForParam(
            PriceModel priceModel, VOPricedParameter voPP, PricedParameter pp)
            throws Exception {

        List<VOPricedOption> voPricedOptions = new ArrayList<VOPricedOption>();
        VOPricedOption voPricedOption = new VOPricedOption();
        voPricedOption.setRoleSpecificUserPrices(new ArrayList<VOPricedRole>());
        voPricedOption.setParameterOptionKey(0);
        voPricedOptions.add(voPricedOption);
        voPP.setPricedOptions(voPricedOptions);

        List<PricedOption> pricedOptions = new ArrayList<PricedOption>();
        PricedOption pricedOption = new PricedOption();
        pricedOption.setParameterOptionKey(0);
        pricedOption
                .setRoleSpecificUserPrices(new ArrayList<PricedProductRole>());
        pricedOptions.add(pricedOption);
        pp.setPricedOptionList(pricedOptions);

        List<PricedParameter> pricedParameters = new ArrayList<PricedParameter>();
        pricedParameters.add(pp);
        priceModel.setSelectedParameters(pricedParameters);

        doNothing().when(service).validatePricedProductRoles(
                anyListOf(VOPricedRole.class), any(Product.class));
        doNothing().when(service).setRoleSpecificPrices(anyLong(),
                any(PriceModel.class), any(PricedParameter.class),
                any(PricedOption.class), anyListOf(VOPricedRole.class),
                anyBoolean(), any(Organization.class), any(Subscription.class),
                anyListOf(PricedProductRole.class));
    }

    private List<PricedProductRole> prepareOldPricedProductRoles(
            BigDecimal oldPrice) {
        List<PricedProductRole> pricedProductRoles = new ArrayList<PricedProductRole>();
        PricedProductRole pricedProductRole = new PricedProductRole();
        RoleDefinition roleDefinition = new RoleDefinition();
        roleDefinition.setKey(0);
        pricedProductRole.setRoleDefinition(roleDefinition);
        pricedProductRole.setPricePerUser(oldPrice);
        pricedProductRoles.add(pricedProductRole);
        return pricedProductRoles;

    }

    private void testCreatePricedProductRole_Bug10521(PriceModel priceModel,
            PricedParameter pricedParameter, PricedOption pricedOption,
            List<PricedProductRole> oldPricedProductRoles, BigDecimal oldPrice)
            throws Exception {

        // given
        long voPriceModelKey = 0;
        VOPricedRole voPricedProductRole = new VOPricedRole();
        RoleDefinition roleDefinition = new RoleDefinition();
        roleDefinition.setKey(0);

        Organization targetCustomer = new Organization();
        Subscription subscription = new Subscription();
        // when
        PricedProductRole pricedProductRole = service.createPricedProductRole(
                voPriceModelKey, voPricedProductRole, priceModel,
                roleDefinition, pricedParameter, pricedOption, targetCustomer,
                subscription, oldPricedProductRoles);

        // then
        int parameterOptionUserRolePriceCalled = (pricedOption != null) ? 1 : 0;
        verify(service.priceModelAudit,
                times(parameterOptionUserRolePriceCalled))
                .editParameterOptionUserRolePrice(service.dm,
                        pricedProductRole, oldPrice);
    }

    private void testCreatePricedProductRole(PriceModel priceModel,
            PricedParameter pricedParameter, PricedOption pricedOption)
            throws Exception {

        // given
        long voPriceModelKey = 1;
        VOPricedRole voPricedProductRole = new VOPricedRole();
        RoleDefinition roleDefinition = new RoleDefinition();
        Organization targetCustomer = new Organization();
        Subscription subscription = new Subscription();

        // when
        PricedProductRole pricedProductRole = service.createPricedProductRole(
                voPriceModelKey, voPricedProductRole, priceModel,
                roleDefinition, pricedParameter, pricedOption, targetCustomer,
                subscription, new ArrayList<PricedProductRole>());

        // then
        int userRolePriceCalled = priceModel != null ? 1 : 0;
        verify(service.priceModelAudit, times(userRolePriceCalled))
                .editServiceRolePrice(service.dm, voPriceModelKey, priceModel,
                        pricedProductRole, DEFAULT_PRICE_VALUE, targetCustomer,
                        subscription);

        int parameterUserRolePriceCalled = pricedParameter != null ? 1 : 0;
        verify(service.priceModelAudit, times(parameterUserRolePriceCalled))
                .editParameterUserRolePrice(service.dm, voPriceModelKey,
                        pricedProductRole, DEFAULT_PRICE_VALUE);

        int parameterOptionUserRolePriceCalled = (pricedOption != null) ? 1 : 0;
        verify(service.priceModelAudit,
                times(parameterOptionUserRolePriceCalled))
                .editParameterOptionUserRolePrice(service.dm,
                        pricedProductRole, DEFAULT_PRICE_VALUE);
    }

    @Test
    public void createPricedProductRole_forPricedParameter() throws Exception {
        testCreatePricedProductRole(null, new PricedParameter(), null);
    }

    @Test
    public void createPricedProductRole_forPricedOption() throws Exception {
        testCreatePricedProductRole(null, null, new PricedOption());
    }

    @Test
    public void setPriceModelToFree() {
        // given
        VOPriceModel voPriceModel = new VOPriceModel();
        voPriceModel.setKey(54L);
        PriceModel priceModel = new PriceModel();
        priceModel.setType(PriceModelType.PER_UNIT);

        // when
        service.setPriceModelToFree(voPriceModel, priceModel);

        // then
        verify(service.priceModelAudit).editPriceModelTypeToFree(
                eq(service.dm), eq(priceModel), eq(54L),
                eq(PriceModelType.PER_UNIT));
    }

    @Test
    public void setPriceModelToChargeable() throws Exception {
        // given
        VOPriceModel voPriceModel = new VOPriceModel();
        voPriceModel.setKey(54L);
        PriceModel priceModel = new PriceModel();
        Product product = new Product();
        priceModel.setProduct(product);
        SupportedCurrency currency = new SupportedCurrency("USD");
        priceModel.setCurrency(currency);
        priceModel.setType(PriceModelType.PER_UNIT);
        priceModel.setPeriod(PRICINGPERIOD_1);
        priceModel.setFreePeriod(FREEPERIOD_1);
        Organization targetCustomer = new Organization();
        Subscription subscription = new Subscription();

        mockSetPriceModelToChargeable(voPriceModel, priceModel, product,
                targetCustomer, subscription);

        // when
        service.setPriceModelToChargeable(voPriceModel, priceModel, true, true,
                ServiceType.TEMPLATE, targetCustomer, subscription, false);

        // then
        verify(service.priceModelAudit).editPriceModelTypeToChargeable(
                eq(service.dm), eq(priceModel), eq(54L), eq(currency),
                eq(PriceModelType.PER_UNIT), eq(FREEPERIOD_1),
                eq(PRICINGPERIOD_1));
    }

    private void mockSetPriceModelToChargeable(VOPriceModel voPriceModel,
            PriceModel priceModel, Product product, Organization organization,
            Subscription subscription) throws Exception {
        doNothing().when(service).setEvents(voPriceModel, priceModel, product,
                true);

        doNothing().when(service).validatePricedProductRoles(
                anyListOf(VOPricedRole.class), eq(product));

        doNothing().when(service).setRoleSpecificPrices(
                eq(voPriceModel.getKey()), eq(priceModel),
                any(PricedParameter.class), any(PricedOption.class),
                anyListOf(VOPricedRole.class), eq(Boolean.TRUE),
                eq(organization), eq(subscription),
                anyListOf(PricedProductRole.class));
    }

    @Test
    public void removePricedEvents() {
        // given
        long voPriceModelKey = 1;
        Collection<PricedEvent> pricedEvents = new ArrayList<PricedEvent>();
        PricedEvent pricedEvent = new PricedEvent();
        pricedEvents.add(pricedEvent);

        // when
        service.removePricedEvents(1, pricedEvents);

        // then
        verify(service.priceModelAudit).removeEventPrice(service.dm,
                voPriceModelKey, pricedEvent);
    }

    @Test
    public void removePricedParameters() {
        // given
        long voPriceModelKey = 1;
        Collection<PricedParameter> pricedParameters = new ArrayList<PricedParameter>();
        PricedParameter pricedParameter = new PricedParameter();
        pricedParameters.add(pricedParameter);
        PriceModel priceModel = new PriceModel();

        // when
        service.removePricedParameters(voPriceModelKey, pricedParameters,
                priceModel);

        // then
        verify(service.priceModelAudit).removeParameterSubscriptionPrice(
                service.dm, voPriceModelKey, pricedParameter);
    }

    @Test
    public void setRoleSpecificPrices_bug10674_LogNotRequired() throws Exception {
        // given
        long voPriceModelKey = 0;
        boolean priceModelCreatedInTransaction = false;
        // when
        service.setRoleSpecificPrices(voPriceModelKey, null,
                preparePricedParameter(), null, prepareVOPricedRoles(),
                priceModelCreatedInTransaction, new Organization(), null, null);

        // then
        verify(service, times(1)).updatePricedProductRole(eq(voPriceModelKey),
                any(VOPricedRole.class), any(PricedProductRole.class),
                (PriceModel) isNull(), any(RoleDefinition.class),
                any(PricedParameter.class), any(Organization.class),
                (Subscription) isNull(), eq(false));
    }

    @Test
    public void setRoleSpecificPrices_bug10674_NeedLog() throws Exception {
        // given
        long voPriceModelKey = 1L;
        boolean priceModelCreatedInTransaction = false;
        // when
        service.setRoleSpecificPrices(voPriceModelKey, null,
                preparePricedParameter(), null, prepareVOPricedRoles(),
                priceModelCreatedInTransaction, new Organization(), null, null);

        // then
        verify(service, times(1)).updatePricedProductRole(eq(voPriceModelKey),
                any(VOPricedRole.class), any(PricedProductRole.class),
                (PriceModel) isNull(), any(RoleDefinition.class),
                any(PricedParameter.class), any(Organization.class),
                (Subscription) isNull(), eq(true));
    }

    private PricedParameter preparePricedParameter() {
        PricedParameter pricedParameter = new PricedParameter();
        PriceModel priceModel = new PriceModel();
        Product product = new Product();
        TechnicalProduct technicalProduct = new TechnicalProduct();
        technicalProduct.setRoleDefinitions(prepareRoleDefinitions());
        product.setTechnicalProduct(technicalProduct);
        priceModel.setProduct(product);
        pricedParameter.setPriceModel(priceModel);
        pricedParameter.setRoleSpecificUserPrices(preparePricedProductRoles());
        return pricedParameter;
    }

    private List<VOPricedRole> prepareVOPricedRoles() {
        List<VOPricedRole> vOPricedRoles = new ArrayList<VOPricedRole>();
        vOPricedRoles.add(prepareVOPricedRole(10000L));
        return vOPricedRoles;

    }

    private VOPricedRole prepareVOPricedRole(long key) {
        VOPricedRole vOPricedRole = new VOPricedRole();
        vOPricedRole.setKey(key);
        VORoleDefinition vORoleDefinition = new VORoleDefinition();
        vORoleDefinition.setKey(key);
        vOPricedRole.setRole(vORoleDefinition);
        return vOPricedRole;

    }

    private List<RoleDefinition> prepareRoleDefinitions() {
        List<RoleDefinition> roleDefinitions = new ArrayList<RoleDefinition>();
        roleDefinitions.add(prepareRoleDefinition(10000L));
        return roleDefinitions;
    }

    private RoleDefinition prepareRoleDefinition(long key) {
        RoleDefinition roleDefinition = new RoleDefinition();
        roleDefinition.setKey(key);
        return roleDefinition;
    }

    private List<PricedProductRole> preparePricedProductRoles() {
        List<PricedProductRole> pricedProductRole = new ArrayList<PricedProductRole>();
        pricedProductRole.add(preparePricedProductRole(10000L));
        return pricedProductRole;
    }

    private PricedProductRole preparePricedProductRole(long key) {
        PricedProductRole pricedProductRole = new PricedProductRole();
        pricedProductRole.setKey(key);
        pricedProductRole.setRoleDefinition(prepareRoleDefinition(10000L));
        return pricedProductRole;
    }
}
