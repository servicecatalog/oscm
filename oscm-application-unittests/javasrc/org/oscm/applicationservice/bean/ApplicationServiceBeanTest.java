/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.applicationservice.bean;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.oscm.types.enumtypes.OperationParameterType.INPUT_STRING;
import static org.oscm.types.enumtypes.OperationParameterType.REQUEST_SELECT;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.persistence.TypedQuery;
import javax.xml.ws.WebServiceException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.oscm.applicationservice.filter.AttributeFilter;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.domobjects.ConfigurationSetting;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.Parameter;
import org.oscm.domobjects.ParameterDefinition;
import org.oscm.domobjects.ParameterSet;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.TechnicalProductOperation;
import org.oscm.domobjects.Uda;
import org.oscm.domobjects.UdaDefinition;
import org.oscm.domobjects.UsageLicense;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.enumtypes.ParameterModificationType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.UdaConfigurationType;
import org.oscm.internal.types.exception.TechnicalServiceNotAliveException;
import org.oscm.internal.types.exception.TechnicalServiceOperationException;
import org.oscm.internal.types.exception.UnsupportedOperationException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.types.exception.ValidationException.ReasonEnum;
import org.oscm.logging.LoggerFactory;
import org.oscm.operation.data.OperationParameter;
import org.oscm.operation.data.OperationResult;
import org.oscm.operation.intf.OperationService;
import org.oscm.provisioning.data.InstanceInfo;
import org.oscm.provisioning.data.InstanceRequest;
import org.oscm.provisioning.data.InstanceResult;
import org.oscm.provisioning.data.User;
import org.oscm.test.BaseAdmUmTest;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.test.stubs.DataServiceStub;
import org.oscm.test.stubs.LocalizerServiceStub;
import org.oscm.types.enumtypes.OperationParameterType;
import org.oscm.types.enumtypes.UdaTargetType;

/**
 * Test class
 * 
 * @author pock
 */
public class ApplicationServiceBeanTest {

    static final String LOGIN_PATH = "/login.jsp";
    static final String BASE_URL = "http://localhost:8080/example-service";

    private static final String USER_ID = "userId";
    private static final String LOCALE_EN = "en";
    private static final String TOO_LONG_URL = "http://localhost:8080/oscm-portal/"
            + "organizationorganizationorganizationorganizationorganizationorganizationorganizationorganizationorganization"
            + "organizationorganizationorganizationorganizationorganizationorganizationorganizationorganizationorganization"
            + "/payment.jsf";

    private ApplicationServiceBean am;
    private ProvisioningServicePortStub servicePort;
    private OperationService operationPort;

    private Subscription subscription;
    private OperationResult operationResult;
    private boolean ParamDefOneTime = false;

    @Captor
    ArgumentCaptor<List<OperationParameter>> passedParams;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        servicePort = new ProvisioningServicePortStub();
        operationPort = mock(OperationService.class);
        operationResult = new OperationResult();

        am = spy(new ApplicationServiceBean());

        doReturn(operationPort).when(am)
                .getServiceClient(any(TechnicalProductOperation.class));
        doReturn(servicePort).when(am).getPort(any(TechnicalProduct.class));
        doReturn(false).when(am).isEventProvisioning(any(TechnicalProduct.class));

        am.cs = new ConfigurationServiceStub() {
            @Override
            public ConfigurationSetting getConfigurationSetting(
                    ConfigurationKey informationId, String contextId) {
                ConfigurationSetting c = new ConfigurationSetting();
                c.setValue("");
                return c;
            }
        };

        am.localizer = new LocalizerServiceStub() {
            @Override
            public String getLocalizedTextFromDatabase(String localeString,
                    long objectKey, LocalizedObjectTypes objectType) {
                return "";
            }
        };

        am.ds = new DataServiceStub() {
            @Override
            public PlatformUser getCurrentUser() {
                PlatformUser user = new PlatformUser();
                user.setEmail("user@fujitsu.com");
                user.setUserId("fujitsu");
                user.setFirstName("firstname");
                user.setLastName("lastName");
                return user;
            }

            @Override
            public PlatformUser getCurrentUserIfPresent() {
                return getCurrentUser();
            }

            @SuppressWarnings("unchecked")
            @Override
            public <T> TypedQuery<T> createNamedQuery(String jpql,
                    Class<T> resultClass) {

                TypedQuery<T> query = Mockito.mock(TypedQuery.class);
                Mockito.when(query.getResultList())
                        .thenReturn(Collections.<T> emptyList());

                return query;
            }

        };

