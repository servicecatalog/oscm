/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: goebel                                    
 *                                                                              
 *  Creation Date: October 5th, 2012                                                      
 *                                                                              
 *  Completion Time: October 5th, 2012                                            
 *                                                                              
 *******************************************************************************/

package org.oscm.subscriptionservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.SessionContext;
import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.oscm.applicationservice.local.ApplicationServiceLocal;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.DomainObject;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.Parameter;
import org.oscm.domobjects.ParameterDefinition;
import org.oscm.domobjects.ParameterSet;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.PriceModel;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.TriggerDefinition;
import org.oscm.domobjects.TriggerProcess;
import org.oscm.domobjects.TriggerProcessParameter;
import org.oscm.encrypter.AESEncrypter;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.internal.intf.TriggerService;
import org.oscm.internal.types.enumtypes.ParameterModificationType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.ServiceType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.SubscriptionMigrationException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.*;
import org.oscm.serviceprovisioningservice.assembler.ParameterAssembler;
import org.oscm.subscriptionservice.auditlog.SubscriptionAuditLogCollector;
import org.oscm.subscriptionservice.dao.ModifiedEntityDao;
import org.oscm.subscriptionservice.dao.OrganizationDao;
import org.oscm.subscriptionservice.dao.ProductDao;
import org.oscm.subscriptionservice.dao.SessionDao;
import org.oscm.taskhandling.local.TaskMessage;
import org.oscm.taskhandling.local.TaskQueueServiceLocal;
import org.oscm.tenantprovisioningservice.bean.TenantProvisioningServiceBean;
import org.oscm.tenantprovisioningservice.vo.TenantProvisioningResult;
import org.oscm.triggerservice.local.TriggerMessage;
import org.oscm.triggerservice.local.TriggerQueueServiceLocal;
import org.oscm.types.enumtypes.TriggerProcessParameterName;

/**
 * Unit test testing the updating of service parameter in context of
 * subscription up- and down-grading. {@link SubscriptionServiceBean}.
 * 
 * @author goebel
 */
public class SubscriptionServiceBeanUpdateParameterTest {
    private static final long USER_KEY = 1111;
    private static final String USER_LOCAL = "de";

    private SubscriptionServiceBean bean;
    private TerminateSubscriptionBean terminateBean;
    private ManageSubscriptionBean manageBean;
    private SubscriptionUtilBean utilBean;
    private ModifyAndUpgradeSubscriptionBean modifyAndUpgradeBean;
    private ProductDao productDao;
    private SessionDao sessionDao;
    private OrganizationDao orgDao = mock(OrganizationDao.class);
    private List<PlatformUser> givenUsers = new ArrayList<>();

    @Captor
    ArgumentCaptor<List<Parameter>> voOrgCaptor;

    @Before
    public void setup() throws Exception {
        AESEncrypter.generateKey();
        MockitoAnnotations.initMocks(this);
        bean = spy(new SubscriptionServiceBean() {
            @Override
            List<VOUda> getUdasForCustomer(String targetType,
                    long targetObjectKey, Organization supplier)
                    throws ValidationException, ObjectNotFoundException,
                    OperationNotPermittedException {
                return new ArrayList<>();
            }
        });
        DataService dsMock = mock(DataService.class);
        bean.dataManager = dsMock;
        bean.audit = mock(SubscriptionAuditLogCollector.class);
        bean.localizer = mock(LocalizerServiceLocal.class);
        bean.tenantProvisioning = mock(TenantProvisioningServiceBean.class);
        bean.triggerQS = mock(TriggerQueueServiceLocal.class);
        bean.tqs = mock(TaskQueueServiceLocal.class);
        bean.appManager = mock(ApplicationServiceLocal.class);
        bean.localizer = mock(LocalizerServiceLocal.class);
        productDao = mock(ProductDao.class);
        sessionDao = mock(SessionDao.class);

        terminateBean = spy(new TerminateSubscriptionBean());
        terminateBean.dataManager = bean.dataManager;
        terminateBean.audit = bean.audit;
        terminateBean.tqs = bean.tqs;
        terminateBean.appManager = bean.appManager;
        bean.terminateBean = terminateBean;

        manageBean = spy(new ManageSubscriptionBean());
        manageBean.dataManager = bean.dataManager;
        manageBean.audit = bean.audit;
        bean.manageBean = manageBean;
        bean.stateValidator = new ValidateSubscriptionStateBean();
        bean.stateValidator.sessionCtx = mock(SessionContext.class);

        utilBean = spy(new SubscriptionUtilBean());
        utilBean.dataManager = bean.dataManager;

        modifyAndUpgradeBean = spy(new ModifyAndUpgradeSubscriptionBean());
        bean.modUpgBean = modifyAndUpgradeBean;
        doReturn(orgDao).when(bean.modUpgBean).getOrganizationDao();
        doReturn(givenUsers).when(orgDao).getOrganizationAdmins(anyLong());

        Query query = mock(Query.class);
        doReturn(sessionDao).when(bean).getSessionDao();
        doReturn(productDao).when(bean).getProductDao();
        doReturn(query).when(bean.dataManager).createNamedQuery(anyString());
        doReturn(Long.valueOf(0)).when(query).getSingleResult();
        // Current user
        PlatformUser user = new PlatformUser();
        user.setLocale("en");
        Organization organization = new Organization();
        user.setOrganization(organization);
        doReturn(user).when(bean.dataManager).getCurrentUser();
        doReturn(givenPlatformUser(USER_KEY, USER_LOCAL)).when(bean.dataManager)
                .getReference(eq(PlatformUser.class), eq(USER_KEY));
        setupProductTemplate();

        TenantProvisioningResult result = new TenantProvisioningResult();
        result.setAsyncProvisioning(true);
        doReturn(result).when(bean.tenantProvisioning)
                .createProductInstance(any(Subscription.class));
    }

