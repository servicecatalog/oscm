/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017                                           
 *                                                                                                                                  
 *  Creation Date: 17.06.15 09:59
 *
 *******************************************************************************/

package org.oscm.triggerservice.bean;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.SessionContext;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.TriggerProcess;
import org.oscm.domobjects.TriggerProcessParameter;
import org.oscm.encrypter.AESEncrypter;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.subscriptionservice.local.SubscriptionServiceLocal;
import org.oscm.internal.intf.TriggerService;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.types.enumtypes.TriggerProcessParameterType;
import org.oscm.internal.types.enumtypes.TriggerProcessStatus;
import org.oscm.internal.types.enumtypes.TriggerType;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.types.exception.TriggerProcessStatusException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOLocalizedText;
import org.oscm.internal.vo.VOParameter;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOServiceDetails;
import org.oscm.internal.vo.VOTriggerProcessParameter;

@RunWith(MockitoJUnitRunner.class)
public class TriggerServiceBeanTest {

    @Mock
    private DataService dm;

    @Mock
    private LocalizerServiceLocal localizer;

    @Mock
    private SessionContext sessionCtx;

    @Mock
    private SubscriptionServiceLocal localService;

    @Spy
    private VOService voService = new VOServiceDetails();

    @InjectMocks
    private TriggerService triggerService = new TriggerServiceBean() {

        @Override
        TriggerProcessParameter getTriggerProcessParameter(long actionKey,
                TriggerProcessParameterType paramName) {
            TriggerProcessParameter parameter = new TriggerProcessParameter();
            parameter.setKey(VALID_KEY);
            parameter
                    .setName(org.oscm.types.enumtypes.TriggerProcessParameterName.PRODUCT);
            parameter.setValue(new VOServiceDetails());
            parameter.setTriggerProcess(new TriggerProcess());
            return parameter;
        }
    };

    private List<VOTriggerProcessParameter> parameters = new ArrayList<>();

    @Spy
    private TriggerProcessParameter parameter = new TriggerProcessParameter();

    @Mock
    private VOTriggerProcessParameter triggerProcessParameter = new VOTriggerProcessParameter();

    private final TriggerProcessParameterType TRIGGER_PROCESS_PARAM_TYPE = TriggerProcessParameterType.PRODUCT;

    private static final long VALID_KEY = 0L;
    private static final long INVALID_KEY = 1L;

    private static final PlatformUser USER = new PlatformUser();
    private static final Organization ORGANIZATION = new Organization();
    private static final Organization EMPTY_ORGANIZATION = new Organization();

    @Mock
    private VOLocalizedText localizedText;

    @Before
    public void setUp() {

        triggerService = spy(triggerService);

        USER.setOrganization(ORGANIZATION);
        when(dm.getCurrentUser()).thenReturn(USER);

        ORGANIZATION.setKey(VALID_KEY);
        EMPTY_ORGANIZATION.setKey(INVALID_KEY);

        when(triggerProcessParameter.getType()).thenReturn(
                TRIGGER_PROCESS_PARAM_TYPE);
        when(triggerProcessParameter.getValue()).thenReturn(voService);

        parameters.add(triggerProcessParameter);
        AESEncrypter.generateKey();
    }

    /**
     * Correct scenario: - TriggerProcessStatus: WAITING_FOR_APPROVAL -
     * TriggerType: SUBSCRIBE_TO_SERVICE
     */
    @Test
    public void testUpdateActionValid() throws SaaSApplicationException {
        // given
        TriggerProcess triggerProcess = TriggerServiceBeanTestHelper
                .getTriggerProcess();
        triggerProcess.getTriggerDefinition().setOrganization(ORGANIZATION);

        // when
        when(dm.getReference(TriggerProcess.class, VALID_KEY)).thenReturn(
                triggerProcess);
        triggerService.updateActionParameters(VALID_KEY, parameters);
    }

    /**
     * TriggerProcessStatus should be different than WAITING_FOR_APPROVAL and
     * TriggerProcessStatusException should be thrown
     */
    @Test(expected = TriggerProcessStatusException.class)
    public void testUpdateActionWrongStatus() throws SaaSApplicationException {
        // given
        TriggerProcessStatus triggerProcessStatus = TriggerServiceBeanTestHelper
                .randomEnum(TriggerProcessStatus.class,
                        TriggerServiceBeanTestHelper.VALID_TRIGGER_STATUS);
        TriggerProcess triggerProcess = TriggerServiceBeanTestHelper
                .getTriggerProcess(triggerProcessStatus,
                        TriggerServiceBeanTestHelper.VALID_TRIGGER_TYPE);
        triggerProcess.getTriggerDefinition().setOrganization(ORGANIZATION);

        // when
        when(dm.getReference(TriggerProcess.class, VALID_KEY)).thenReturn(
                triggerProcess);
        triggerService.updateActionParameters(VALID_KEY, parameters);
    }

