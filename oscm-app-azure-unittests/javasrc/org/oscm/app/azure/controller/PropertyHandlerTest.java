/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *******************************************************************************/
package org.oscm.app.azure.controller;

import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;

import org.oscm.app.azure.data.FlowState;
import org.oscm.app.v1_0.data.PasswordAuthentication;
import org.oscm.app.v1_0.data.ProvisioningSettings;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by PLGrubskiM on 2017-03-27.
 */
public class PropertyHandlerTest {

    PropertyHandler ph;

    @Test
    public void propertiesTest() {
        // given
        // when
        ph = prepareSettingsAndParameters();
        // then
        Assert.assertTrue(ph.getUserName().equals("API_USER_NAME"));
        Assert.assertTrue(ph.getPassword().equals("API_USER_PWD"));
        Assert.assertTrue(ph.getTemplateBaseUrl().equals("http://someUrl.test"));
        Assert.assertTrue(String.valueOf(ph.getReadyTimeout()).equals("1000000"));
        Assert.assertTrue(ph.getMailForCompletion().equals("MAIL_FOR_COMPLETION"));
        Assert.assertTrue(ph.getSubscriptionId().equals("SUBSCRIPTION_ID"));
        Assert.assertTrue(ph.getTenantId().equals("TENANT_ID"));
        Assert.assertTrue(ph.getClientId().equals("CLIENT_ID"));
        Assert.assertTrue(ph.getClientSecret().equals("CLIENT_SECRET"));
        Assert.assertTrue(ph.getRegion().equals("REGION"));
        Assert.assertTrue(ph.getResourceGroupName().equals("RESOURCE_GROUP_NAME"));
        Assert.assertTrue(ph.getVirtualNetwork().equals("VIRTUAL_NETWORK"));
        Assert.assertTrue(ph.getSubnet().equals("SUBNET"));
        Assert.assertTrue(ph.getStorageAccount().equals("STORAGE_ACCOUNT"));
        Assert.assertTrue(ph.getInstanceCount().equals("INSTANCE_COUNT"));
        Assert.assertTrue(ph.getVirtualMachineImageID().equals("VIRTUAL_MACHINE_IMAGE_ID"));
        Assert.assertTrue(ph.getVMName().equals("VM_NAME"));
        Assert.assertTrue(ph.getInstanceName().equals("INSTANCE_NAME"));
        Assert.assertTrue(ph.getTemplateName().equals("TEMPLATE_NAME"));
        Assert.assertTrue(ph.getTemplateParametersName().equals("TEMPLATE_PARAMETERS_NAME"));
        Assert.assertTrue(ph.getDeploymentName().equals("DEPLOYMENT_NAME"));
        Assert.assertTrue(ph.getFlowState().toString().equals("MODIFICATION_REQUESTED"));
        Assert.assertTrue(ph.getStartTime().equals("START_TIME"));

    }

    @Test
    public void getConfigurationAsStringTest() {
        // given
        ph = prepareSettingsAndParameters();
        // when
        final String configurationAsString = ph.getConfigurationAsString();
        // then
        Assert.assertTrue(!configurationAsString.isEmpty());
        Assert.assertTrue(configurationAsString.contains("API_USER_NAME"));
        Assert.assertTrue(configurationAsString.contains("TENANT_ID"));
        Assert.assertTrue(configurationAsString.contains("RESOURCE_GROUP_NAME"));
        Assert.assertTrue(configurationAsString.contains("http://someUrl.test"));
        Assert.assertTrue(configurationAsString.contains("REGION"));
    }

    @Test
    public void getTemplateParametersUrlTest() {
        // given
        String url = "http://someurl.test";
        HashMap<String, String> configSettings = new HashMap<>();
        HashMap<String, String> parameters = new HashMap<>();

        parameters.put("TEMPLATE_BASE_URL", url);
        parameters.put("TEMPLATE_PARAMETERS_NAME", "TEMPLATE_PARAMETERS_NAME");

        ProvisioningSettings newSettings = new ProvisioningSettings(parameters, configSettings, "en");
        ph = new PropertyHandler(newSettings);
        // when
        final String templateParametersUrl = ph.getTemplateParametersUrl();
        // then
        Assert.assertTrue(templateParametersUrl.equals(url + "/" + "TEMPLATE_PARAMETERS_NAME"));
    }

    @Test(expected = RuntimeException.class)
    public void getTemplateParametersUrlTest_invalidURL() {
        // given
        String url = "someurl.test";
        HashMap<String, String> configSettings = new HashMap<>();
        HashMap<String, String> parameters = new HashMap<>();

        parameters.put("TEMPLATE_BASE_URL", url);
        parameters.put("TEMPLATE_PARAMETERS_NAME", "TEMPLATE_PARAMETERS_NAME");

        ProvisioningSettings newSettings = new ProvisioningSettings(parameters, configSettings, "en");
        ph = new PropertyHandler(newSettings);
        // when
        ph.getTemplateParametersUrl();
    }

    @Test
    public void getValidatedPropertyTest() {
        // given
        ph = prepareSettingsAndParameters();
        // when
        ph.getValidatedProperty(ph.getSettings().getParameters(), "CLIENT_ID");
        // then
    }