    private void setupProductTemplate() throws Exception {
        Product productTemplate = new Product();
        productTemplate.setVendor(new Organization());
        TechnicalProduct technicalProduct = new TechnicalProduct();
        technicalProduct.setOnlyOneSubscriptionAllowed(false);
        productTemplate.setTechnicalProduct(technicalProduct);
        productTemplate.setStatus(ServiceStatus.ACTIVE);
        productTemplate.setType(ServiceType.TEMPLATE);
        PriceModel pricemodel = new PriceModel();
        productTemplate.setPriceModel(pricemodel);
        doReturn(productTemplate).when(bean.dataManager)
                .getReference(eq(Product.class), anyLong());
    }

    @Test
    public void subscribeToServiceInt_Bug10262() throws Exception {
        // given
        TriggerProcess trigger = givenTriggerProcess(Boolean.FALSE);

        doNothing().when(bean.triggerQS)
                .sendAllNonSuspendingMessages(anyListOf(TriggerMessage.class));
        doNothing().when(bean.tqs)
                .sendAllMessages(anyListOf(TaskMessage.class));
        // when
        bean.subscribeToServiceInt(trigger);
        // then
        assertSubscriptionParameterConfigurationUpdated(0);
    }

    @Test
    public void subscribeToServiceInt_Bug11006() throws Exception {
        // given
        TriggerProcess trigger = givenTriggerProcess(Boolean.FALSE);

        doNothing().when(bean.triggerQS)
                .sendAllNonSuspendingMessages(anyListOf(TriggerMessage.class));
        doNothing().when(bean.tqs)
                .sendAllMessages(anyListOf(TaskMessage.class));
        doReturn(givenPlatformUser(USER_KEY, USER_LOCAL)).when(bean.dataManager)
                .getReferenceByBusinessKey(any(PlatformUser.class));
        // when
        Subscription result = bean.subscribeToServiceInt(trigger);
        // then
        assertEquals(USER_KEY, result.getOwner().getKey());
        assertEquals(USER_LOCAL, result.getOwner().getLocale());

    }

    @Test
    public void subscribeToServiceInt_Bug11028() throws Exception {
        // given
        TriggerProcess trigger = givenTriggerProcess(Boolean.TRUE);

        doNothing().when(bean.triggerQS)
                .sendAllNonSuspendingMessages(anyListOf(TriggerMessage.class));
        doNothing().when(bean.tqs)
                .sendAllMessages(anyListOf(TaskMessage.class));
        doReturn(givenPlatformUser(USER_KEY, USER_LOCAL)).when(bean.dataManager)
                .getReferenceByBusinessKey(any(PlatformUser.class));
        doReturn(new Subscription()).when(bean.manageBean)
                .loadSubscription(anyString(), anyLong());
        doNothing().when(bean).addRevokeUserInt(any(TriggerProcess.class));

        // when
        Subscription result = bean.subscribeToServiceInt(trigger);
        // then
        assertEquals(USER_KEY, result.getOwner().getKey());
        assertEquals(USER_LOCAL, result.getOwner().getLocale());

    }

