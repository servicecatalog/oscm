/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 08.11.2010                                                      
 *                                                                              
 *  Completion Time: 15.11.2010                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.triggerservice.assembler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.TriggerDefinition;
import org.oscm.domobjects.TriggerProcess;
import org.oscm.domobjects.TriggerProcessParameter;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.encrypter.AESEncrypter;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.internal.types.enumtypes.TriggerProcessStatus;
import org.oscm.internal.types.enumtypes.TriggerTargetType;
import org.oscm.internal.types.enumtypes.TriggerType;
import org.oscm.internal.types.enumtypes.UserAccountStatus;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOOrganizationPaymentConfiguration;
import org.oscm.internal.vo.VOPaymentType;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOServicePaymentConfiguration;
import org.oscm.internal.vo.VOTriggerDefinition;
import org.oscm.internal.vo.VOTriggerProcess;
import org.oscm.internal.vo.VOTriggerProcessParameter;
import org.oscm.internal.vo.VOUser;
import org.oscm.test.stubs.LocalizerServiceStub;
import org.oscm.types.enumtypes.TriggerProcessParameterName;

/**
 * Tests for the trigger process assembler.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class TriggerProcessAssemblerTest {

    private TriggerProcess triggerProcess;
    private LocalizerFacade localizerFacade;
    private VOService voService;
    private VOOrganizationPaymentConfiguration paymentConfig;

    @Before
    public void setUp() throws Exception {
        AESEncrypter.generateKey();
        voService = new VOService();
        voService.setTechnicalId("techId");
        voService.setName("serviceName");

        VOOrganization voOrg = new VOOrganization();
        voOrg.setOrganizationId("voOrgId");
        voOrg.setName("orgName");
        paymentConfig = new VOOrganizationPaymentConfiguration();
        paymentConfig.setOrganization(voOrg);

        Organization organization = new Organization();
        organization.setOrganizationId("organizationId");
        PlatformUser user = new PlatformUser();
        user.setKey(44);
        user.setOrganization(organization);
        user.setUserId("userId");
        user.setStatus(UserAccountStatus.ACTIVE);
        triggerProcess = new TriggerProcess();
        triggerProcess.setActivationDate(123);
        triggerProcess.setKey(11);
        triggerProcess.setState(TriggerProcessStatus.APPROVED);
        triggerProcess.setUser(user);

        TriggerProcessParameter param = new TriggerProcessParameter();
        param.setKey(22);
        param.setName(TriggerProcessParameterName.OBJECT_ID);
        param.setTriggerProcess(triggerProcess);
        param.setValue("param1Value");

        TriggerProcessParameter param1 = new TriggerProcessParameter();
        param1.setKey(55);
        param1.setName(TriggerProcessParameterName.PRODUCT);
        param1.setTriggerProcess(triggerProcess);
        param1.setValue(voService);

        List<TriggerProcessParameter> params = new ArrayList<>();
        params.add(param);
        params.add(param1);

        triggerProcess.setTriggerProcessParameters(params);

        TriggerDefinition triggerDefinition = new TriggerDefinition();
        triggerDefinition.setKey(33);
        triggerDefinition.setTarget("target");
        triggerDefinition.setTargetType(TriggerTargetType.WEB_SERVICE);
        triggerDefinition.setType(TriggerType.ADD_REVOKE_USER);
        triggerDefinition.setSuspendProcess(true);
        triggerDefinition.setOrganization(organization);

        triggerProcess.setTriggerDefinition(triggerDefinition);

        localizerFacade = new LocalizerFacade(new LocalizerServiceStub() {
            @Override
            public String getLocalizedTextFromDatabase(String localeString,
                    long objectKey, LocalizedObjectTypes objectType) {
                return "";
            }
        }, "en");
    }

    @Test
    public void testConstructor() throws Exception {
        // only for coverage
        TriggerProcessAssembler result = new TriggerProcessAssembler();
        assertNotNull(result);
    }

    @Test
    public void testToVOTriggerProcess_NullInput() throws Exception {
        VOTriggerProcess result = TriggerProcessAssembler
                .toVOTriggerProcess(null, null);
        assertNull(result);
    }

    @Test
    public void testToVOTriggerProcess() throws Exception {
        VOTriggerProcess result = TriggerProcessAssembler
                .toVOTriggerProcess(triggerProcess, localizerFacade);
        validateResult(result, TriggerType.ADD_REVOKE_USER, false);
        assertEquals("param1Value", result.getTargetNames().get(0));
    }

    @Test
    public void testToVOTriggerProcess_Subscribe() throws Exception {
        triggerProcess.getTriggerDefinition()
                .setType(TriggerType.SUBSCRIBE_TO_SERVICE);
        VOTriggerProcess result = TriggerProcessAssembler
                .toVOTriggerProcess(triggerProcess, localizerFacade);
        validateResult(result, TriggerType.SUBSCRIBE_TO_SERVICE, true);
        assertEquals("serviceName", result.getTargetNames().get(1));
    }

    @Test
    public void testToVOTriggerProcess_UpgradeSub() throws Exception {
        triggerProcess.getTriggerDefinition()
                .setType(TriggerType.UPGRADE_SUBSCRIPTION);
        VOTriggerProcess result = TriggerProcessAssembler
                .toVOTriggerProcess(triggerProcess, localizerFacade);
        validateResult(result, TriggerType.UPGRADE_SUBSCRIPTION, true);
        assertEquals("serviceName", result.getTargetNames().get(1));
    }

    @Test
    public void testToVOTriggerProcess_SavePaymentConf_CustomerSpec()
            throws Exception {
        TriggerProcessParameter param = new TriggerProcessParameter();
        param.setKey(55);
        param.setName(TriggerProcessParameterName.CUSTOMER_CONFIGURATION);
        param.setTriggerProcess(triggerProcess);
        param.setValue(paymentConfig);
        triggerProcess.getTriggerProcessParameters().add(param);

        triggerProcess.getTriggerDefinition()
                .setType(TriggerType.SAVE_PAYMENT_CONFIGURATION);
        VOTriggerProcess result = TriggerProcessAssembler
                .toVOTriggerProcess(triggerProcess, localizerFacade);
        validateResult(result, TriggerType.SAVE_PAYMENT_CONFIGURATION, true);
        assertEquals(TriggerProcessParameterName.CUSTOMER_CONFIGURATION.name(),
                result.getParameter());
        assertEquals("orgName (voOrgId)", result.getTargetNames().get(1));
    }

    @Test
    public void testToVOTriggerProcess_SavePaymentConf_CustomerDefault()
            throws Exception {
        TriggerProcessParameter param = new TriggerProcessParameter();
        param.setKey(55);
        param.setName(TriggerProcessParameterName.DEFAULT_CONFIGURATION);
        param.setTriggerProcess(triggerProcess);
        param.setValue(new HashSet<VOPaymentType>());
        triggerProcess.getTriggerProcessParameters().add(param);

        triggerProcess.getTriggerDefinition()
                .setType(TriggerType.SAVE_PAYMENT_CONFIGURATION);
        VOTriggerProcess result = TriggerProcessAssembler
                .toVOTriggerProcess(triggerProcess, localizerFacade);
        validateResult(result, TriggerType.SAVE_PAYMENT_CONFIGURATION, false);
        assertEquals(TriggerProcessParameterName.DEFAULT_CONFIGURATION.name(),
                result.getParameter());
    }

    @Test
    public void testToVOTriggerProcess_SavePaymentConf_ServiceDefault()
            throws Exception {
        TriggerProcessParameter param = new TriggerProcessParameter();
        param.setKey(55);
        param.setName(
                TriggerProcessParameterName.DEFAULT_SERVICE_PAYMENT_CONFIGURATION);
        param.setTriggerProcess(triggerProcess);
        param.setValue(new HashSet<VOPaymentType>());
        triggerProcess.getTriggerProcessParameters().add(param);

        triggerProcess.getTriggerDefinition()
                .setType(TriggerType.SAVE_PAYMENT_CONFIGURATION);
        VOTriggerProcess result = TriggerProcessAssembler
                .toVOTriggerProcess(triggerProcess, localizerFacade);
        validateResult(result, TriggerType.SAVE_PAYMENT_CONFIGURATION, false);
        assertEquals(
                TriggerProcessParameterName.DEFAULT_SERVICE_PAYMENT_CONFIGURATION
                        .name(),
                result.getParameter());
    }

    @Test
    public void testToVOTriggerProcess_SavePaymentConf_ServiceSpecific()
            throws Exception {
        VOServicePaymentConfiguration conf = new VOServicePaymentConfiguration();
        VOService svc = new VOService();
        svc.setServiceId("serviceId");
        conf.setService(svc);
        conf.setEnabledPaymentTypes(new HashSet<VOPaymentType>());
        TriggerProcessParameter param = new TriggerProcessParameter();
        param.setKey(55);
        param.setName(
                TriggerProcessParameterName.SERVICE_PAYMENT_CONFIGURATION);
        param.setTriggerProcess(triggerProcess);
        param.setValue(conf);
        triggerProcess.getTriggerProcessParameters().add(param);

        triggerProcess.getTriggerDefinition()
                .setType(TriggerType.SAVE_PAYMENT_CONFIGURATION);
        VOTriggerProcess result = TriggerProcessAssembler
                .toVOTriggerProcess(triggerProcess, localizerFacade);
        validateResult(result, TriggerType.SAVE_PAYMENT_CONFIGURATION, true);
        assertEquals(TriggerProcessParameterName.SERVICE_PAYMENT_CONFIGURATION
                .name(), result.getParameter());
        assertEquals(svc.getServiceId(), result.getTargetNames().get(1));
    }

    @Test
    public void toVOTriggerProcessParameter_NullValue() throws Exception {
        // given

        // when
        VOTriggerProcessParameter voParam = TriggerProcessAssembler
                .toVOTriggerProcessParameter(null);

        // then
        assertNull(voParam);

    }

    @Test
    public void toVoTriggerProcessParameter_ShouldPass() throws Exception {
        // given
        final long key = 0;
        TriggerProcessParameter parameter = new TriggerProcessParameter();

        parameter.setName(TriggerProcessParameterName.PRODUCT);
        parameter.setValue(new VOService());
        parameter.setKey(key);
        parameter.setTriggerProcess(new TriggerProcess());

        // when
        VOTriggerProcessParameter voParameter = TriggerProcessAssembler
                .toVOTriggerProcessParameter(parameter);

        // then
        assertThat(voParameter, new BaseMatcher<VOTriggerProcessParameter>() {
            @Override
            public boolean matches(Object o) {

                VOTriggerProcessParameter voParameter = (VOTriggerProcessParameter) o;

                return voParameter.getKey() == key
                        && voParameter.getType()
                                .equals(org.oscm.internal.types.enumtypes.TriggerProcessParameterType.PRODUCT)
                        && voParameter.getValue() instanceof VOService;

            }

            @Override
            public void describeTo(Description description) {
            }
        });
    }

    /**
     * Validates the settings of the assembled value object.
     * 
     * @param result
     *            The value object.
     * @param requiresDetailedEvaluation
     */
    private void validateResult(VOTriggerProcess result,
            TriggerType triggerType, boolean requiresDetailedEvaluation) {
        assertNotNull(result);
        assertEquals(11, result.getKey());
        assertEquals(123, result.getActivationDate());
        assertEquals(0, result.getVersion());
        assertEquals(TriggerProcessStatus.APPROVED, result.getStatus());
        if (requiresDetailedEvaluation) {
            assertEquals(2, result.getTargetNames().size());
        } else {
            assertEquals(1, result.getTargetNames().size());
        }
        assertEquals("", result.getReason());
        VOTriggerDefinition triggerDefinition = result.getTriggerDefinition();
        assertEquals(33, triggerDefinition.getKey());
        assertEquals("target", triggerDefinition.getTarget());
        assertEquals(TriggerTargetType.WEB_SERVICE,
                triggerDefinition.getTargetType());
        assertEquals(triggerType, triggerDefinition.getType());
        assertEquals(0, triggerDefinition.getVersion());

        VOUser user = result.getUser();
        assertEquals(44, user.getKey());
        assertEquals(0, user.getVersion());
        assertEquals("organizationId", user.getOrganizationId());
        assertEquals(UserAccountStatus.ACTIVE, user.getStatus());
        assertEquals("userId", user.getUserId());
    }

}