    @Test(expected = RuntimeException.class)
    public void getValidatedPropertyTest_missing() {
        // given
        ph = prepareSettingsAndParameters();
        ph.getSettings().getParameters().remove("CLIENT_ID");
        // when
        ph.getValidatedProperty(ph.getSettings().getParameters(), "CLIENT_ID");
        // then
    }

    @Test
    public void setResourceGroupNameTest() {
        // given
        ph = prepareSettingsAndParameters();
        // when
        ph.setResourceGroupName("newGroup");
        // then
        Assert.assertTrue(ph.getSettings().getParameters().containsValue("newGroup"));
    }

    @Test(expected = RuntimeException.class)
    public void getTemplateUrlTest_invalidUrl() {
        // given
        ph = prepareSettingsAndParameters();
        ph.getSettings().getParameters().put("TEMPLATE_BASE_URL", "this is not url");
        // when
        ph.getTemplateUrl();
        // then
    }

    @Test
    public void getTemplateParametersUrl_missingFileName() {
        // given
        ph = prepareSettingsAndParameters();
        ph.getSettings().getParameters().remove("TEMPLATE_PARAMETERS_NAME");
        // when
        final String templateUrl = ph.getTemplateParametersUrl();
        // then
        Assert.assertTrue(templateUrl == null);
    }

    @Test
    public void setDeploymentNameTest() {
        // given
        ph = prepareSettingsAndParameters();
        // when
        ph.setDeploymentName("newName");
        // then
        Assert.assertTrue(ph.getSettings().getParameters().get("DEPLOYMENT_NAME").equals("newName"));
    }

    @Test
    public void setFlowStateTest() {
        // given
        ph = prepareSettingsAndParameters();
        // when
        ph.setFlowState(FlowState.CREATING);
        // then
        Assert.assertTrue(ph.getSettings().getParameters().get("FLOW_STATUS").equals(FlowState.CREATING.name()));
    }

    @Test
    public void setStartTimeTest() {
        // given
        ph = prepareSettingsAndParameters();
        // when
        ph.setStartTime("newTime");
        // then
        Assert.assertTrue(ph.getSettings().getParameters().get("START_TIME").equals("newTime"));
    }

    @Test
    public void getTPAuthenticationTest() {
        // given
        ph = prepareSettingsAndParameters();
        PasswordAuthentication auth = mock(PasswordAuthentication.class);
        when(auth.getUserName()).thenReturn("mockUserName");
        ph.getSettings().setAuthentication(auth);
        // when
        final PasswordAuthentication tpAuthentication = ph.getTPAuthentication();
        // then
        Assert.assertTrue(tpAuthentication.getUserName().equals("mockUserName"));
    }

    @Test
    public void getLocaleTest() {
        // given
        ph = prepareSettingsAndParameters();
        // when
        final String customerLocale = ph.getCustomerLocale();
        // then
        Assert.assertTrue(customerLocale.equals("en"));
        // when
        ProvisioningSettings settings = mock(ProvisioningSettings.class);
        ph = new PropertyHandler(settings);
        final String customerLocale1 = ph.getCustomerLocale();
        Assert.assertTrue(customerLocale1.equals("en"));
    }

    private PropertyHandler prepareSettingsAndParameters() {
        // given
        HashMap<String, String> configSettings = new HashMap<>();
        HashMap<String, String> parameters = new HashMap<>();

        configSettings.put("API_USER_NAME", "API_USER_NAME");
        configSettings.put("API_USER_PWD", "API_USER_PWD");
        parameters.put("TEMPLATE_BASE_URL", "http://someUrl.test");
        configSettings.put("READY_TIMEOUT", "1000000");
        parameters.put("MAIL_FOR_COMPLETION", "MAIL_FOR_COMPLETION");
        parameters.put("SUBSCRIPTION_ID", "SUBSCRIPTION_ID");
        parameters.put("TENANT_ID", "TENANT_ID");
        parameters.put("CLIENT_ID", "CLIENT_ID");
        parameters.put("CLIENT_SECRET", "CLIENT_SECRET");
        parameters.put("REGION", "REGION");
        parameters.put("RESOURCE_GROUP_NAME", "RESOURCE_GROUP_NAME");
        parameters.put("VIRTUAL_NETWORK", "VIRTUAL_NETWORK");
        parameters.put("SUBNET", "SUBNET");
        parameters.put("STORAGE_ACCOUNT", "STORAGE_ACCOUNT");
        parameters.put("INSTANCE_COUNT", "INSTANCE_COUNT");
        parameters.put("VIRTUAL_MACHINE_IMAGE_ID", "VIRTUAL_MACHINE_IMAGE_ID");
        parameters.put("VM_NAME", "VM_NAME");
        parameters.put("INSTANCE_NAME", "INSTANCE_NAME");
        parameters.put("TEMPLATE_NAME", "TEMPLATE_NAME");
        parameters.put("TEMPLATE_PARAMETERS_NAME", "TEMPLATE_PARAMETERS_NAME");
        parameters.put("DEPLOYMENT_NAME", "DEPLOYMENT_NAME");
        parameters.put("FLOW_STATUS", "MODIFICATION_REQUESTED");
        parameters.put("START_TIME", "START_TIME");

        ProvisioningSettings newSettings = new ProvisioningSettings(parameters, configSettings, "en");

        // when
        return new PropertyHandler(newSettings);
    }
}