    @Test
    public void modifySubscriptionInt_OK() throws Exception {
        // given
        TriggerProcess trigger = givenTriggerProcess(Boolean.FALSE);

        Organization organization = new Organization();
        doReturn(givenUser(organization)).when(bean.dataManager)
                .getCurrentUser();

        doReturn(givenSubscription(organization)).when(bean.dataManager)
                .getReference(eq(Subscription.class), anyLong());
        doReturn(Boolean.TRUE).when(bean).checkIfParametersAreModified(
                any(Subscription.class), any(Subscription.class),
                any(Product.class), any(Product.class),
                anyListOf(VOParameter.class), anyBoolean());

        doReturn(mock(ModifiedEntityDao.class)).when(bean)
                .getModifiedEntityDao();

        TriggerService ts = mock(TriggerService.class);
        List<VOTriggerProcess> triggerProcesses = new ArrayList<>();
        bean.triggerService = ts;
        when(ts.getAllActionsForOrganizationRelatedSubscription()).thenReturn(triggerProcesses);

        // when
        bean.modifySubscriptionInt(trigger);

        // then
        assertSubscriptionParameterConfigurationUpdated(0);
    }

    @Test
    public void upgradeSubscriptionInt_OK() throws Exception {
        // given
        TriggerProcess trigger = givenTriggerProcess(Boolean.FALSE);
        Organization organization = new Organization();
        doReturn(givenUser(organization)).when(bean.dataManager)
                .getCurrentUser();
        doReturn(givenProducts(organization)).when(bean)
                .replaceByCustomerSpecificProducts(anyListOf(Product.class),
                        anyLong(), any(Organization.class));

        doReturn(givenSubscription(organization)).when(bean.dataManager)
                .getReferenceByBusinessKey(any(DomainObject.class));
        doReturn(Boolean.TRUE).when(bean).checkIfParametersAreModified(
                any(Subscription.class), any(Subscription.class),
                any(Product.class), any(Product.class),
                anyListOf(VOParameter.class), anyBoolean());

        // when
        bean.upgradeSubscriptionInt(trigger);

        // then
        assertSubscriptionParameterConfigurationUpdated(0);

    }

    @Test
    public void upgradeSubscriptionInt_AuditLog() throws Exception {
        // given
        TriggerProcess trigger = givenTriggerProcess(Boolean.FALSE);
        Organization organization = new Organization();
        ArgumentCaptor<Product> captorInitialProduct = ArgumentCaptor
                .forClass(Product.class);
        ArgumentCaptor<DataService> captorDataService = ArgumentCaptor
                .forClass(DataService.class);
        ArgumentCaptor<Product> captorTargetProduct = ArgumentCaptor
                .forClass(Product.class);
        ArgumentCaptor<Subscription> captorSubscription = ArgumentCaptor
                .forClass(Subscription.class);
        Product initialProduct = givenSubscription(organization).getProduct();
        Product targetProduct = createProduct("targetProduct");
        Subscription subscription = givenSubscription(organization);

        doReturn(targetProduct).when(bean.dataManager)
                .getReference(eq(Product.class), anyLong());
        doReturn(givenUser(organization)).when(bean.dataManager)
                .getCurrentUser();
        doReturn(givenProducts(organization)).when(bean)
                .replaceByCustomerSpecificProducts(anyListOf(Product.class),
                        anyLong(), any(Organization.class));

        doReturn(givenSubscription(organization)).when(bean.dataManager)
                .getReferenceByBusinessKey(any(DomainObject.class));
        doReturn(Boolean.TRUE).when(bean).checkIfParametersAreModified(
                any(Subscription.class), any(Subscription.class),
                any(Product.class), any(Product.class),
                anyListOf(VOParameter.class), anyBoolean());

        // when
        bean.upgradeSubscriptionInt(trigger);

        // then
        verify(bean.audit).upDowngradeSubscription(captorDataService.capture(),
                captorSubscription.capture(), captorInitialProduct.capture(),
                captorTargetProduct.capture());
        assertEquals(initialProduct.getProductId(),
                captorInitialProduct.getValue().getProductId());
        assertEquals(targetProduct.getProductId(),
                captorTargetProduct.getValue().getProductId());
        assertEquals(subscription.getSubscriptionId(),
                captorSubscription.getValue().getSubscriptionId());

    }