        LoggerFactory.activateRollingFileAppender("./logs", null, "DEBUG");
    }

    @Test
    public void asyncCreateInstance() throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        Subscription sub = createSubscription(true);

        am.asyncCreateInstance(sub);
        List<Parameter> params = sub.getParameterSet().getParameters();

        InstanceRequest request = servicePort.getInstanceRequest();
        assertEquals(sub.getOrganization().getLocale(),
                request.getDefaultLocale());
        assertEquals(
                ApplicationServiceBean.SERVICE_PATH
                        + Long.toHexString(sub.getKey()),
                request.getLoginUrl());
        assertEquals(sub.getOrganization().getOrganizationId(),
                request.getOrganizationId());
        assertEquals(sub.getOrganization().getName(),
                request.getOrganizationName());
        assertEquals(params.get(0).getParameterDefinition().getParameterId(),
                request.getParameterValue().get(0).getParameterId());
        assertEquals(params.get(0).getValue(),
                request.getParameterValue().get(0).getValue());
        assertEquals(sub.getSubscriptionId(), request.getSubscriptionId());
        validateParameters(sub, null);
        assertNotNull(servicePort.getRequestingUser());
        assertTrue(servicePort.getRequestingUser().getEmail().length() > 0);
        assertTrue(servicePort.getRequestingUser().getUserId().length() > 0);
        assertTrue(servicePort.getRequestingUser().getUserFirstName()
                .length() > 0);
        assertTrue(
                servicePort.getRequestingUser().getUserLastName().length() > 0);
    }

    @Test
    public void asyncCreateInstance_Error()
            throws TechnicalServiceNotAliveException {
        Subscription sub = createSubscription(true);

        try {
            servicePort.setReturnCode(ProvisioningServicePortStub.RC_ERROR);
            am.asyncCreateInstance(sub);
            fail();
        } catch (TechnicalServiceOperationException e) {
            assertArrayEquals(new String[] { sub.getSubscriptionId(), "Error" },
                    e.getMessageParams());
        }
    }

    @Test(expected = TechnicalServiceNotAliveException.class)
    public void asyncCreateInstance_throwError() throws Exception {
        servicePort.setThrowError(true);
        Subscription sub = createSubscription(true);
        am.asyncCreateInstance(sub);
    }

    @Test(expected = TechnicalServiceOperationException.class)
    public void asyncCreateInstance_Exception()
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        Subscription sub = createSubscription(true);
        servicePort.setReturnCode(ProvisioningServicePortStub.RC_EXCEPTION);
        am.asyncCreateInstance(sub);
    }

    @Test(expected = TechnicalServiceOperationException.class)
    public void asyncCreateInstance_Null()
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        Subscription sub = createSubscription(true);
        servicePort.setReturnCode(ProvisioningServicePortStub.RC_NULL);
        am.asyncCreateInstance(sub);
    }

    @Test
    public void createInstance() throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        Subscription sub = createSubscription(true);

        InstanceResult result = am.createInstance(sub);
        InstanceInfo info = result.getInstance();

        assertEquals(sub.getOrganization().getLocale(), info.getAccessInfo());
        assertEquals(BASE_URL, info.getBaseUrl());
        assertEquals(sub.getOrganization().getOrganizationId()
                + sub.getSubscriptionId(), info.getInstanceId());
        assertEquals(ApplicationServiceBean.SERVICE_PATH
                + Long.toHexString(sub.getKey()), info.getLoginPath());
        validateParameters(sub, null);
        assertNotNull(servicePort.getRequestingUser());
        assertTrue(servicePort.getRequestingUser().getEmail().length() > 0);
        assertTrue(servicePort.getRequestingUser().getUserId().length() > 0);
        assertTrue(servicePort.getRequestingUser().getUserFirstName()
                .length() > 0);
        assertTrue(
                servicePort.getRequestingUser().getUserLastName().length() > 0);
    }

    @Test
    public void createInstance_NoParamSet()
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        Subscription sub = createSubscription(false);

        am.createInstance(sub);

        List<Parameter> parameters = servicePort.getParameters();
        assertNotNull(parameters);
        assertEquals(1, parameters.size());
        assertEquals("NonConfigurable",
                parameters.get(0).getParameterDefinition().getParameterId());
        assertNotNull(servicePort.getRequestingUser());
        assertTrue(servicePort.getRequestingUser().getEmail().length() > 0);
        assertTrue(servicePort.getRequestingUser().getUserId().length() > 0);
        assertTrue(servicePort.getRequestingUser().getUserFirstName()
                .length() > 0);
        assertTrue(
                servicePort.getRequestingUser().getUserLastName().length() > 0);
    }

    @Test(expected = TechnicalServiceOperationException.class)
    public void createInstance_NoInstanceReturned()
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        Subscription sub = createSubscription(true);
        sub.getOrganization().setOrganizationId(null);
        am.createInstance(sub);
    }

    @Test(expected = TechnicalServiceOperationException.class)
    public void createInstance_Exception()
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        Subscription sub = createSubscription(true);
        servicePort.setReturnCode(ProvisioningServicePortStub.RC_EXCEPTION);
        am.createInstance(sub);
    }

    @Test(expected = TechnicalServiceNotAliveException.class)
    public void createInstance_throwError() throws Exception {
        Subscription sub = createSubscription(true);
        servicePort.setThrowError(true);
        am.createInstance(sub);
    }

    @Test(expected = ValidationException.class)
    public void createInstance_ReturnedInstanceIdNull() throws Throwable {
        servicePort.setReturnedInstanceInfo(
                createInstanceInfo(null, null, null, null));
        Subscription sub = createSubscription(true);
        try {
            am.createInstance(sub);
        } catch (TechnicalServiceOperationException e) {
            throw e.getCause();
        }
    }

    @Test(expected = ValidationException.class)
    public void createInstance_ReturnedInstanceIdToLong() throws Throwable {
        servicePort.setReturnedInstanceInfo(createInstanceInfo(null, null,
                BaseAdmUmTest.TOO_LONG_DESCRIPTION, null));
        Subscription sub = createSubscription(true);
        try {
            am.createInstance(sub);
        } catch (TechnicalServiceOperationException e) {
            throw e.getCause();
        }
    }

    @Test(expected = ValidationException.class)
    public void createInstance_ReturnedBaseUrlInvalid() throws Throwable {
        servicePort.setReturnedInstanceInfo(
                createInstanceInfo(null, "some invalid url", "id", LOGIN_PATH));
        Subscription sub = createSubscription(true);
        try {
            am.createInstance(sub);
        } catch (TechnicalServiceOperationException e) {
            throw e.getCause();
        }
    }

    @Test(expected = ValidationException.class)
    public void createInstance_ReturnedBaseUrlToLong() throws Throwable {
        servicePort.setReturnedInstanceInfo(
                createInstanceInfo(null, TOO_LONG_URL, "id", LOGIN_PATH));
        Subscription sub = createSubscription(true);
        try {
            am.createInstance(sub);
        } catch (TechnicalServiceOperationException e) {
            throw e.getCause();
        }
    }

    @Test(expected = ValidationException.class)
    public void createInstance_ReturnedAccessInfoToLong() throws Throwable {
        servicePort.setReturnedInstanceInfo(createInstanceInfo(
                BaseAdmUmTest.TOO_LONG_DESCRIPTION
                        + BaseAdmUmTest.TOO_LONG_DESCRIPTION
                        + BaseAdmUmTest.TOO_LONG_DESCRIPTION
                        + BaseAdmUmTest.TOO_LONG_DESCRIPTION
                        + BaseAdmUmTest.TOO_LONG_DESCRIPTION
                        + BaseAdmUmTest.TOO_LONG_DESCRIPTION
                        + BaseAdmUmTest.TOO_LONG_DESCRIPTION
                        + BaseAdmUmTest.TOO_LONG_DESCRIPTION
                        + BaseAdmUmTest.TOO_LONG_DESCRIPTION
                        + BaseAdmUmTest.TOO_LONG_DESCRIPTION
                        + BaseAdmUmTest.TOO_LONG_DESCRIPTION
                        + BaseAdmUmTest.TOO_LONG_DESCRIPTION
                        + BaseAdmUmTest.TOO_LONG_DESCRIPTION
                        + BaseAdmUmTest.TOO_LONG_DESCRIPTION
                        + BaseAdmUmTest.TOO_LONG_DESCRIPTION
                        + BaseAdmUmTest.TOO_LONG_DESCRIPTION + "1",
                null, "id", null));
        Subscription sub = createSubscription(true);
        try {
            am.createInstance(sub);
        } catch (TechnicalServiceOperationException e) {
            throw e.getCause();
        }
    }

    @Test(expected = ValidationException.class)
    public void createInstance_ReturnedLoginPathToLong() throws Throwable {
        servicePort.setReturnedInstanceInfo(createInstanceInfo(null, BASE_URL,
                "id", BaseAdmUmTest.TOO_LONG_DESCRIPTION));
        Subscription sub = createSubscription(true);
        try {
            am.createInstance(sub);
        } catch (TechnicalServiceOperationException e) {
            throw e.getCause();
        }
    }

    @Test(expected = ValidationException.class)
    public void createInstance_ReturnedLoginPathWithoutBaseUrl()
            throws Throwable {
        servicePort.setReturnedInstanceInfo(
                createInstanceInfo(null, null, "id", LOGIN_PATH));
        Subscription sub = createSubscription(true, ServiceAccessType.USER);
        try {
            am.createInstance(sub);
        } catch (TechnicalServiceOperationException e) {
            throw e.getCause();
        }
    }

    @Test(expected = ValidationException.class)
    public void createInstance_ReturnedBaseUrlWithoutLoginPath()
            throws Throwable {
        servicePort.setReturnedInstanceInfo(
                createInstanceInfo(null, BASE_URL, "id", null));
        Subscription sub = createSubscription(true, ServiceAccessType.USER);
        try {
            am.createInstance(sub);
        } catch (TechnicalServiceOperationException e) {
            throw e.getCause();
        }
    }

    @Test
    public void deleteInstance() throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        Subscription sub = createSubscription(true);
        am.deleteInstance(sub);
        assertEquals(sub.getProductInstanceId(), servicePort.getInstanceId());
        assertEquals(sub.getOrganization().getOrganizationId(),
                servicePort.getOrganizationId());
        assertEquals(sub.getSubscriptionId(), servicePort.getSubscriptionId());
        assertNotNull(servicePort.getRequestingUser());
        assertTrue(servicePort.getRequestingUser().getEmail().length() > 0);
        assertTrue(servicePort.getRequestingUser().getUserId().length() > 0);
        assertTrue(servicePort.getRequestingUser().getUserFirstName()
                .length() > 0);
        assertTrue(
                servicePort.getRequestingUser().getUserLastName().length() > 0);
    }

    @Test(expected = TechnicalServiceOperationException.class)
    public void deleteInstance_Exception()
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        Subscription sub = createSubscription(true);
        servicePort.setReturnCode(ProvisioningServicePortStub.RC_EXCEPTION);
        am.deleteInstance(sub);
    }

    @Test(expected = TechnicalServiceNotAliveException.class)
    public void deleteInstance_throwError() throws Exception {
        Subscription sub = createSubscription(true);
        servicePort.setThrowError(true);
        am.deleteInstance(sub);
    }

    @Test
    public void modifySubscription() throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        Subscription sub = createSubscription(true);
        am.modifySubscription(sub);
        validateParameters(sub, sub.getProductInstanceId());
        assertNotNull(servicePort.getRequestingUser());
        assertTrue(servicePort.getRequestingUser().getEmail().length() > 0);
        assertTrue(servicePort.getRequestingUser().getUserId().length() > 0);
        assertTrue(servicePort.getRequestingUser().getUserFirstName()
                .length() > 0);
        assertTrue(
                servicePort.getRequestingUser().getUserLastName().length() > 0);
    }

    @Test
    public void modifySubscription_filterOnetimeParameter()
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        ParamDefOneTime = true;
        Subscription sub = createSubscription(true);
        am.modifySubscription(sub);
        validateParameters(sub, sub.getProductInstanceId());
        assertNotNull(servicePort.getRequestingUser());
        assertTrue(servicePort.getRequestingUser().getEmail().length() > 0);
        assertTrue(servicePort.getRequestingUser().getUserId().length() > 0);
        assertTrue(servicePort.getRequestingUser().getUserFirstName()
                .length() > 0);
        assertTrue(
                servicePort.getRequestingUser().getUserLastName().length() > 0);
    }

    @Test
    public void modifySubscription_WithoutParameter()
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        Subscription sub = createSubscription(true);
        sub.getProduct().setParameterSet(null);
        am.modifySubscription(sub);

        // only the non-configurable parameter definition must be returned.
        assertEquals(1, servicePort.getParameters().size());
        assertNotNull(servicePort.getRequestingUser());
        assertTrue(servicePort.getRequestingUser().getEmail().length() > 0);
        assertTrue(servicePort.getRequestingUser().getUserId().length() > 0);
        assertTrue(servicePort.getRequestingUser().getUserFirstName()
                .length() > 0);
        assertTrue(
                servicePort.getRequestingUser().getUserLastName().length() > 0);
    }

    @Test(expected = TechnicalServiceOperationException.class)
    public void modifySubscription_Exception()
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        Subscription sub = createSubscription(true);
        servicePort.setReturnCode(ProvisioningServicePortStub.RC_EXCEPTION);
        am.modifySubscription(sub);
    }

    @Test(expected = TechnicalServiceNotAliveException.class)
    public void modifySubscription_throwError() throws Exception {
        Subscription sub = createSubscription(true);
        servicePort.setThrowError(true);
        am.modifySubscription(sub);
    }

    @Test
    public void asyncModifySubscription()
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        // given
        Subscription sub = createSubscription(true);
        // when
        am.asyncModifySubscription(sub, sub.getProduct());
        // then
        validateParameters(sub, sub.getProductInstanceId());
        assertNotNull(servicePort.getRequestingUser());
        assertTrue(servicePort.getRequestingUser().getEmail().length() > 0);
        assertTrue(servicePort.getRequestingUser().getUserId().length() > 0);
        assertTrue(servicePort.getRequestingUser().getUserFirstName()
                .length() > 0);
        assertTrue(
                servicePort.getRequestingUser().getUserLastName().length() > 0);
    }

    @Test
    public void asyncModifySubscription_filterOnetimeParameter()
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        // given
        ParamDefOneTime = true;
        Subscription sub = createSubscription(true);
        // when
        am.asyncModifySubscription(sub, sub.getProduct());
        // then
        validateParameters(sub, sub.getProductInstanceId());
        assertNotNull(servicePort.getRequestingUser());
        assertTrue(servicePort.getRequestingUser().getEmail().length() > 0);
        assertTrue(servicePort.getRequestingUser().getUserId().length() > 0);
        assertTrue(servicePort.getRequestingUser().getUserFirstName()
                .length() > 0);
        assertTrue(
                servicePort.getRequestingUser().getUserLastName().length() > 0);
    }

    @Test
    public void asyncModifySubscription_WithoutParameter()
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        // given
        Subscription sub = createSubscription(true);
        sub.getProduct().setParameterSet(null);
        // when
        am.asyncModifySubscription(sub, sub.getProduct());
        // then
        assertEquals(1, servicePort.getParameters().size());
        assertNotNull(servicePort.getRequestingUser());
        assertTrue(servicePort.getRequestingUser().getEmail().length() > 0);
        assertTrue(servicePort.getRequestingUser().getUserId().length() > 0);
        assertTrue(servicePort.getRequestingUser().getUserFirstName()
                .length() > 0);
        assertTrue(
                servicePort.getRequestingUser().getUserLastName().length() > 0);
    }

    @Test(expected = TechnicalServiceOperationException.class)
    public void asyncModifySubscription_Exception()
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        // given
        Subscription sub = createSubscription(true);
        servicePort.setReturnCode(ProvisioningServicePortStub.RC_EXCEPTION);
        // when
        am.asyncModifySubscription(sub, sub.getProduct());
    }

    @Test(expected = TechnicalServiceNotAliveException.class)
    public void asyncModifySubscription_throwError() throws Exception {
        // given
        Subscription sub = createSubscription(true);
        servicePort.setThrowError(true);
        // when
        am.asyncModifySubscription(sub, sub.getProduct());
    }

    @Test
    public void asyncUpgradeSubscription()
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        // given
        Subscription sub = createSubscription(true);
        Product product = sub.getProduct();
        // when
        am.asyncUpgradeSubscription(sub, product);
        // then
        validateParameters(sub, sub.getProductInstanceId());
        assertNotNull(servicePort.getRequestingUser());
        assertTrue(servicePort.getRequestingUser().getEmail().length() > 0);
        assertTrue(servicePort.getRequestingUser().getUserId().length() > 0);
        assertTrue(servicePort.getRequestingUser().getUserFirstName()
                .length() > 0);
        assertTrue(
                servicePort.getRequestingUser().getUserLastName().length() > 0);
    }

    @Test
    public void asyncUpgradeSubscription_filterOnetimeParameter()
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        // given
        Subscription sub = createSubscription(true);
        Product product = sub.getProduct();
        // when
        am.asyncUpgradeSubscription(sub, product);
        // then
        validateParameters(sub, sub.getProductInstanceId());
        assertNotNull(servicePort.getRequestingUser());
        assertTrue(servicePort.getRequestingUser().getEmail().length() > 0);
        assertTrue(servicePort.getRequestingUser().getUserId().length() > 0);
        assertTrue(servicePort.getRequestingUser().getUserFirstName()
                .length() > 0);
        assertTrue(
                servicePort.getRequestingUser().getUserLastName().length() > 0);
    }

    @Test
    public void asyncUpgradeSubscription_WithoutParameter()
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        // given
        Subscription sub = createSubscription(true);
        sub.getProduct().setParameterSet(null);
        Product product = sub.getProduct();
        // when
        am.asyncUpgradeSubscription(sub, product);
        // then
        assertEquals(1, servicePort.getParameters().size());
        assertNotNull(servicePort.getRequestingUser());
        assertTrue(servicePort.getRequestingUser().getEmail().length() > 0);
        assertTrue(servicePort.getRequestingUser().getUserId().length() > 0);
        assertTrue(servicePort.getRequestingUser().getUserFirstName()
                .length() > 0);
        assertTrue(
                servicePort.getRequestingUser().getUserLastName().length() > 0);
    }

    @Test(expected = TechnicalServiceOperationException.class)
    public void asyncUpgradeSubscription_Exception()
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        // given
        Subscription sub = createSubscription(true);
        Product product = sub.getProduct();
        servicePort.setReturnCode(ProvisioningServicePortStub.RC_EXCEPTION);
        // when
        am.asyncUpgradeSubscription(sub, product);
    }

    @Test(expected = TechnicalServiceNotAliveException.class)
    public void asyncUpgradeSubscription_throwError() throws Exception {
        // given
        Subscription sub = createSubscription(true);
        Product product = sub.getProduct();
        servicePort.setThrowError(true);
        // when
        am.asyncUpgradeSubscription(sub, product);
    }

    @Test
    public void upgradeSubscription() throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        // given
        Subscription sub = createSubscription(true);
        // when
        am.upgradeSubscription(sub);
        // then
        validateParameters(sub, sub.getProductInstanceId());
        assertNotNull(servicePort.getRequestingUser());
        assertTrue(servicePort.getRequestingUser().getEmail().length() > 0);
        assertTrue(servicePort.getRequestingUser().getUserId().length() > 0);
        assertTrue(servicePort.getRequestingUser().getUserFirstName()
                .length() > 0);
        assertTrue(
                servicePort.getRequestingUser().getUserLastName().length() > 0);
    }

    @Test
    public void upgradeSubscription_filterOnetimeParameter()
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        // given
        Subscription sub = createSubscription(true);
        // when
        am.upgradeSubscription(sub);
        // then
        validateParameters(sub, sub.getProductInstanceId());
        assertNotNull(servicePort.getRequestingUser());
        assertTrue(servicePort.getRequestingUser().getEmail().length() > 0);
        assertTrue(servicePort.getRequestingUser().getUserId().length() > 0);
        assertTrue(servicePort.getRequestingUser().getUserFirstName()
                .length() > 0);
        assertTrue(
                servicePort.getRequestingUser().getUserLastName().length() > 0);
    }

    @Test
    public void upgradeSubscription_WithoutParameter()
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        // given
        Subscription sub = createSubscription(true);
        sub.getProduct().setParameterSet(null);
        // when
        am.upgradeSubscription(sub);
        // then
        assertEquals(1, servicePort.getParameters().size());
        assertNotNull(servicePort.getRequestingUser());
        assertTrue(servicePort.getRequestingUser().getEmail().length() > 0);
        assertTrue(servicePort.getRequestingUser().getUserId().length() > 0);
        assertTrue(servicePort.getRequestingUser().getUserFirstName()
                .length() > 0);
        assertTrue(
                servicePort.getRequestingUser().getUserLastName().length() > 0);
    }

    @Test(expected = TechnicalServiceOperationException.class)
    public void upgradeSubscription_Exception()
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        // given
        Subscription sub = createSubscription(true);
        servicePort.setReturnCode(ProvisioningServicePortStub.RC_EXCEPTION);
        // when
        am.upgradeSubscription(sub);
    }

    @Test(expected = TechnicalServiceNotAliveException.class)
    public void upgradeSubscription_throwError() throws Exception {
        // given
        Subscription sub = createSubscription(true);
        servicePort.setThrowError(true);
        // when
        am.upgradeSubscription(sub);
    }

    @Test
    public void createUsers() throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        Subscription sub = createSubscription(true);
        addUsageLicense(sub, "test1");
        addUsageLicense(sub, "test2");
        PlatformUser user = sub.getUsageLicenses().get(0).getUser();

        User[] users = am.createUsersForSubscription(sub);
        assertEquals(2, users.length);
        assertEquals(user.getUserId(), users[0].getUserId());
        assertEquals(user.getUserId(), users[0].getApplicationUserId());
        assertEquals(user.getEmail(), users[0].getEmail());
        assertEquals(user.getLocale(), users[0].getLocale());
        assertEquals(user.getUserId(), users[0].getUserLastName());
        assertNotNull(servicePort.getRequestingUser());
        assertTrue(servicePort.getRequestingUser().getEmail().length() > 0);
        assertTrue(servicePort.getRequestingUser().getUserId().length() > 0);
        assertTrue(servicePort.getRequestingUser().getUserFirstName()
                .length() > 0);
        assertTrue(
                servicePort.getRequestingUser().getUserLastName().length() > 0);
    }

    @Test
    public void createUsers_WithoutUsers()
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        Subscription sub = createSubscription(true);

        am.createUsers(sub, null);
        assertNull(servicePort.getInstanceId());
    }

    @Test
    public void createUsers_ProductAccessDirect()
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        Subscription sub = createSubscription(true);
        sub.getProduct().getTechnicalProduct()
                .setAccessType(ServiceAccessType.DIRECT);

        am.createUsers(sub, null);
        assertNull(servicePort.getInstanceId());
    }

    @Test(expected = TechnicalServiceOperationException.class)
    public void createUsers_Exception()
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        Subscription sub = createSubscription(true);
        addUsageLicense(sub, "test1");

        servicePort.setReturnCode(ProvisioningServicePortStub.RC_EXCEPTION);
        am.createUsersForSubscription(sub);
    }

    @Test(expected = TechnicalServiceNotAliveException.class)
    public void createUsers_throwError()
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        Subscription sub = createSubscription(true);
        addUsageLicense(sub, "test1");
        servicePort.setThrowError(true);
        am.createUsersForSubscription(sub);
    }

    @Test(expected = ValidationException.class)
    public void createUsers_ApplicationUserIdToLong() throws Throwable {
        servicePort.setApplicationUserId(BaseAdmUmTest.TOO_LONG_DESCRIPTION);
        Subscription sub = createSubscription(true);
        addUsageLicense(sub, "test1");
        try {
            am.createUsersForSubscription(sub);
        } catch (TechnicalServiceOperationException e) {
            throw e.getCause();
        }
    }

    @Test(expected = ValidationException.class)
    public void createUsers_ApplicationUserIdEmpty() throws Throwable {
        servicePort.setApplicationUserId("       ");
        Subscription sub = createSubscription(true);
        addUsageLicense(sub, "test1");
        try {
            am.createUsersForSubscription(sub);
        } catch (TechnicalServiceOperationException e) {
            throw e.getCause();
        }
    }

    @Test
    public void deleteUsers() throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        Subscription sub = createSubscription(true);
        addUsageLicense(sub, "test1");
        addUsageLicense(sub, "test2");
        sub.getUsageLicenses().get(0).setApplicationUserId("appId");

        am.deleteUsers(sub, sub.getUsageLicenses());
        assertEquals(2, servicePort.getUsers().size());
        assertEquals(sub.getUsageLicenses().get(0).getApplicationUserId(),
                servicePort.getUsers().get(0).getUserId());
        assertNotNull(servicePort.getRequestingUser());
        assertTrue(servicePort.getRequestingUser().getEmail().length() > 0);
        assertTrue(servicePort.getRequestingUser().getUserId().length() > 0);
        assertTrue(servicePort.getRequestingUser().getUserFirstName()
                .length() > 0);
        assertTrue(
                servicePort.getRequestingUser().getUserLastName().length() > 0);
    }

    @Test
    public void deleteUsers_WithoutUSers()
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        Subscription sub = createSubscription(true);

        am.deleteUsers(sub, null);
        assertNull(servicePort.getInstanceId());
    }

    @Test
    public void deleteUsers_ProductAccessDirect()
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        Subscription sub = createSubscription(true);
        sub.getProduct().getTechnicalProduct()
                .setAccessType(ServiceAccessType.DIRECT);

        am.deleteUsers(sub, null);
        assertNull(servicePort.getInstanceId());
    }

    @Test(expected = TechnicalServiceOperationException.class)
    public void deleteUsers_Exception()
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        Subscription sub = createSubscription(true);
        addUsageLicense(sub, "test1");

        servicePort.setReturnCode(ProvisioningServicePortStub.RC_EXCEPTION);
        am.deleteUsers(sub, sub.getUsageLicenses());
    }

    @Test(expected = TechnicalServiceNotAliveException.class)
    public void deleteUsers_throwError() throws Exception {
        Subscription sub = createSubscription(true);
        addUsageLicense(sub, "test1");
        servicePort.setThrowError(true);
        am.deleteUsers(sub, sub.getUsageLicenses());
    }

    @Test
    public void updateUsers() throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        Subscription sub = createSubscription(true);
        addUsageLicense(sub, "test1");
        addUsageLicense(sub, "test2");
        sub.getUsageLicenses().get(0).setApplicationUserId("appId");

        am.updateUsers(sub, sub.getUsageLicenses());
        assertEquals(2, servicePort.getUsers().size());
        assertEquals(sub.getUsageLicenses().get(0).getApplicationUserId(),
                servicePort.getUsers().get(0).getUserId());
        assertNotNull(servicePort.getRequestingUser());
        assertTrue(servicePort.getRequestingUser().getEmail().length() > 0);
        assertTrue(servicePort.getRequestingUser().getUserId().length() > 0);
        assertTrue(servicePort.getRequestingUser().getUserFirstName()
                .length() > 0);
        assertTrue(
                servicePort.getRequestingUser().getUserLastName().length() > 0);
    }

    @Test
    public void updateUsers_WithoutUsers()
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        Subscription sub = createSubscription(true);

        am.updateUsers(sub, null);
        assertNull(servicePort.getInstanceId());
    }

    @Test
    public void updateUsers_ProductAccessDirect()
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        Subscription sub = createSubscription(true);
        sub.getProduct().getTechnicalProduct()
                .setAccessType(ServiceAccessType.DIRECT);

        am.updateUsers(sub, null);
        assertNull(servicePort.getInstanceId());
    }

    @Test(expected = TechnicalServiceOperationException.class)
    public void updateUsers_Exception()
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {

        Subscription sub = createSubscription(true);
        addUsageLicense(sub, "test1");

        servicePort.setReturnCode(ProvisioningServicePortStub.RC_EXCEPTION);
        am.updateUsers(sub, sub.getUsageLicenses());
    }

    @Test(expected = TechnicalServiceNotAliveException.class)
    public void updateUsers_throwError() throws Exception {
        Subscription sub = createSubscription(true);
        addUsageLicense(sub, "test1");
        servicePort.setThrowError(true);
        am.updateUsers(sub, sub.getUsageLicenses());
    }

    @Test
    public void validateCommunication()
            throws TechnicalServiceNotAliveException {
        TechnicalProduct techProduct = new TechnicalProduct();
        am.validateCommunication(techProduct);
    }

    @Test(expected = TechnicalServiceNotAliveException.class)
    public void validateCommunication_throwError() throws Exception {
        TechnicalProduct techProduct = new TechnicalProduct();
        techProduct.setAccessType(ServiceAccessType.USER);
        servicePort.setThrowError(true);
        am.validateCommunication(techProduct);
    }

    @Test(expected = TechnicalServiceNotAliveException.class)
    public void getPortFault() throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        Subscription sub = createSubscription(true);
        doThrow(new TechnicalServiceNotAliveException(
                TechnicalServiceNotAliveException.Reason.ENDPOINT)).when(am)
                        .getPort(any(TechnicalProduct.class));

        am.asyncCreateInstance(sub);
    }

    @Test
    public void executeServiceOperation_NullReturned() throws Exception {
        when(operationPort.executeServiceOperation(anyString(), anyString(),
                anyString(), anyString(), anyListOf(OperationParameter.class)))
                        .thenReturn(operationResult);

        String userId = "1";
        String operationId = "OP";
        am.executeServiceOperation(userId, createSubscription(false), null,
                createTechnicalProductOperation(operationId), null);
        verify(operationPort).executeServiceOperation(eq(userId), anyString(),
                anyString(), eq(operationId),
                anyListOf(OperationParameter.class));
    }

    @Test
    public void executeServiceOperation_EmptyReturned() throws Exception {
        when(operationPort.executeServiceOperation(anyString(), anyString(),
                anyString(), anyString(), passedParams.capture()))
                        .thenReturn(operationResult);
        String userId = "1";
        String operationId = "OP";
        am.executeServiceOperation(userId, createSubscription(false), null,
                createTechnicalProductOperation(operationId), null);
        verify(operationPort).executeServiceOperation(eq(userId), anyString(),
                anyString(), eq(operationId),
                anyListOf(OperationParameter.class));
        assertTrue(passedParams.getValue().isEmpty());
    }

    @Test
    public void executeServiceOperation_Parameters() throws Exception {
        when(operationPort.executeServiceOperation(anyString(), anyString(),
                anyString(), anyString(), passedParams.capture()))
                        .thenReturn(operationResult);
        String userId = "1";
        String operationId = "OP";
        String trasactionId = "transactionid";
        Map<String, String> params = new HashMap<>();
        params.put("param1", "value1");
        params.put("param2", null);
        Subscription sub = createSubscription(false);

        am.executeServiceOperation(userId, sub, trasactionId,
                createTechnicalProductOperation(operationId), params);

        verify(operationPort).executeServiceOperation(eq(userId),
                eq(sub.getProductInstanceId()), eq(trasactionId),
                eq(operationId), anyListOf(OperationParameter.class));
        List<OperationParameter> list = passedParams.getValue();
        for (OperationParameter op : list) {
            assertTrue(params.containsKey(op.getName()));
            assertEquals(params.get(op.getName()), op.getValue());
        }
    }

    @Test(expected = TechnicalServiceOperationException.class)
    public void executeServiceOperation_MessageReturned() throws Exception {
        String error = "some error message";
        operationResult.setErrorMessage(error);
        when(operationPort.executeServiceOperation(anyString(), anyString(),
                anyString(), anyString(), anyListOf(OperationParameter.class)))
                        .thenReturn(operationResult);
        Subscription sub = createSubscription(false);
        String userId = "1";
        String operationId = "OP";
        try {
            am.executeServiceOperation(userId, sub, null,
                    createTechnicalProductOperation(operationId), null);
        } catch (TechnicalServiceOperationException e) {
            assertTrue(e.getMessage().indexOf(error) > 0);
            assertEquals(2, e.getMessageParams().length);
            assertEquals(sub.getSubscriptionId(), e.getMessageParams()[0]);
            assertEquals(error, e.getMessageParams()[1]);
            throw e;
        }
    }

    @Test(expected = TechnicalServiceNotAliveException.class)
    public void executeServiceOperation_ErrorThrown() throws Exception {
        when(operationPort.executeServiceOperation(anyString(), anyString(),
                anyString(), anyString(), anyListOf(OperationParameter.class)))
                        .thenThrow(new Error("error"));
        Subscription sub = createSubscription(false);
        String userId = "1";
        String operationId = "OP";
        am.executeServiceOperation(userId, sub, null,
                createTechnicalProductOperation(operationId), null);
    }

    @Test
    public void activateInstance() throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        Subscription sub = createSubscription(true);
        am.activateInstance(sub);
        assertEquals(sub.getProductInstanceId(), servicePort.getInstanceId());
        assertNotNull(servicePort.getRequestingUser());
        assertTrue(servicePort.getRequestingUser().getEmail().length() > 0);
        assertTrue(servicePort.getRequestingUser().getUserId().length() > 0);
        assertTrue(servicePort.getRequestingUser().getUserFirstName()
                .length() > 0);
        assertTrue(
                servicePort.getRequestingUser().getUserLastName().length() > 0);
    }

    @Test(expected = TechnicalServiceOperationException.class)
    public void activateInstanceException()
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        Subscription sub = createSubscription(true);
        servicePort.setReturnCode(ProvisioningServicePortStub.RC_EXCEPTION);
        am.activateInstance(sub);
    }

    @Test(expected = TechnicalServiceNotAliveException.class)
    public void activateInstance_throwError() throws Exception {
        Subscription sub = createSubscription(true);
        servicePort.setThrowError(true);
        am.activateInstance(sub);
    }

    @Test
    public void deactivateInstance() throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        Subscription sub = createSubscription(true);
        am.deactivateInstance(sub);
        assertEquals(sub.getProductInstanceId(), servicePort.getInstanceId());
        assertNotNull(servicePort.getRequestingUser());
        assertTrue(servicePort.getRequestingUser().getEmail().length() > 0);
        assertTrue(servicePort.getRequestingUser().getUserId().length() > 0);
        assertTrue(servicePort.getRequestingUser().getUserFirstName()
                .length() > 0);
        assertTrue(
                servicePort.getRequestingUser().getUserLastName().length() > 0);
    }

    @Test(expected = TechnicalServiceOperationException.class)
    public void deactivateInstanceException()
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        Subscription sub = createSubscription(true);
        servicePort.setReturnCode(ProvisioningServicePortStub.RC_EXCEPTION);
        am.deactivateInstance(sub);
    }

    @Test(expected = TechnicalServiceNotAliveException.class)
    public void deactivateInstance_throwError() throws Exception {
        Subscription sub = createSubscription(true);
        servicePort.setThrowError(true);
        am.deactivateInstance(sub);
    }

    @Test
    public void validateInstanceInfo_LOGIN() throws Throwable {
        validateInstanceInfo(ServiceAccessType.LOGIN);
    }

    @Test
    public void validateInstanceInfo_USER() throws Throwable {
        validateInstanceInfo(ServiceAccessType.USER);
    }

    @Test
    public void validateInstanceInfo_DIRECT() throws Throwable {
        validateInstanceInfo(ServiceAccessType.DIRECT);
    }

    private void validateInstanceInfo(ServiceAccessType accessType)
            throws Throwable {
        ApplicationServiceBean bean = mockForInstanceInfoValidation(accessType);
        InstanceInfo info = createInstanceInfo();

        bean.validateInstanceInfo(info, subscription);
    }

    @Test
    public void validateInstanceInfo_USER_AccessInfo_NoneOnTP()
            throws Throwable {
        validateInstanceInfo_AccessInfo_NoneOnTP(ServiceAccessType.USER);
    }

    @Test
    public void validateInstanceInfo_DIRECT_AccessInfo_NoneOnTP()
            throws Throwable {
        validateInstanceInfo_AccessInfo_NoneOnTP(ServiceAccessType.DIRECT);
    }

    private void validateInstanceInfo_AccessInfo_NoneOnTP(
            ServiceAccessType accessType) throws Throwable {
        ApplicationServiceBean bean = mockForInstanceInfoValidation(accessType);
        // no access info available on technical service
        doReturn(null).when(bean.localizer).getLocalizedTextFromDatabase(
                anyString(), anyLong(),
                eq(LocalizedObjectTypes.TEC_PRODUCT_LOGIN_ACCESS_DESC));
        InstanceInfo info = createInstanceInfo();

        bean.validateInstanceInfo(info, subscription);
    }

    @Test
    public void validateInstanceInfo_USER_NoAccessInfo_NoneOnTP()
            throws Throwable {
        validateInstanceInfo_NoAccessInfo_NoneOnTP(ServiceAccessType.USER);
    }

    @Test
    public void validateInstanceInfo_DIRECT_NoAccessInfo_NoneOnTP()
            throws Throwable {
        validateInstanceInfo_NoAccessInfo_NoneOnTP(ServiceAccessType.DIRECT);
    }

    private void validateInstanceInfo_NoAccessInfo_NoneOnTP(
            ServiceAccessType accessType) throws Throwable {
        ApplicationServiceBean bean = mockForInstanceInfoValidation(accessType);
        // no access info available on technical service
        doReturn(null).when(bean.localizer).getLocalizedTextFromDatabase(
                anyString(), anyLong(),
                eq(LocalizedObjectTypes.TEC_PRODUCT_LOGIN_ACCESS_DESC));
        InstanceInfo info = createInstanceInfo();
        info.setAccessInfo(null);

        try {
            bean.validateInstanceInfo(info, subscription);
            fail();
        } catch (TechnicalServiceOperationException e) {
            ValidationException ve = (ValidationException) e.getCause();
            assertEquals("accessInfo", ve.getMember());
            assertEquals(ReasonEnum.REQUIRED, ve.getReason());
        }
    }

    @Test
    public void validateInstanceInfo_USER_NoAccessInfo_ExistingOnTP()
            throws Throwable {
        validateInstanceInfo_NoAccessInfo_ExistingOnTP(ServiceAccessType.USER);
    }

    @Test
    public void validateInstanceInfo_DIRECT_NoAccessInfo_ExistingOnTP()
            throws Throwable {
        validateInstanceInfo_NoAccessInfo_ExistingOnTP(
                ServiceAccessType.DIRECT);
    }

    private void validateInstanceInfo_NoAccessInfo_ExistingOnTP(
            ServiceAccessType accessType) throws Throwable {
        ApplicationServiceBean bean = mockForInstanceInfoValidation(accessType);
        doReturn("Existing access info on technical product")
                .when(bean.localizer).getLocalizedTextFromDatabase(anyString(),
                        anyLong(),
                        eq(LocalizedObjectTypes.TEC_PRODUCT_LOGIN_ACCESS_DESC));
        InstanceInfo info = createInstanceInfo();
        info.setAccessInfo(null);

        bean.validateInstanceInfo(info, subscription);
    }

    @Test
    public void validateInstanceInfo_NoBaseUrl_LOGIN() throws Throwable {
        validateInstanceInfo_NoBaseUrl(ServiceAccessType.LOGIN);
    }

    private void validateInstanceInfo_NoBaseUrl(ServiceAccessType accessType)
            throws Throwable {
        ApplicationServiceBean bean = mockForInstanceInfoValidation(accessType);
        InstanceInfo info = createInstanceInfo();
        info.setBaseUrl(null);

        try {
            bean.validateInstanceInfo(info, subscription);
            fail();
        } catch (TechnicalServiceOperationException e) {
            ValidationException ve = (ValidationException) e.getCause();
            assertEquals("baseUrl", ve.getMember());
            assertEquals(ReasonEnum.URL, ve.getReason());
        }
    }

    @Test
    public void validateInstanceInfo_NoBaseUrl_USER() throws Throwable {
        ApplicationServiceBean bean = mockForInstanceInfoValidation(
                ServiceAccessType.USER);
        InstanceInfo info = createInstanceInfo();
        info.setBaseUrl(null);

        bean.validateInstanceInfo(info, subscription);
    }

    @Test
    public void validateInstanceInfo_NoBaseUrl_DIRECT() throws Throwable {
        ApplicationServiceBean bean = mockForInstanceInfoValidation(
                ServiceAccessType.DIRECT);
        InstanceInfo info = createInstanceInfo();
        info.setBaseUrl(null);

        bean.validateInstanceInfo(info, subscription);
    }

    @Test
    public void validateInstanceInfo_NoLoginPath_LOGIN() throws Throwable {
        validateInstanceInfo_NoLoginPath(ServiceAccessType.LOGIN);
    }

    private void validateInstanceInfo_NoLoginPath(ServiceAccessType accessType)
            throws Throwable {
        ApplicationServiceBean bean = mockForInstanceInfoValidation(accessType);
        InstanceInfo info = createInstanceInfo();
        info.setLoginPath(null);

        try {
            bean.validateInstanceInfo(info, subscription);
            fail();
        } catch (TechnicalServiceOperationException e) {
            ValidationException ve = (ValidationException) e.getCause();
            assertEquals("loginPath", ve.getMember());
            assertEquals(ReasonEnum.REQUIRED, ve.getReason());
        }
    }

    @Test
    public void validateInstanceInfo_NoLoginPath_USER() throws Throwable {
        ApplicationServiceBean bean = mockForInstanceInfoValidation(
                ServiceAccessType.USER);
        InstanceInfo info = createInstanceInfo();
        info.setLoginPath(null);

        bean.validateInstanceInfo(info, subscription);
    }

    @Test
    public void validateInstanceInfo_NoLoginPath_DIRECT() throws Throwable {
        ApplicationServiceBean bean = mockForInstanceInfoValidation(
                ServiceAccessType.DIRECT);
        InstanceInfo info = createInstanceInfo();
        info.setLoginPath(null);

        bean.validateInstanceInfo(info, subscription);
    }

    @Test
    public void toInstanceRequest_DIRECT() throws Throwable {
        InstanceRequest request = toInstanceRequest(ServiceAccessType.DIRECT);
        assertNull(request.getLoginUrl());
    }

    @Test
    public void toInstanceRequest_USER() throws Throwable {
        InstanceRequest request = toInstanceRequest(ServiceAccessType.USER);
        assertNull(request.getLoginUrl());
    }

    @Test
    public void getOperationParameterValues_NoRequestRequired()
            throws Exception {
        TechnicalProductOperation tpo = createTechnicalProductOperation("op1",
                INPUT_STRING);
        Subscription sub = createSubscription(false);

        Map<String, List<String>> result = am
                .getOperationParameterValues(USER_ID, tpo, sub);

        assertTrue(result.isEmpty());
        verifyZeroInteractions(operationPort);
    }

    @Test
    public void getOperationParameterValues_NullReturned() throws Exception {
        TechnicalProductOperation tpo = createTechnicalProductOperation("op1",
                REQUEST_SELECT);
        Subscription sub = createSubscription(false);
        when(operationPort.getParameterValues(anyString(), anyString(),
                anyString())).thenReturn(null);

        Map<String, List<String>> result = am
                .getOperationParameterValues(USER_ID, tpo, sub);

        assertTrue(result.isEmpty());
        verify(operationPort).getParameterValues(same(USER_ID),
                eq(sub.getProductInstanceId()), eq(tpo.getOperationId()));
    }

    @Test(expected = TechnicalServiceNotAliveException.class)
    public void getOperationParameterValues_Timeout() throws Exception {
        when(operationPort.getParameterValues(anyString(), anyString(),
                anyString())).thenThrow(
                        new WebServiceException(new SocketTimeoutException()));
        TechnicalProductOperation tpo = createTechnicalProductOperation("op1",
                REQUEST_SELECT);
        Subscription sub = createSubscription(false);

        am.getOperationParameterValues(USER_ID, tpo, sub);
    }

    @Test(expected = TechnicalServiceNotAliveException.class)
    public void getOperationParameterValues_Throwable() throws Exception {
        when(operationPort.getParameterValues(anyString(), anyString(),
                anyString())).thenThrow(new RuntimeException());
        TechnicalProductOperation tpo = createTechnicalProductOperation("op1",
                REQUEST_SELECT);
        Subscription sub = createSubscription(false);

        am.getOperationParameterValues(USER_ID, tpo, sub);
    }

    @Test(expected = TechnicalServiceOperationException.class)
    public void getOperationParameterValues_WebServiceException()
            throws Exception {
        when(operationPort.getParameterValues(anyString(), anyString(),
                anyString())).thenThrow(new WebServiceException());
        TechnicalProductOperation tpo = createTechnicalProductOperation("op1",
                REQUEST_SELECT);
        Subscription sub = createSubscription(false);

        am.getOperationParameterValues(USER_ID, tpo, sub);
    }

    @Test(expected = TechnicalServiceOperationException.class)
    public void getOperationParameterValues_Unsupported() throws Exception {
        when(operationPort.getParameterValues(anyString(), anyString(),
                anyString())).thenThrow(new UnsupportedOperationException());
        TechnicalProductOperation tpo = createTechnicalProductOperation("op1",
                REQUEST_SELECT);
        Subscription sub = createSubscription(false);

        am.getOperationParameterValues(USER_ID, tpo, sub);
    }

    @Test
    public void getOperationParameterValues() throws Exception {
        TechnicalProductOperation tpo = createTechnicalProductOperation("op1",
                REQUEST_SELECT, REQUEST_SELECT);
        Subscription sub = createSubscription(false);
        when(operationPort.getParameterValues(anyString(), anyString(),
                anyString())).thenReturn(initOperationParameters(tpo, 3));

        Map<String, List<String>> result = am
                .getOperationParameterValues(USER_ID, tpo, sub);

        for (org.oscm.domobjects.OperationParameter op : tpo.getParameters()) {
            assertTrue(result.containsKey(op.getId()));
            List<String> list = result.get(op.getId());
            assertTrue(list.contains(op.getId() + "_value0"));
            assertTrue(list.contains(op.getId() + "_value1"));
            assertTrue(list.contains(op.getId() + "_value2"));
        }
    }

    @Test
    public void saveAttribute() throws Exception {
        Subscription sub = createSubscription(true);

        long vendorKey = 42L;

        Organization vendor = new Organization();
        vendor.setKey(vendorKey);

        ArrayList<UdaDefinition> list = new ArrayList<>();

        UdaDefinition udaDef = new UdaDefinition();
        udaDef.setTargetType(UdaTargetType.CUSTOMER);
        udaDef.setConfigurationType(UdaConfigurationType.SUPPLIER);
        udaDef.setDefaultValue("value1");
        list.add(udaDef);

        udaDef = new UdaDefinition();
        udaDef.setTargetType(UdaTargetType.CUSTOMER);
        udaDef.setConfigurationType(UdaConfigurationType.SUPPLIER);
        udaDef.setDefaultValue("value2");

        Uda uda = new Uda();
        uda.setTargetObjectKey(vendorKey);
        uda.setUdaValue("value3");
        udaDef.setUdas(Arrays.asList(uda));

        list.add(udaDef);

        vendor.setUdaDefinitions(list);

        sub.getProduct().setVendor(vendor);

        am.saveAttributes(sub);

        assertEquals(2, AttributeFilter.getCustomAttributeList(sub).size());

    }

    private List<OperationParameter> initOperationParameters(
            TechnicalProductOperation tpo, int values) {
        List<OperationParameter> result = new LinkedList<>();
        for (org.oscm.domobjects.OperationParameter op : tpo.getParameters()) {
            for (int i = 0; i < values; i++) {
                OperationParameter p = new OperationParameter();
                p.setName(op.getId());
                p.setValue(op.getId() + "_value" + i);
                result.add(p);
            }
        }
        return result;
    }

    private InstanceRequest toInstanceRequest(ServiceAccessType accessType)
            throws Throwable {
        ApplicationServiceBean bean = mockForInstanceInfoValidation(accessType);
        Subscription sub = createSubscription(true, accessType);

        bean.cs = mock(ConfigurationServiceLocal.class);
        ConfigurationSetting setting = new ConfigurationSetting();
        doReturn(setting).when(bean.cs).getConfigurationSetting(
                any(ConfigurationKey.class), anyString());

        return bean.toInstanceRequest(sub);
    }

    private ApplicationServiceBean mockForInstanceInfoValidation(
            ServiceAccessType accessType) {
        ApplicationServiceBean bean = new ApplicationServiceBean();
        bean.localizer = mock(LocalizerServiceLocal.class);

        TechnicalProduct technicalProduct = new TechnicalProduct();
        technicalProduct.setAccessType(accessType);
        Product product = new Product();
        product.setTechnicalProduct(technicalProduct);
        subscription = new Subscription();
        subscription.bindToProduct(product);

        return bean;
    }

    private InstanceInfo createInstanceInfo() {
        InstanceInfo info = new InstanceInfo();
        info.setInstanceId("abc");
        info.setBaseUrl("http://localhost:8080");
        info.setLoginPath("/login");
        info.setAccessInfo("Some access info");
        return info;
    }

    private void addUsageLicense(Subscription sub, String userId) {
        UsageLicense license = new UsageLicense();
        license.setSubscription(sub);

        PlatformUser user = new PlatformUser();
        user.setUserId(userId);
        user.setEmail(userId + "@est.fujitsu.com");
        user.setLocale(LOCALE_EN);
        user.setOrganization(sub.getOrganization());

        license.setUser(user);
        sub.getUsageLicenses().add(license);
    }

    private Subscription createSubscription(boolean createParameterSet) {
        return createSubscription(createParameterSet,
                ServiceAccessType.EXTERNAL);
    }

    private Subscription createSubscription(boolean createParameterSet,
            ServiceAccessType type) {
        String locale = LOCALE_EN;
        String organizationId = "organizationId";
        String name = "name";
        String parameterId = "parameterId";
        String parameterValue = "parameterValue";
        long subscriptionKey = 0x1000;
        String subscriptionId = "subscriptionId";
        String instanceId = "instanceId";

        Organization org = new Organization();
        org.setOrganizationId(organizationId);
        org.setLocale(locale);
        org.setName(name);

        TechnicalProduct techProd = new TechnicalProduct();
        techProd.setAccessType(type);
        techProd.setParameterDefinitions(new ArrayList<ParameterDefinition>());

        ParameterDefinition parmDef = new ParameterDefinition();
        parmDef.setParameterId(parameterId);
        parmDef.setModificationType(
                ParamDefOneTime ? ParameterModificationType.ONE_TIME
                        : ParameterModificationType.STANDARD);
        techProd.getParameterDefinitions().add(parmDef);
        Parameter param = new Parameter();

        param.setParameterDefinition(parmDef);
        param.setValue(parameterValue);

        ParameterDefinition parmDef2 = new ParameterDefinition();
        parmDef2.setParameterId("NonConfigurable");
        parmDef2.setConfigurable(false);
        parmDef2.setDefaultValue("123");
        parmDef2.setModificationType(ParameterModificationType.STANDARD);
        techProd.getParameterDefinitions().add(parmDef2);

        Product prod = new Product();
        prod.setTechnicalProduct(techProd);
        prod.setVendor(org);

        if (createParameterSet) {
            ParameterSet paramSet = new ParameterSet();
            paramSet.setParameters(new ArrayList<Parameter>());
            paramSet.getParameters().add(param);

            prod.setParameterSet(paramSet);
        }

        Subscription sub = new Subscription();
        sub.setKey(subscriptionKey);
        sub.setSubscriptionId(subscriptionId);
        sub.setOrganization(org);
        sub.bindToProduct(prod);
        sub.setProductInstanceId(instanceId);

        return sub;

    }

    /**
     * Validates the parameters as retrieved by the provisioning service.
     * 
     * @param sub
     *            The subscription that was modified.
     * @param productInstanceId
     *            The expected product instance id.
     */
    private void validateParameters(Subscription sub,
            String productInstanceId) {
        if (productInstanceId != null) {
            assertEquals(sub.getProductInstanceId(),
                    servicePort.getInstanceId());
        }
        ParameterSet paramSet = sub.getParameterSet();
        List<Parameter> parameters = servicePort.getParameters();
        assertNotNull(parameters);
        assertEquals(ParamDefOneTime ? 1 : 2, parameters.size());

        int paramIndex = 0;
        if (!ParamDefOneTime) {
            Parameter serviceParam1 = parameters.get(paramIndex);
            assertEquals(paramSet.getParameters().get(paramIndex).getValue(),
                    serviceParam1.getValue());
            assertEquals(
                    paramSet.getParameters().get(paramIndex)
                            .getParameterDefinition().getParameterId(),
                    serviceParam1.getParameterDefinition().getParameterId());
            paramIndex++;
        }

        Parameter serviceParam2 = parameters.get(paramIndex);
        assertEquals("NonConfigurable",
                serviceParam2.getParameterDefinition().getParameterId());
        assertEquals("123", serviceParam2.getValue());

    }

    private TechnicalProductOperation createTechnicalProductOperation(String id,
            OperationParameterType... types) {
        TechnicalProductOperation op = new TechnicalProductOperation();
        op.setOperationId(id);
        for (OperationParameterType type : types) {
            org.oscm.domobjects.OperationParameter p = new org.oscm.domobjects.OperationParameter();
            p.setType(type);
            op.setOperationId(Integer.toHexString(new Random().nextInt()));
            op.getParameters().add(p);
        }
        return op;
    }

    private static InstanceInfo createInstanceInfo(String access,
            String baseUrl, String instanceId, String loginPath) {
        InstanceInfo info = new InstanceInfo();
        info.setAccessInfo(access);
        info.setBaseUrl(baseUrl);
        info.setInstanceId(instanceId);
        info.setLoginPath(loginPath);
        return info;
    }
}