    /**
     * TriggerType should be different than SUBSCRIBE_TO_SERVICE and
     * OperationNotPermittedException should be thrown
     */
    @Test(expected = OperationNotPermittedException.class)
    public void testUpdateActionWrongType() throws SaaSApplicationException {
        // given
        TriggerType triggerType = TriggerServiceBeanTestHelper.randomEnum(
                TriggerType.class,
                TriggerServiceBeanTestHelper.VALID_TRIGGER_TYPE);
        TriggerProcess triggerProcess = TriggerServiceBeanTestHelper
                .getTriggerProcess(
                        TriggerServiceBeanTestHelper.VALID_TRIGGER_STATUS,
                        triggerType);
        triggerProcess.getTriggerDefinition().setOrganization(ORGANIZATION);

        // when
        when(dm.getReference(TriggerProcess.class, VALID_KEY)).thenReturn(
                triggerProcess);
        triggerService.updateActionParameters(VALID_KEY, parameters);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testUpdateActionNotPermitted() throws SaaSApplicationException {
        // given
        TriggerProcess triggerProcess = TriggerServiceBeanTestHelper
                .getTriggerProcess();
        triggerProcess.getTriggerDefinition().setOrganization(
                EMPTY_ORGANIZATION);

        // when
        when(dm.getReference(TriggerProcess.class, VALID_KEY)).thenReturn(
                triggerProcess);
        triggerService.updateActionParameters(VALID_KEY, parameters);
    }

    @Test
    public void testUpdateActionStringParameter()
            throws SaaSApplicationException {
        // given
        final String PARAM_VALUE = "TEST";
        List<VOParameter> parameterList = TriggerServiceBeanTestHelper
                .getVOParameters(ParameterValueType.STRING, PARAM_VALUE);
        parameterList.get(0).setConfigurable(true);
        TriggerProcess triggerProcess = TriggerServiceBeanTestHelper
                .getTriggerProcess();
        triggerProcess.getTriggerDefinition().setOrganization(ORGANIZATION);

        // when
        when(dm.getReference(TriggerProcess.class, VALID_KEY)).thenReturn(
                triggerProcess);
        when(voService.getParameters()).thenReturn(parameterList);
        triggerService.updateActionParameters(VALID_KEY, parameters);
    }

    @Test(expected = ValidationException.class)
    public void testUpdateActionParameterNullAndMandatory()
            throws SaaSApplicationException {
        // given
        final String PARAM_VALUE = null;
        List<VOParameter> parameterList = TriggerServiceBeanTestHelper
                .getVOParameters(ParameterValueType.STRING, PARAM_VALUE);
        parameterList.get(0).getParameterDefinition().setMandatory(true);
        parameterList.get(0).setConfigurable(true);
        TriggerProcess triggerProcess = TriggerServiceBeanTestHelper
                .getTriggerProcess();
        triggerProcess.getTriggerDefinition().setOrganization(ORGANIZATION);

        // when
        when(dm.getReference(TriggerProcess.class, VALID_KEY)).thenReturn(
                triggerProcess);
        when(voService.getParameters()).thenReturn(parameterList);
        triggerService.updateActionParameters(VALID_KEY, parameters);
    }

    @Test
    public void tesUpdateActionIntegerParameter()
            throws SaaSApplicationException {
        final String PARAM_VALUE = "123";
        List<VOParameter> parameterList = TriggerServiceBeanTestHelper
                .getVOParameters(ParameterValueType.INTEGER, PARAM_VALUE);
        parameterList.get(0).setConfigurable(true);
        TriggerProcess triggerProcess = TriggerServiceBeanTestHelper
                .getTriggerProcess();
        triggerProcess.getTriggerDefinition().setOrganization(ORGANIZATION);

        // when
        when(dm.getReference(TriggerProcess.class, VALID_KEY)).thenReturn(
                triggerProcess);
        when(voService.getParameters()).thenReturn(parameterList);
        triggerService.updateActionParameters(VALID_KEY, parameters);
    }

    @Test(expected = ValidationException.class)
    public void tesUpdateActionIntegerParameterNotInt()
            throws SaaSApplicationException {
        final String PARAM_VALUE = "TEST";
        List<VOParameter> parameterList = TriggerServiceBeanTestHelper
                .getVOParameters(ParameterValueType.INTEGER, PARAM_VALUE);
        parameterList.get(0).setConfigurable(true);
        TriggerProcess triggerProcess = TriggerServiceBeanTestHelper
                .getTriggerProcess();
        triggerProcess.getTriggerDefinition().setOrganization(ORGANIZATION);

        // when
        when(dm.getReference(TriggerProcess.class, VALID_KEY)).thenReturn(
                triggerProcess);
        when(voService.getParameters()).thenReturn(parameterList);
        triggerService.updateActionParameters(VALID_KEY, parameters);
    }

    @Test(expected = ValidationException.class)
    public void testUpdateActionIntegerParameterOutOfRange()
            throws SaaSApplicationException {
        final String PARAM_VALUE = "123";
        List<VOParameter> parameterList = TriggerServiceBeanTestHelper
                .getVOParameters(ParameterValueType.INTEGER, PARAM_VALUE,
                        Long.valueOf(1L), Long.valueOf(5L));
        TriggerProcess triggerProcess = TriggerServiceBeanTestHelper
                .getTriggerProcess();
        triggerProcess.getTriggerDefinition().setOrganization(ORGANIZATION);

        // when
        when(dm.getReference(TriggerProcess.class, VALID_KEY)).thenReturn(
                triggerProcess);
        when(voService.getParameters()).thenReturn(parameterList);
        triggerService.updateActionParameters(VALID_KEY, parameters);
    }

    @Test
    public void testUpdateActionLongParameter() throws SaaSApplicationException {
        final String PARAM_VALUE = "123";
        List<VOParameter> parameterList = TriggerServiceBeanTestHelper
                .getVOParameters(ParameterValueType.LONG, PARAM_VALUE,
                        Long.valueOf(1L), Long.valueOf(500L));
        parameterList.get(0).setConfigurable(true);
        TriggerProcess triggerProcess = TriggerServiceBeanTestHelper
                .getTriggerProcess();
        triggerProcess.getTriggerDefinition().setOrganization(ORGANIZATION);

        // when
        when(dm.getReference(TriggerProcess.class, VALID_KEY)).thenReturn(
                triggerProcess);
        when(voService.getParameters()).thenReturn(parameterList);
        triggerService.updateActionParameters(VALID_KEY, parameters);
    }

    @Test(expected = ValidationException.class)
    public void testUpdateActionLongParameterNotLong()
            throws SaaSApplicationException {
        final String PARAM_VALUE = "TEST";
        List<VOParameter> parameterList = TriggerServiceBeanTestHelper
                .getVOParameters(ParameterValueType.LONG, PARAM_VALUE,
                        Long.valueOf(1L), Long.valueOf(500L));
        TriggerProcess triggerProcess = TriggerServiceBeanTestHelper
                .getTriggerProcess();
        triggerProcess.getTriggerDefinition().setOrganization(ORGANIZATION);

        // when
        when(dm.getReference(TriggerProcess.class, VALID_KEY)).thenReturn(
                triggerProcess);
        when(voService.getParameters()).thenReturn(parameterList);
        triggerService.updateActionParameters(VALID_KEY, parameters);
    }

    @Test(expected = ValidationException.class)
    public void testUpdateActionLongParameterOutOfRange()
            throws SaaSApplicationException {
        final String PARAM_VALUE = "123";
        List<VOParameter> parameterList = TriggerServiceBeanTestHelper
                .getVOParameters(ParameterValueType.LONG, PARAM_VALUE,
                        Long.valueOf(1L), Long.valueOf(5L));
        TriggerProcess triggerProcess = TriggerServiceBeanTestHelper
                .getTriggerProcess();
        triggerProcess.getTriggerDefinition().setOrganization(ORGANIZATION);

        // when
        when(dm.getReference(TriggerProcess.class, VALID_KEY)).thenReturn(
                triggerProcess);
        when(voService.getParameters()).thenReturn(parameterList);
        triggerService.updateActionParameters(VALID_KEY, parameters);
    }

    @Test
    public void testUpdateActionEnumParameter() throws SaaSApplicationException {
        final String PARAM_VALUE = "Option 1";
        List<VOParameter> parameterList = TriggerServiceBeanTestHelper
                .getVOParameters(ParameterValueType.ENUMERATION, PARAM_VALUE,
                        PARAM_VALUE);
        parameterList.get(0).setConfigurable(true);
        TriggerProcess triggerProcess = TriggerServiceBeanTestHelper
                .getTriggerProcess();
        triggerProcess.getTriggerDefinition().setOrganization(ORGANIZATION);

        // when
        when(dm.getReference(TriggerProcess.class, VALID_KEY)).thenReturn(
                triggerProcess);
        when(voService.getParameters()).thenReturn(parameterList);
        triggerService.updateActionParameters(VALID_KEY, parameters);
    }

    @Test(expected = ValidationException.class)
    public void testUpdateActionEnumParameterNotOnList()
            throws SaaSApplicationException {
        final String PARAM_VALUE = "Option 1";
        List<VOParameter> parameterList = TriggerServiceBeanTestHelper
                .getVOParameters(ParameterValueType.ENUMERATION, PARAM_VALUE,
                        "Option 2");
        TriggerProcess triggerProcess = TriggerServiceBeanTestHelper
                .getTriggerProcess();
        triggerProcess.getTriggerDefinition().setOrganization(ORGANIZATION);

        // when
        when(dm.getReference(TriggerProcess.class, VALID_KEY)).thenReturn(
                triggerProcess);
        when(voService.getParameters()).thenReturn(parameterList);
        triggerService.updateActionParameters(VALID_KEY, parameters);
    }

    @Test
    public void testUpdateActionBooleanParameter()
            throws SaaSApplicationException {
        // given
        final String PARAM_VALUE = "true";
        List<VOParameter> parameterList = TriggerServiceBeanTestHelper
                .getVOParameters(ParameterValueType.BOOLEAN, PARAM_VALUE);
        parameterList.get(0).setConfigurable(true);
        TriggerProcess triggerProcess = TriggerServiceBeanTestHelper
                .getTriggerProcess();
        triggerProcess.getTriggerDefinition().setOrganization(ORGANIZATION);

        // when
        when(dm.getReference(TriggerProcess.class, VALID_KEY)).thenReturn(
                triggerProcess);
        when(voService.getParameters()).thenReturn(parameterList);
        triggerService.updateActionParameters(VALID_KEY, parameters);
    }

    @Test(expected = ValidationException.class)
    public void testUpdateActionBooleanParameterNotBool()
            throws SaaSApplicationException {
        // given
        final String PARAM_VALUE = "TEST";
        List<VOParameter> parameterList = TriggerServiceBeanTestHelper
                .getVOParameters(ParameterValueType.BOOLEAN, PARAM_VALUE);
        TriggerProcess triggerProcess = TriggerServiceBeanTestHelper
                .getTriggerProcess();
        triggerProcess.getTriggerDefinition().setOrganization(ORGANIZATION);

        // when
        when(dm.getReference(TriggerProcess.class, VALID_KEY)).thenReturn(
                triggerProcess);
        when(voService.getParameters()).thenReturn(parameterList);
        triggerService.updateActionParameters(VALID_KEY, parameters);
    }

    @Test
    public void testGetActionParameterExist() throws Exception {
        // given

        // when
        VOTriggerProcessParameter parameter = triggerService
                .getActionParameter(VALID_KEY,
                        TriggerProcessParameterType.PRODUCT);

        VOService paramService = (VOService) parameter.getValue();

        // then
        Assert.assertSame(paramService.getName(), voService.getName());
        Assert.assertEquals(parameter.getKey(), VALID_KEY);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testGetActionParameterNotExist() throws Exception {
        // given
        triggerService = new TriggerServiceBean() {
            @Override
            protected TriggerProcessParameter getTriggerProcessParameter(
                    long actionKey, TriggerProcessParameterType paramName) {
                return null;
            }
        };

        // when
        triggerService.getActionParameter(VALID_KEY,
                TriggerProcessParameterType.PRODUCT);

        // then ObjectNotFoundException
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetActionParameterParamNameNull() throws Exception {
        // given
        // when
        triggerService.getActionParameter(VALID_KEY, null);

        // then IllegalArgumentException
    }

    @Test
    public void testUpdateActionLongParameter_Bug11732() throws Exception {
        // given
        final String PARAM_VALUE = "123";
        List<VOParameter> parameterList = TriggerServiceBeanTestHelper
                .getVOParameters(ParameterValueType.LONG, PARAM_VALUE,
                        Long.valueOf(1L), null);
        parameterList.get(0).setConfigurable(true);
        TriggerProcess triggerProcess = TriggerServiceBeanTestHelper
                .getTriggerProcess();
        triggerProcess.getTriggerDefinition().setOrganization(ORGANIZATION);

        // when
        when(dm.getReference(TriggerProcess.class, VALID_KEY)).thenReturn(
                triggerProcess);
        when(voService.getParameters()).thenReturn(parameterList);
        triggerService.updateActionParameters(VALID_KEY, parameters);
    }

}