    @Test
    public void updateConfiguredParameterValues_ConfigurableParameter() {
        // given
        Product product = givenProductWithParamsHavingDefaultValues("Param_ID1",
                "Param_ID2");
        Subscription subscription = givenSubscriptionWithParamsHavingOtherVaues(
                "Param_ID1", "Param_ID2");
        List<VOParameter> parameters = givenConfigurableStandardParametersToUpdate(
                "Param_ID1", "Param_ID2");
        // when
        List<Parameter> result = bean.updateConfiguredParameterValues(product,
                parameters, subscription);

        // then
        assertEquals(2, result.size());
    }

    @Test
    public void updateConfiguredParameterValues_NonConfigurableParameter() {
        // given product, target subscription and non configurable parameters
        Product product = givenProductWithParamsHavingDefaultValues("Param_ID1",
                "Param_ID2");
        Subscription subscription = givenSubscriptionWithParamsHavingOtherVaues(
                "Param_ID1", "Param_ID2");

        List<VOParameter> parameters = givenNonConfigurableStandardParametersToUpdate(
                "Param_ID1", "Param_ID2");
        // when
        List<Parameter> result = bean.updateConfiguredParameterValues(product,
                parameters, subscription);

        // then ensure parameters are not updated
        assertEquals(2, result.size());

    }

    @Test
    public void updateConfiguredParameterValues_NonConfigurableOneTimeParameter() {
        // given product, target subscription and non configurable parameters
        Product product = givenProductWithParamsHavingDefaultValues(
                "Param_ID1");
        Subscription subscription = givenSubscriptionWithParamsHavingOtherVaues(
                "Param_ID1");

        List<VOParameter> parameters = givenNonConfigurableOneTimeParametersToUpdate(
                "Param_ID1");
        // when
        List<Parameter> result = bean.updateConfiguredParameterValues(product,
                parameters, subscription);

        // then
        assertEquals(1, result.size());
    }

    /**
     * Upgrade to a service with additional parameters must be possible. Avoid
     * NPE if cached parameter is not found. <br>
     * Part 1 - non configurable target parameters
     */
    @Test
    public void updateConfiguredParameterValues_AdditionalParam_B9422_1() {
        // given product, parameters and a subscription having a new parameter
        Product product = givenProductWithParamsHavingDefaultValues("Param_ID1",
                "Param_ID2");
        Subscription subscription = givenSubscriptionWithParamsHavingOtherVaues(
                "Param_ID1", "Param_ID2");

        List<VOParameter> parameters = givenNonConfigurableStandardParametersToUpdate(
                "Param_ID1", "Param_ID2", "Param_ID3");

        // when
        List<Parameter> result = bean.updateConfiguredParameterValues(product,
                parameters, subscription);

        // then
        assertEquals(2, result.size());
    }

    /**
     * Upgrade to a service with additional parameters must be possible. Avoid
     * NPE if cached parameter is not found.<br>
     * Part 2 - configurable target parameters
     */
    @Test
    public void updateConfiguredParameterValues_AdditionalParam_B9422_2() {
        // given product, parameters and a subscription having a new parameter
        Product product = givenProductWithParamsHavingDefaultValues("Param_ID1",
                "Param_ID2");
        Subscription subscription = givenSubscriptionWithParamsHavingOtherVaues(
                "Param_ID1", "Param_ID2");

        List<VOParameter> parameters = givenConfigurableStandardParametersToUpdate(
                "Param_ID1", "Param_ID2", "Param_ID3");

        // when
        List<Parameter> result = bean.updateConfiguredParameterValues(product,
                parameters, subscription);

        // then
        assertEquals(2, result.size());
    }

    @Test(expected = SubscriptionMigrationException.class)
    public void checkIfParametersAreModified_UpOrDowngradeNotValid()
            throws Exception {
        // given product, parameters and a subscription having a new parameter
        Product product = givenProductWithParamsHavingDefaultValues(
                "Param_ID1");
        Subscription subscription = givenSubscriptionWithParamsHavingOtherVaues(
                "Param_ID1");
        List<VOParameter> parameters = givenNonConfigurableStandardParametersToUpdate(
                "Param_ID1");

        doNothing().when(bean).verifyIfParameterConcurrentlyChanged(
                any(Product.class), anyListOf(VOParameter.class), anyBoolean());

        doReturn(Boolean.valueOf(false)).when(bean)
                .isParameterUpOrDowngradeValid(any(Parameter.class),
                        any(VOParameter.class));
        // when
        bean.checkIfParametersAreModified(subscription, subscription, product,
                product, parameters, true);

    }

    @Test
    public void checkIfParametersAreModified_UpOrDowngradeValid()
            throws Exception {
        // given product, parameters and a subscription having a new parameter
        Product product = givenProductWithParamsHavingDefaultValues(
                "Param_ID1");
        Subscription subscription = givenSubscriptionWithParamsHavingOtherVaues(
                "Param_ID1");

        List<VOParameter> parameters = givenNonConfigurableStandardParametersToUpdate(
                "Param_ID1");
        doNothing().when(bean).verifyIfParameterConcurrentlyChanged(
                any(Product.class), anyListOf(VOParameter.class), anyBoolean());

        doReturn(Boolean.valueOf(true)).when(bean)
                .isParameterUpOrDowngradeValid(any(Parameter.class),
                        any(VOParameter.class));
        // when
        bean.checkIfParametersAreModified(subscription, subscription, product,
                product, parameters, true);

        // then
        assertParametersNotUpdated(parameters, product);
    }

    @Test
    public void isParameterUpOrDowngradeValid_Standard_SameValues() {
        // Given one time parameter with different values
        Parameter dbParameter = createParameter("Parm1", "23",
                ParameterModificationType.STANDARD, false);

        VOParameter targetParam = toVO(createParameter("Parm1", "23",
                ParameterModificationType.STANDARD, true));

        assertTrue(
                bean.isParameterUpOrDowngradeValid(dbParameter, targetParam));
    }

    @Test
    public void isParameterUpOrDowngradeValid_Standard_DiffValues() {
        // Given one time parameter with different values
        Parameter dbParameter = createParameter("Parm1", "23",
                ParameterModificationType.STANDARD, false);

        VOParameter targetParam = toVO(createParameter("Parm1", "24",
                ParameterModificationType.STANDARD, true));

        assertFalse(
                bean.isParameterUpOrDowngradeValid(dbParameter, targetParam));
    }

    @Test
    public void isParameterUpOrDowngradeValid_OneTime_SameValues() {
        // Given one time parameter with different values
        Parameter dbParameter = createParameter("Parm1", "23",
                ParameterModificationType.ONE_TIME, false);

        VOParameter targetParam = toVO(createParameter("Parm1", "23",
                ParameterModificationType.ONE_TIME, true));

        assertTrue(
                bean.isParameterUpOrDowngradeValid(dbParameter, targetParam));
    }

    @Test
    public void isParameterUpOrDowngradeValid_OneTime_DiffValues() {
        // Given one time parameter with different values
        Parameter dbParameter = createParameter("Parm1", "23",
                ParameterModificationType.ONE_TIME, false);

        VOParameter targetParam = toVO(createParameter("Parm1", "24",
                ParameterModificationType.ONE_TIME, true));

        assertTrue(
                bean.isParameterUpOrDowngradeValid(dbParameter, targetParam));
    }

    @Test
    public void isParameterUpOrDowngradeValid_OneTime_DiffModType() {
        // Given parameters with different modification types
        Parameter dbParameter = createParameter("Parm1", "23",
                ParameterModificationType.STANDARD, false);

        VOParameter targetParam = toVO(createParameter("Parm1", "23",
                ParameterModificationType.ONE_TIME, true));
        // when
        boolean result = bean.isParameterUpOrDowngradeValid(dbParameter,
                targetParam);

        // then
        assertFalse(result);
    }

    @Test
    public void isParameterUpOrDowngradeValid_OneTime_DiffID() {
        // Given parameters with different IDs
        Parameter dbParameter = createParameter("ID", "23",
                ParameterModificationType.STANDARD, false);

        VOParameter targetParam = toVO(createParameter("ID_OTHER", "23",
                ParameterModificationType.ONE_TIME, true));
        // when
        boolean result = bean.isParameterUpOrDowngradeValid(dbParameter,
                targetParam);

        // then
        assertFalse(result);
    }

    @Test
    public void checkIfParametersAreModified_ValidationException()
            throws Exception {
        // given product with one time parameter
        Product product = givenProductWithOneTimeParamsHavingDefaultValues(
                "Param_ID1");
        Subscription subscription = givenSubscriptionWithParamsHavingOtherVaues(
                "Param_ID1");
        List<VOParameter> parameters = givenConfigurableOneTimeParametersToUpdate(
                "Param_ID1");
        doNothing().when(bean).verifyIfParameterConcurrentlyChanged(
                any(Product.class), anyListOf(VOParameter.class), anyBoolean());
        try {
            // when check modification for update
            bean.checkIfParametersAreModified(subscription, subscription,
                    product, product, parameters, false);
            fail("ValidationException expected");
        } catch (ValidationException ve) {
            assertEquals(
                    ValidationException.ReasonEnum.ONE_TIME_PARAMETER_NOT_ALLOWED,
                    ve.getReason());
        }

    }

    private VOParameter toVO(Parameter parameter) {
        ParameterSet paramSet = new ParameterSet();
        List<Parameter> params = new ArrayList<>();
        params.add(parameter);
        paramSet.setParameters(params);
        return ParameterAssembler.toVOParameters(paramSet,
                new LocalizerFacade(bean.localizer, "en")).get(0);
    }

    private Product givenProductWithParamsHavingDefaultValues(String... ids) {
        return givenProductWithParameter(ParameterModificationType.STANDARD,
                "default", ids);
    }

    private Product givenProductWithOneTimeParamsHavingDefaultValues(
            String... ids) {
        return givenProductWithParameter(ParameterModificationType.ONE_TIME,
                "default", ids);
    }

    private Product givenProductWithParameter(String defaultValue,
            String... ids) {
        return givenProductWithParameter(ParameterModificationType.STANDARD,
                defaultValue, ids);
    }

    private Product givenProductWithParameter(ParameterModificationType modType,
            String defaultValue, String... ids) {
        Product product = new Product();
        ParameterSet productParams = new ParameterSet();
        List<Parameter> paramList = new ArrayList<>();
        for (String id : ids) {
            paramList.add(createParameter(id, defaultValue, modType, true));
        }
        productParams.setParameters(paramList);
        product.setParameterSet(productParams);
        return product;
    }

    private Parameter createParameter(String id, String defaultValue,
            ParameterModificationType modtype, boolean configurable) {
        ParameterDefinition def = new ParameterDefinition();
        def.setParameterId(id);
        def.setModificationType(modtype);
        Parameter param = new Parameter();
        param.setValue(defaultValue);
        param.setConfigurable(configurable);
        param.setParameterDefinition(def);
        return param;
    }

    private List<VOParameter> givenParametersToUpdate(String newValue,
            boolean configurable, ParameterModificationType modType,
            String... ids) {
        List<VOParameter> paramList = new ArrayList<>();
        for (String id : ids) {
            paramList.add(
                    toVO(createParameter(id, newValue, modType, configurable)));
        }
        return paramList;
    }

    private List<VOParameter> givenConfigurableStandardParametersToUpdate(
            String... ids) {
        return givenParametersToUpdate("newValue", true,
                ParameterModificationType.STANDARD, ids);
    }

    private List<VOParameter> givenConfigurableOneTimeParametersToUpdate(
            String... ids) {
        return givenParametersToUpdate("newValue", true,
                ParameterModificationType.ONE_TIME, ids);
    }

    private List<VOParameter> givenNonConfigurableStandardParametersToUpdate(
            String... ids) {
        return givenParametersToUpdate("newValue", false,
                ParameterModificationType.STANDARD, ids);
    }

    private List<VOParameter> givenNonConfigurableOneTimeParametersToUpdate(
            String... ids) {
        return givenParametersToUpdate("newValue", false,
                ParameterModificationType.ONE_TIME, ids);
    }

    private Subscription givenSubscriptionWithParamsHavingOtherVaues(
            String... paramIds) {
        Product targetProduct = givenProductWithParameter("otherValue",
                paramIds);
        Subscription subscription = new Subscription();
        subscription.setProduct(targetProduct);
        subscription.setSubscriptionId("subscriptionId");
        return subscription;
    }

    private void assertParametersNotUpdated(List<VOParameter> parameterList,
            Product product) {
        if (checkUpdated(parameterList, product)) {
            fail("parameters were updated.");
        }
    }

    private void assertSubscriptionParameterConfigurationUpdated(
            int changedNumber) {
        verify(bean.audit, times(1)).editSubscriptionParameterConfiguration(
                eq(bean.dataManager), any(Product.class),
                voOrgCaptor.capture());
        assertEquals(changedNumber, voOrgCaptor.getValue().size());
    }

    private boolean checkUpdated(List<VOParameter> parameterList,
            Product product) {
        for (Parameter parameter : product.getParameterSet().getParameters()) {
            VOParameter voparameter = findParameter(parameterList,
                    parameter.getParameterDefinition().getParameterId());
            if (voparameter != null) {
                String value = parameter.getValue();
                if (!value.equals(voparameter.getValue())) {
                    return false;
                }
            }
        }
        return true;
    }

    private VOParameter findParameter(List<VOParameter> list, String id) {
        for (VOParameter voparameter : list) {
            if (!voparameter.getParameterDefinition().getParameterId()
                    .equals(id)) {
                continue;
            }
            return voparameter;
        }
        return null;
    }

    private TriggerProcess givenTriggerProcess(Boolean autoAssign) {

        TriggerProcess trigger = new TriggerProcess();
        TriggerDefinition triggerDef = new TriggerDefinition();

        trigger.setTriggerDefinition(triggerDef);

        VOSubscription sub = new VOSubscription();
        sub.setSubscriptionId("123");

        VOService product = new VOService();
        product.setAutoAssignUserEnabled(autoAssign);

        List<TriggerProcessParameter> paraList = new ArrayList<>();
        paraList.add(initTriggerParameter(
                TriggerProcessParameterName.SUBSCRIPTION, sub));
        paraList.add(initTriggerParameter(TriggerProcessParameterName.PRODUCT,
                product));
        paraList.add(initTriggerParameter(
                TriggerProcessParameterName.PAYMENTINFO, new VOPaymentInfo()));
        paraList.add(initTriggerParameter(
                TriggerProcessParameterName.BILLING_CONTACT,
                new VOBillingContact()));
        paraList.add(initTriggerParameter(TriggerProcessParameterName.UDAS,
                new ArrayList<>()));
        paraList.add(initTriggerParameter(TriggerProcessParameterName.USERS,
                new ArrayList<>()));
        paraList.add(initTriggerParameter(
                TriggerProcessParameterName.PARAMETERS, new ArrayList<>()));

        trigger.setTriggerProcessParameters(paraList);
        trigger.setUser(givenPlatformUser(USER_KEY, USER_LOCAL));

        return trigger;
    }

    private TriggerProcessParameter initTriggerParameter(
            TriggerProcessParameterName name, Object object) {
        TriggerProcessParameter para = new TriggerProcessParameter();
        para.setName(name);
        para.setValue(object);
        return para;
    }

    private PlatformUser givenUser(Organization organization) {
        PlatformUser user = new PlatformUser();
        user.setOrganization(organization);
        return user;
    }

    private Subscription givenSubscription(Organization organization) {
        Subscription subscription = new Subscription();
        subscription.setOrganization(organization);
        subscription.setSubscriptionId("222");
        subscription.setStatus(SubscriptionStatus.ACTIVE);

        Organization verdor = new Organization();
        Product pro = new Product();
        pro.setType(ServiceType.TEMPLATE);
        pro.setProductId("initialProduct");
        pro.setAutoAssignUserEnabled(Boolean.FALSE);
        pro.setVendor(verdor);
        TechnicalProduct technicalproduct = new TechnicalProduct();
        pro.setTechnicalProduct(technicalproduct);
        subscription.setProduct(pro);

        return subscription;
    }

    private PlatformUser givenPlatformUser(long key, String local) {
        PlatformUser user = new PlatformUser();
        user.setLocale(local);
        user.setKey(key);
        Organization organization = new Organization();
        user.setOrganization(organization);

        return user;
    }

    private List<Product> givenProducts(Organization organization) {
        Subscription result = new Subscription();
        result.setOrganization(organization);
        Product currentProduct = new Product();
        currentProduct.setType(ServiceType.TEMPLATE);
        result.setProduct(currentProduct);

        List<Product> pros = new ArrayList<>();
        pros.add(new Product());
        return pros;
    }

    public Product createProduct(String productID) {
        Product product = new Product();
        product.setType(ServiceType.TEMPLATE);
        product.setVendor(new Organization());
        product.setProductId(productID);
        TechnicalProduct technicalProduct = new TechnicalProduct();
        technicalProduct.setOnlyOneSubscriptionAllowed(false);
        product.setTechnicalProduct(technicalProduct);
        product.setStatus(ServiceStatus.ACTIVE);
        PriceModel pricemodel = new PriceModel();
        product.setPriceModel(pricemodel);
        return product;
    }
}
