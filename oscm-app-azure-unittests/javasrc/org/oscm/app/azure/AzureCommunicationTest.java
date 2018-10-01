/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *******************************************************************************/
package org.oscm.app.azure;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.naming.ServiceUnavailableException;

import com.google.common.collect.AbstractIterator;
import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;
import com.microsoft.azure.management.compute.ComputeManagementClient;
import com.microsoft.azure.management.compute.VirtualMachineOperations;
import com.microsoft.azure.management.compute.models.InstanceViewStatus;
import com.microsoft.azure.management.compute.models.NetworkInterfaceReference;
import com.microsoft.azure.management.compute.models.NetworkProfile;
import com.microsoft.azure.management.compute.models.VirtualMachine;
import com.microsoft.azure.management.compute.models.VirtualMachineGetResponse;
import com.microsoft.azure.management.compute.models.VirtualMachineInstanceView;
import com.microsoft.azure.management.network.NetworkInterfaceOperations;
import com.microsoft.azure.management.network.NetworkResourceProviderClient;
import com.microsoft.azure.management.network.PublicIpAddressOperations;
import com.microsoft.azure.management.network.VirtualNetworkOperations;
import com.microsoft.azure.management.network.models.*;
import com.microsoft.azure.management.resources.DeploymentOperationOperations;
import com.microsoft.azure.management.resources.DeploymentOperations;
import com.microsoft.azure.management.resources.ProviderOperations;
import com.microsoft.azure.management.resources.ResourceGroupOperations;
import com.microsoft.azure.management.resources.ResourceManagementClient;
import com.microsoft.azure.management.resources.models.Deployment;
import com.microsoft.azure.management.resources.models.DeploymentExistsResult;
import com.microsoft.azure.management.resources.models.DeploymentExtended;
import com.microsoft.azure.management.resources.models.DeploymentGetResult;
import com.microsoft.azure.management.resources.models.DeploymentOperation;
import com.microsoft.azure.management.resources.models.DeploymentOperationProperties;
import com.microsoft.azure.management.resources.models.DeploymentOperationsCreateResult;
import com.microsoft.azure.management.resources.models.DeploymentOperationsListParameters;
import com.microsoft.azure.management.resources.models.DeploymentOperationsListResult;
import com.microsoft.azure.management.resources.models.DeploymentPropertiesExtended;
import com.microsoft.azure.management.resources.models.LongRunningOperationResponse;
import com.microsoft.azure.management.resources.models.Provider;
import com.microsoft.azure.management.resources.models.ProviderGetResult;
import com.microsoft.azure.management.resources.models.ProviderResourceType;
import com.microsoft.azure.management.resources.models.ResourceGroupExistsResult;
import com.microsoft.azure.management.storage.StorageAccountOperations;
import com.microsoft.azure.management.storage.StorageManagementClient;
import com.microsoft.azure.management.storage.models.StorageAccount;
import com.microsoft.azure.management.storage.models.StorageAccountKeys;
import com.microsoft.azure.management.storage.models.StorageAccountListKeysResponse;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageCredentials;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.windowsazure.core.OperationResponse;
import com.microsoft.windowsazure.core.utils.BOMInputStream;
import com.microsoft.windowsazure.exception.ServiceException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.oscm.app.azure.controller.PropertyHandler;
import org.oscm.app.azure.data.AccessInfo;
import org.oscm.app.azure.data.AzureState;
import org.oscm.app.azure.data.PowerState;
import org.oscm.app.azure.exception.AzureClientException;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.exceptions.AbortException;
import org.oscm.app.v2_0.exceptions.AuthenticationException;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by PLGrubskiM on 2017-03-22.
 */
public class AzureCommunicationTest {

    private static final String CLIENT_ID = "clientId";
    private static final String CLIENT_SECRET = "clientSecret";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String REGION = "region";
    private static final String VMNAME = "vmname";
    private static final String VNETWORK_NAME = "myvnetwork";
    private static final String RERSOURCE_GROUP_NAME = "resGroup";
    private static final String TENANT_ID = "tenant";
    private static final String STORAGE_ACCOUNT_NAME = "storageAcc";
    private static final String TEMPLATE_URL = "http://templateUrl";
    private static final String DEPLOYMENT_NAME = "deploymentName";
    private static final String TEMPLATE_PARAMETER_URL = "http://templateParamUrl";
    private static final String IMAGE_ID_WINDOWS_SERVER_2012 = "WindowsServer 2012-R2-Datacenter";
    private static final String IMAGE_ID_WINDOWS_SERVER_2016 = "WindowsServer 2016-Datacenter";
    private static final String IMAGE_ID_REDHAT = "Linux RedHat";
    private static final String IMAGE_ID_INVALID = "Invalid";

    private AzureCommunication azureComm;
    private ResourceManagementClient resourceManagementClient;
    private ComputeManagementClient computeManagementClient;
    private NetworkResourceProviderClient networkClient;
    private PropertyHandler ph;
    private boolean tokenNull = false;
    private boolean throwEx = false;
    private String state = "Creating";
    private boolean doesResourceGroupExist = false;
    private String powerstate = "Powerstate/RUNNING";
    private boolean throwBOMEx = false;
    private boolean throwCreateOrUpdateEx = false;
    private boolean throwInstanceStartingEx = false;
    private boolean throwInstanceStoppingEx = false;
    private boolean throwResourceClientEx = false;
    private boolean skipPublicIp = false;
    private boolean skipBothIps = false;

    @Before
    public void setUp() {
        ph = mock(PropertyHandler.class);
        resourceManagementClient = mock(ResourceManagementClient.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_emptyClientId() throws AbortException {
        // given
        azureComm = new AzureCommunication(ph);
        // when
        // then
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_emptyUsername() throws AbortException {
        // given
        when(ph.getClientId()).thenReturn(CLIENT_ID);
        // when
        azureComm = new AzureCommunication(ph);
        // then
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_emptyPwd() throws AbortException {
        // given
        when(ph.getClientId()).thenReturn(CLIENT_ID);
        when(ph.getUserName()).thenReturn(USERNAME);
        // when
        azureComm = new AzureCommunication(ph);
        // then
    }

    @Test
    public void constructor_Ok() {
        // given
        // when
        azureComm = new AzureCommunication(ph) {
            @Override
            protected AuthenticationResult acquireToken(AuthenticationContext context, String clientId,
                                                        String apiUserName, String apiPassword)
                    throws ExecutionException, InterruptedException {

                String accessTokenType = "accessTokenType";
                String accessToken = "accessToken";
                String refreshToken = "refreshToken";
                long expiresIn = 1000000L;
                String idToken = "idToken";
                boolean isMultipleResourcesRefreshToken = false;
                AuthenticationResult mockAuthRes = new AuthenticationResult(accessTokenType, accessToken, refreshToken,
                        expiresIn, idToken, null, isMultipleResourcesRefreshToken);
                return mockAuthRes;
            }
        };
        // then
        // no exceptions
    }

    @Test(expected = AzureClientException.class)
    public void constructor_withClientSecret_emptyClientId() {
        // given
        when(ph.getClientSecret()).thenReturn("secret");
        // when
        azureComm = new AzureCommunication(ph) {
            @Override
            protected AuthenticationResult acquireToken(AuthenticationContext context, String clientId,
                                                        String apiUserName, String apiPassword)
                    throws ExecutionException, InterruptedException {

                String accessTokenType = "accessTokenType";
                String accessToken = "accessToken";
                String refreshToken = "refreshToken";
                long expiresIn = 1000000L;
                String idToken = "idToken";
                boolean isMultipleResourcesRefreshToken = false;
                AuthenticationResult mockAuthRes = new AuthenticationResult(accessTokenType, accessToken, refreshToken,
                        expiresIn, idToken, null, isMultipleResourcesRefreshToken);
                return mockAuthRes;
            }
        };
        // then
        // no exceptions
    }

    @Test
    public void createInstanceTest() throws Exception {
        // given
        when(ph.getRegion()).thenReturn(REGION);
        when(ph.getResourceGroupName()).thenReturn(RERSOURCE_GROUP_NAME);
        when(ph.getStorageAccount()).thenReturn(STORAGE_ACCOUNT_NAME);
        when(ph.getTemplateUrl()).thenReturn(TEMPLATE_URL);
        when(ph.getDeploymentName()).thenReturn(DEPLOYMENT_NAME);
        azureComm = prepareAzureCommWithMocks();
        // when
        azureComm.createInstance();
        // then
        verify(resourceManagementClient, times(1)).getDeploymentsOperations();
    }

    @Test(expected = AzureClientException.class)
    public void createInstanceTest_exceptionInBOM() throws Exception {
        // given
        throwBOMEx = true;
        when(ph.getRegion()).thenReturn(REGION);
        when(ph.getResourceGroupName()).thenReturn(RERSOURCE_GROUP_NAME);
        when(ph.getStorageAccount()).thenReturn(STORAGE_ACCOUNT_NAME);
        when(ph.getTemplateUrl()).thenReturn(TEMPLATE_URL);
        when(ph.getDeploymentName()).thenReturn(DEPLOYMENT_NAME);
        azureComm = prepareAzureCommWithMocks();
        // when
        azureComm.createInstance();
        // then
        verify(resourceManagementClient, times(1)).getDeploymentsOperations();
    }

    @Test(expected = AzureClientException.class)
    public void createInstanceTest_exceptionIncreateOrUpdate() throws Exception {
        // given
        throwCreateOrUpdateEx = true;
        when(ph.getRegion()).thenReturn(REGION);
        when(ph.getResourceGroupName()).thenReturn(RERSOURCE_GROUP_NAME);
        when(ph.getStorageAccount()).thenReturn(STORAGE_ACCOUNT_NAME);
        when(ph.getTemplateUrl()).thenReturn(TEMPLATE_URL);
        when(ph.getDeploymentName()).thenReturn(DEPLOYMENT_NAME);
        azureComm = prepareAzureCommWithMocks();
        // when
        azureComm.createInstance();
        // then
        verify(resourceManagementClient, times(1)).getDeploymentsOperations();
    }

    @Test
    public void updateInstanceTest() throws AbortException {
        // given
        when(ph.getRegion()).thenReturn(REGION);
        when(ph.getResourceGroupName()).thenReturn(RERSOURCE_GROUP_NAME);
        when(ph.getStorageAccount()).thenReturn(STORAGE_ACCOUNT_NAME);
        when(ph.getTemplateUrl()).thenReturn(TEMPLATE_URL);
        when(ph.getDeploymentName()).thenReturn(DEPLOYMENT_NAME);
        when(ph.getTemplateParametersUrl()).thenReturn(TEMPLATE_PARAMETER_URL);
        when(ph.getInstanceCount()).thenReturn("1");
        when(ph.getVirtualMachineImageID()).thenReturn(IMAGE_ID_WINDOWS_SERVER_2012);
        azureComm = prepareAzureCommWithMocks();
        // when
        azureComm.updateInstance();
        // then
        verify(resourceManagementClient, times(1)).getDeploymentsOperations();
    }

    @Test
    public void deleteInstanceTest() throws AbortException {
        // given
        when(ph.getRegion()).thenReturn(REGION);
        when(ph.getResourceGroupName()).thenReturn(RERSOURCE_GROUP_NAME);
        when(ph.getStorageAccount()).thenReturn(STORAGE_ACCOUNT_NAME);
        when(ph.getTemplateUrl()).thenReturn(TEMPLATE_URL);
        when(ph.getDeploymentName()).thenReturn(DEPLOYMENT_NAME);
        when(ph.getTemplateParametersUrl()).thenReturn(TEMPLATE_PARAMETER_URL);
        when(ph.getInstanceCount()).thenReturn("1");
        when(ph.getVirtualMachineImageID()).thenReturn(IMAGE_ID_WINDOWS_SERVER_2012);
        when(ph.getVMName()).thenReturn(VMNAME);
        when(ph.getVirtualNetwork()).thenReturn(VNETWORK_NAME);
        azureComm = prepareAzureCommWithMocks();
        // when
        azureComm.deleteInstance();
        // then
        verify(resourceManagementClient, times(2)).getDeploymentsOperations();
    }

    @Test
    public void getAccessTokenFromClientCredentialsTest() throws InterruptedException, MalformedURLException, ExecutionException, ServiceUnavailableException, AuthenticationException {
        // given
        when(ph.getRegion()).thenReturn(REGION);
        when(ph.getResourceGroupName()).thenReturn(RERSOURCE_GROUP_NAME);
        when(ph.getStorageAccount()).thenReturn(STORAGE_ACCOUNT_NAME);
        when(ph.getTemplateUrl()).thenReturn(TEMPLATE_URL);
        when(ph.getDeploymentName()).thenReturn(DEPLOYMENT_NAME);
        when(ph.getTemplateParametersUrl()).thenReturn(TEMPLATE_PARAMETER_URL);
        when(ph.getInstanceCount()).thenReturn("1");
        when(ph.getVirtualMachineImageID()).thenReturn(IMAGE_ID_WINDOWS_SERVER_2012);
        when(ph.getVMName()).thenReturn(VMNAME);
        azureComm = prepareAzureCommWithMocks();
        // when
        final AuthenticationResult accessTokenFromClientCredentials = azureComm.getAccessTokenFromClientCredentials(TENANT_ID, CLIENT_ID, CLIENT_SECRET);
        // then
        Assert.assertTrue(accessTokenFromClientCredentials != null);

    }

    @Test(expected = ServiceUnavailableException.class)
    public void getAccessTokenFromClientCredentialsTest_nullResult() throws InterruptedException, MalformedURLException, ExecutionException, ServiceUnavailableException, AuthenticationException {
        // given
        tokenNull = true;

        when(ph.getRegion()).thenReturn(REGION);
        when(ph.getResourceGroupName()).thenReturn(RERSOURCE_GROUP_NAME);
        when(ph.getStorageAccount()).thenReturn(STORAGE_ACCOUNT_NAME);
        when(ph.getTemplateUrl()).thenReturn(TEMPLATE_URL);
        when(ph.getDeploymentName()).thenReturn(DEPLOYMENT_NAME);
        when(ph.getTemplateParametersUrl()).thenReturn(TEMPLATE_PARAMETER_URL);
        when(ph.getInstanceCount()).thenReturn("1");
        when(ph.getVirtualMachineImageID()).thenReturn(IMAGE_ID_WINDOWS_SERVER_2012);
        when(ph.getVMName()).thenReturn(VMNAME);
        azureComm = prepareAzureCommWithMocks();
        // when
        azureComm.getAccessTokenFromClientCredentials(TENANT_ID, CLIENT_ID, CLIENT_SECRET);
        // then
    }

    @Test
    public void createAndLogAzureExceptionTest() {
        // given
        azureComm = prepareAzureCommWithMocks();
        // when
        Throwable exception = new APPlatformException("inner message");
        final AzureClientException ex = azureComm.createAndLogAzureException("msg", exception);
        // then
        Assert.assertTrue(ex.getCause() instanceof APPlatformException);
        Assert.assertTrue(ex.getCause().getMessage().equals("inner message"));
    }

    @Test
    public void deleteVMContainerTest() throws ServiceException, IOException, URISyntaxException, InvalidKeyException {
        // given
        azureComm = prepareAzureCommWithMocks();
        // when
        azureComm.deleteVMContainer();
        // then
        // no exceptions
    }

    @Test(expected = InvalidKeyException.class)
    public void deleteVMContainerTest_exception() throws ServiceException, IOException, URISyntaxException, InvalidKeyException {
        // given
        throwEx = true;
        azureComm = prepareAzureCommWithMocks();
        // when
        azureComm.deleteVMContainer();
        // then
    }

    @Test
    public void startInstanceTest() throws ServiceException, IOException, URISyntaxException, InvalidKeyException {
        // given
        when(ph.getRegion()).thenReturn(REGION);
        when(ph.getResourceGroupName()).thenReturn(RERSOURCE_GROUP_NAME);
        when(ph.getStorageAccount()).thenReturn(STORAGE_ACCOUNT_NAME);
        when(ph.getTemplateUrl()).thenReturn(TEMPLATE_URL);
        when(ph.getDeploymentName()).thenReturn(DEPLOYMENT_NAME);
        when(ph.getTemplateParametersUrl()).thenReturn(TEMPLATE_PARAMETER_URL);
        when(ph.getInstanceCount()).thenReturn("1");
        when(ph.getVirtualMachineImageID()).thenReturn(IMAGE_ID_WINDOWS_SERVER_2012);
        when(ph.getVMName()).thenReturn(VMNAME);
        azureComm = prepareAzureCommWithMocks();
        // when
        azureComm.startInstance();
        // then
        // no exceptions
    }

    @Test(expected = AzureClientException.class)
    public void startInstanceTest_exception() throws ServiceException, IOException, URISyntaxException, InvalidKeyException {
        // given
        throwInstanceStartingEx = true;
        when(ph.getRegion()).thenReturn(REGION);
        when(ph.getResourceGroupName()).thenReturn(RERSOURCE_GROUP_NAME);
        when(ph.getStorageAccount()).thenReturn(STORAGE_ACCOUNT_NAME);
        when(ph.getTemplateUrl()).thenReturn(TEMPLATE_URL);
        when(ph.getDeploymentName()).thenReturn(DEPLOYMENT_NAME);
        when(ph.getTemplateParametersUrl()).thenReturn(TEMPLATE_PARAMETER_URL);
        when(ph.getInstanceCount()).thenReturn("1");
        when(ph.getVirtualMachineImageID()).thenReturn(IMAGE_ID_WINDOWS_SERVER_2012);
        when(ph.getVMName()).thenReturn(VMNAME);
        azureComm = prepareAzureCommWithMocks();
        // when
        azureComm.startInstance();
        // then
        // no exceptions
    }

    @Test
    public void stopInstanceTest() throws ServiceException, IOException, URISyntaxException, InvalidKeyException {
        // given
        when(ph.getRegion()).thenReturn(REGION);
        when(ph.getResourceGroupName()).thenReturn(RERSOURCE_GROUP_NAME);
        when(ph.getStorageAccount()).thenReturn(STORAGE_ACCOUNT_NAME);
        when(ph.getTemplateUrl()).thenReturn(TEMPLATE_URL);
        when(ph.getDeploymentName()).thenReturn(DEPLOYMENT_NAME);
        when(ph.getTemplateParametersUrl()).thenReturn(TEMPLATE_PARAMETER_URL);
        when(ph.getInstanceCount()).thenReturn("1");
        when(ph.getVirtualMachineImageID()).thenReturn(IMAGE_ID_WINDOWS_SERVER_2012);
        when(ph.getVMName()).thenReturn(VMNAME);
        azureComm = prepareAzureCommWithMocks();
        // when
        azureComm.stopInstance();
        // then
        // no exceptions
    }

    @Test(expected = AzureClientException.class)
    public void stopInstanceTest_exception() throws ServiceException, IOException, URISyntaxException, InvalidKeyException {
        // given
        throwInstanceStoppingEx = true;
        when(ph.getRegion()).thenReturn(REGION);
        when(ph.getResourceGroupName()).thenReturn(RERSOURCE_GROUP_NAME);
        when(ph.getStorageAccount()).thenReturn(STORAGE_ACCOUNT_NAME);
        when(ph.getTemplateUrl()).thenReturn(TEMPLATE_URL);
        when(ph.getDeploymentName()).thenReturn(DEPLOYMENT_NAME);
        when(ph.getTemplateParametersUrl()).thenReturn(TEMPLATE_PARAMETER_URL);
        when(ph.getInstanceCount()).thenReturn("1");
        when(ph.getVirtualMachineImageID()).thenReturn(IMAGE_ID_WINDOWS_SERVER_2012);
        when(ph.getVMName()).thenReturn(VMNAME);
        azureComm = prepareAzureCommWithMocks();
        // when
        azureComm.stopInstance();
        // then
        // no exceptions
    }

    @Test
    public void getDeploymentStateTest() throws ServiceException, IOException, URISyntaxException, InvalidKeyException {
        // given
        state = "Running";

        when(ph.getRegion()).thenReturn(REGION);
        when(ph.getResourceGroupName()).thenReturn(RERSOURCE_GROUP_NAME);
        when(ph.getStorageAccount()).thenReturn(STORAGE_ACCOUNT_NAME);
        when(ph.getTemplateUrl()).thenReturn(TEMPLATE_URL);
        when(ph.getDeploymentName()).thenReturn(DEPLOYMENT_NAME);
        when(ph.getTemplateParametersUrl()).thenReturn(TEMPLATE_PARAMETER_URL);
        when(ph.getInstanceCount()).thenReturn("1");
        when(ph.getVirtualMachineImageID()).thenReturn(IMAGE_ID_WINDOWS_SERVER_2012);
        when(ph.getVMName()).thenReturn(VMNAME);
        azureComm = prepareAzureCommWithMocks();
        // when
        final AzureState deploymentState = azureComm.getDeploymentState();
        // then
        Assert.assertTrue(deploymentState != null);
        Assert.assertTrue(deploymentState.getProvisioningState().equals("Running"));
        // no exceptions
    }

    @Test(expected = AzureClientException.class)
    public void getDeploymentStateTest_exception() throws ServiceException, IOException, URISyntaxException, InvalidKeyException {
        // given
        throwResourceClientEx = true;
        state = "Running";

        when(ph.getRegion()).thenReturn(REGION);
        when(ph.getResourceGroupName()).thenReturn(RERSOURCE_GROUP_NAME);
        when(ph.getStorageAccount()).thenReturn(STORAGE_ACCOUNT_NAME);
        when(ph.getTemplateUrl()).thenReturn(TEMPLATE_URL);
        when(ph.getDeploymentName()).thenReturn(DEPLOYMENT_NAME);
        when(ph.getTemplateParametersUrl()).thenReturn(TEMPLATE_PARAMETER_URL);
        when(ph.getInstanceCount()).thenReturn("1");
        when(ph.getVirtualMachineImageID()).thenReturn(IMAGE_ID_WINDOWS_SERVER_2012);
        when(ph.getVMName()).thenReturn(VMNAME);
        azureComm = prepareAzureCommWithMocks();
        // when
        final AzureState deploymentState = azureComm.getDeploymentState();
        // then
        Assert.assertTrue(deploymentState != null);
        Assert.assertTrue(deploymentState.getProvisioningState().equals("Running"));
        // no exceptions
    }

    @Test
    public void getDeploymentStateTest_failed() throws ServiceException, IOException, URISyntaxException, InvalidKeyException {
        // given
        state = "Failed";

        when(ph.getRegion()).thenReturn(REGION);
        when(ph.getResourceGroupName()).thenReturn(RERSOURCE_GROUP_NAME);
        when(ph.getStorageAccount()).thenReturn(STORAGE_ACCOUNT_NAME);
        when(ph.getTemplateUrl()).thenReturn(TEMPLATE_URL);
        when(ph.getDeploymentName()).thenReturn(DEPLOYMENT_NAME);
        when(ph.getTemplateParametersUrl()).thenReturn(TEMPLATE_PARAMETER_URL);
        when(ph.getInstanceCount()).thenReturn("1");
        when(ph.getVirtualMachineImageID()).thenReturn(IMAGE_ID_WINDOWS_SERVER_2012);
        when(ph.getVMName()).thenReturn(VMNAME);
        azureComm = prepareAzureCommWithMocks();
        // when
        final AzureState deploymentState = azureComm.getDeploymentState();
        // then
        Assert.assertTrue(deploymentState != null);
        Assert.assertTrue(deploymentState.getProvisioningState().equals("Failed"));
    }

    @Test
    public void getDeletingStateTest() {
        // given
        doesResourceGroupExist = false;

        when(ph.getRegion()).thenReturn(REGION);
        when(ph.getResourceGroupName()).thenReturn(RERSOURCE_GROUP_NAME);
        when(ph.getStorageAccount()).thenReturn(STORAGE_ACCOUNT_NAME);
        when(ph.getTemplateUrl()).thenReturn(TEMPLATE_URL);
        when(ph.getDeploymentName()).thenReturn(DEPLOYMENT_NAME);
        when(ph.getTemplateParametersUrl()).thenReturn(TEMPLATE_PARAMETER_URL);
        when(ph.getInstanceCount()).thenReturn("1");
        when(ph.getVirtualMachineImageID()).thenReturn(IMAGE_ID_WINDOWS_SERVER_2012);
        when(ph.getVMName()).thenReturn(VMNAME);
        azureComm = prepareAzureCommWithMocks();
        // when
        final AzureState deletingState = azureComm.getDeletingState();
        // then
        Assert.assertTrue(deletingState.getProvisioningState().equals("Deleted"));
    }

    @Test
    public void getDeletingStateTest_withResGroup() {
        // given
        doesResourceGroupExist = true;

        when(ph.getRegion()).thenReturn(REGION);
        when(ph.getResourceGroupName()).thenReturn(RERSOURCE_GROUP_NAME);
        when(ph.getStorageAccount()).thenReturn(STORAGE_ACCOUNT_NAME);
        when(ph.getTemplateUrl()).thenReturn(TEMPLATE_URL);
        when(ph.getDeploymentName()).thenReturn(DEPLOYMENT_NAME);
        when(ph.getTemplateParametersUrl()).thenReturn(TEMPLATE_PARAMETER_URL);
        when(ph.getInstanceCount()).thenReturn("1");
        when(ph.getVirtualMachineImageID()).thenReturn(IMAGE_ID_WINDOWS_SERVER_2012);
        when(ph.getVMName()).thenReturn(VMNAME);
        azureComm = prepareAzureCommWithMocks();
        // when
        final AzureState deletingState = azureComm.getDeletingState();
        // then
        Assert.assertTrue(deletingState.getProvisioningState().equals("Deleting"));
    }

    @Test
    public void getStartingStateTest() {
        // given
        when(ph.getRegion()).thenReturn(REGION);
        when(ph.getResourceGroupName()).thenReturn(RERSOURCE_GROUP_NAME);
        when(ph.getStorageAccount()).thenReturn(STORAGE_ACCOUNT_NAME);
        when(ph.getTemplateUrl()).thenReturn(TEMPLATE_URL);
        when(ph.getDeploymentName()).thenReturn(DEPLOYMENT_NAME);
        when(ph.getTemplateParametersUrl()).thenReturn(TEMPLATE_PARAMETER_URL);
        when(ph.getInstanceCount()).thenReturn("1");
        when(ph.getVirtualMachineImageID()).thenReturn(IMAGE_ID_WINDOWS_SERVER_2012);
        when(ph.getVMName()).thenReturn(VMNAME);
        azureComm = prepareAzureCommWithMocks();
        // when
        final AzureState startingState = azureComm.getStartingState();
        // then
        Assert.assertTrue(startingState.getProvisioningState().equals("Succeeded"));
    }

    @Test
    public void getStartingStateTest2() {
        // given
        powerstate = "Powerstate/DEALLOCATED";
        when(ph.getRegion()).thenReturn(REGION);
        when(ph.getResourceGroupName()).thenReturn(RERSOURCE_GROUP_NAME);
        when(ph.getStorageAccount()).thenReturn(STORAGE_ACCOUNT_NAME);
        when(ph.getTemplateUrl()).thenReturn(TEMPLATE_URL);
        when(ph.getDeploymentName()).thenReturn(DEPLOYMENT_NAME);
        when(ph.getTemplateParametersUrl()).thenReturn(TEMPLATE_PARAMETER_URL);
        when(ph.getInstanceCount()).thenReturn("1");
        when(ph.getVirtualMachineImageID()).thenReturn(IMAGE_ID_WINDOWS_SERVER_2012);
        when(ph.getVMName()).thenReturn(VMNAME);
        azureComm = prepareAzureCommWithMocks();
        // when
        final AzureState startingState = azureComm.getStartingState();
        // then
        Assert.assertTrue(startingState.getProvisioningState().equals("Running"));
    }

    @Test
    public void getStoppingStateTest() {
        // given
        when(ph.getRegion()).thenReturn(REGION);
        when(ph.getResourceGroupName()).thenReturn(RERSOURCE_GROUP_NAME);
        when(ph.getStorageAccount()).thenReturn(STORAGE_ACCOUNT_NAME);
        when(ph.getTemplateUrl()).thenReturn(TEMPLATE_URL);
        when(ph.getDeploymentName()).thenReturn(DEPLOYMENT_NAME);
        when(ph.getTemplateParametersUrl()).thenReturn(TEMPLATE_PARAMETER_URL);
        when(ph.getInstanceCount()).thenReturn("1");
        when(ph.getVirtualMachineImageID()).thenReturn(IMAGE_ID_WINDOWS_SERVER_2012);
        when(ph.getVMName()).thenReturn(VMNAME);
        azureComm = prepareAzureCommWithMocks();
        // when
        final AzureState startingState = azureComm.getStoppingState();
        // then
        Assert.assertTrue(startingState.getProvisioningState().equals("Running"));
    }

    @Test
    public void getStoppingStateTest2() {
        // given
        powerstate = "Powerstate/DEALLOCATED";
        when(ph.getRegion()).thenReturn(REGION);
        when(ph.getResourceGroupName()).thenReturn(RERSOURCE_GROUP_NAME);
        when(ph.getStorageAccount()).thenReturn(STORAGE_ACCOUNT_NAME);
        when(ph.getTemplateUrl()).thenReturn(TEMPLATE_URL);
        when(ph.getDeploymentName()).thenReturn(DEPLOYMENT_NAME);
        when(ph.getTemplateParametersUrl()).thenReturn(TEMPLATE_PARAMETER_URL);
        when(ph.getInstanceCount()).thenReturn("1");
        when(ph.getVirtualMachineImageID()).thenReturn(IMAGE_ID_WINDOWS_SERVER_2012);
        when(ph.getVMName()).thenReturn(VMNAME);
        azureComm = prepareAzureCommWithMocks();
        // when
        final AzureState startingState = azureComm.getStoppingState();
        // then
        Assert.assertTrue(startingState.getProvisioningState().equals("Succeeded"));
    }

    @Test
    public void getPowerState() {
        // given
        when(ph.getRegion()).thenReturn(REGION);
        when(ph.getResourceGroupName()).thenReturn(RERSOURCE_GROUP_NAME);
        when(ph.getStorageAccount()).thenReturn(STORAGE_ACCOUNT_NAME);
        when(ph.getTemplateUrl()).thenReturn(TEMPLATE_URL);
        when(ph.getDeploymentName()).thenReturn(DEPLOYMENT_NAME);
        when(ph.getTemplateParametersUrl()).thenReturn(TEMPLATE_PARAMETER_URL);
        when(ph.getInstanceCount()).thenReturn("1");
        when(ph.getVirtualMachineImageID()).thenReturn(IMAGE_ID_WINDOWS_SERVER_2012);
        when(ph.getVMName()).thenReturn(VMNAME);
        azureComm = prepareAzureCommWithMocks();
        // when
        final ArrayList<PowerState> powerStates = azureComm.getPowerStates();
        // then
        Assert.assertTrue(powerStates.get(0).name().equalsIgnoreCase("running"));
    }

    @Test
    public void getPowerState_unknown() {
        // given
        powerstate = "this / powerstate/is invalid";

        when(ph.getRegion()).thenReturn(REGION);
        when(ph.getResourceGroupName()).thenReturn(RERSOURCE_GROUP_NAME);
        when(ph.getStorageAccount()).thenReturn(STORAGE_ACCOUNT_NAME);
        when(ph.getTemplateUrl()).thenReturn(TEMPLATE_URL);
        when(ph.getDeploymentName()).thenReturn(DEPLOYMENT_NAME);
        when(ph.getTemplateParametersUrl()).thenReturn(TEMPLATE_PARAMETER_URL);
        when(ph.getInstanceCount()).thenReturn("1");
        when(ph.getVirtualMachineImageID()).thenReturn(IMAGE_ID_WINDOWS_SERVER_2012);
        when(ph.getVMName()).thenReturn(VMNAME);
        azureComm = prepareAzureCommWithMocks();
        // when
        final ArrayList<PowerState> powerStates = azureComm.getPowerStates();
        // then
        Assert.assertTrue(powerStates.get(0).name().equalsIgnoreCase("unknown"));
    }

    @Test(expected = AzureClientException.class)
    public void getAvailableRegionsTest_exception() {
        // given
        throwEx = true;

        when(ph.getRegion()).thenReturn(REGION);
        when(ph.getResourceGroupName()).thenReturn(RERSOURCE_GROUP_NAME);
        when(ph.getStorageAccount()).thenReturn(STORAGE_ACCOUNT_NAME);
        when(ph.getTemplateUrl()).thenReturn(TEMPLATE_URL);
        when(ph.getDeploymentName()).thenReturn(DEPLOYMENT_NAME);
        when(ph.getTemplateParametersUrl()).thenReturn(TEMPLATE_PARAMETER_URL);
        when(ph.getInstanceCount()).thenReturn("1");
        when(ph.getVirtualMachineImageID()).thenReturn(IMAGE_ID_WINDOWS_SERVER_2012);
        when(ph.getVMName()).thenReturn(VMNAME);
        azureComm = prepareAzureCommWithMocks();
        // when
        final ArrayList<String> availableRegions = azureComm.getAvailableRegions();
        // then
        Assert.assertTrue(!availableRegions.isEmpty());
    }

    @Test
    public void getAvailableRegionsTest() {
        // given
        throwEx = false;

        when(ph.getRegion()).thenReturn(REGION);
        when(ph.getResourceGroupName()).thenReturn(RERSOURCE_GROUP_NAME);
        when(ph.getStorageAccount()).thenReturn(STORAGE_ACCOUNT_NAME);
        when(ph.getTemplateUrl()).thenReturn(TEMPLATE_URL);
        when(ph.getDeploymentName()).thenReturn(DEPLOYMENT_NAME);
        when(ph.getTemplateParametersUrl()).thenReturn(TEMPLATE_PARAMETER_URL);
        when(ph.getInstanceCount()).thenReturn("1");
        when(ph.getVirtualMachineImageID()).thenReturn(IMAGE_ID_WINDOWS_SERVER_2012);
        when(ph.getVMName()).thenReturn(VMNAME);
        azureComm = prepareAzureCommWithMocks();
        // when
        final ArrayList<String> availableRegions = azureComm.getAvailableRegions();
        // then
        Assert.assertTrue(availableRegions.isEmpty());
    }

    @Test
    public void getAccessInfoTest() {
        // given
        when(ph.getRegion()).thenReturn(REGION);
        when(ph.getResourceGroupName()).thenReturn(RERSOURCE_GROUP_NAME);
        when(ph.getStorageAccount()).thenReturn(STORAGE_ACCOUNT_NAME);
        when(ph.getTemplateUrl()).thenReturn(TEMPLATE_URL);
        when(ph.getDeploymentName()).thenReturn(DEPLOYMENT_NAME);
        when(ph.getTemplateParametersUrl()).thenReturn(TEMPLATE_PARAMETER_URL);
        when(ph.getInstanceCount()).thenReturn("1");
        when(ph.getVirtualMachineImageID()).thenReturn(IMAGE_ID_WINDOWS_SERVER_2012);
        when(ph.getVMName()).thenReturn(VMNAME);
        azureComm = prepareAzureCommWithMocks();
        // when
        final AccessInfo state = azureComm.getAccessInfo("state");
        // then
        final String output = state.getOutput("en");
        Assert.assertTrue(output.contains("someIp"));
        Assert.assertTrue(output.contains("state"));
        Assert.assertTrue(output.contains("vmname"));
    }

    @Test
    public void getAccessInfoTest_prvIp() {
        // given
        skipPublicIp = true;
        when(ph.getRegion()).thenReturn(REGION);
        when(ph.getResourceGroupName()).thenReturn(RERSOURCE_GROUP_NAME);
        when(ph.getStorageAccount()).thenReturn(STORAGE_ACCOUNT_NAME);
        when(ph.getTemplateUrl()).thenReturn(TEMPLATE_URL);
        when(ph.getDeploymentName()).thenReturn(DEPLOYMENT_NAME);
        when(ph.getTemplateParametersUrl()).thenReturn(TEMPLATE_PARAMETER_URL);
        when(ph.getInstanceCount()).thenReturn("1");
        when(ph.getVirtualMachineImageID()).thenReturn(IMAGE_ID_WINDOWS_SERVER_2012);
        when(ph.getVMName()).thenReturn(VMNAME);
        azureComm = prepareAzureCommWithMocks();
        // when
        final AccessInfo state = azureComm.getAccessInfo("state");
        // then
        final String output = state.getOutput("en");
        Assert.assertTrue(output.contains("somePrvIp"));
        Assert.assertTrue(output.contains("state"));
        Assert.assertTrue(output.contains("vmname"));
    }

    @Test
    public void getAccessInfoTest_noIps() {
        // given
        skipPublicIp = true;
        skipBothIps = true;
        when(ph.getRegion()).thenReturn(REGION);
        when(ph.getResourceGroupName()).thenReturn(RERSOURCE_GROUP_NAME);
        when(ph.getStorageAccount()).thenReturn(STORAGE_ACCOUNT_NAME);
        when(ph.getTemplateUrl()).thenReturn(TEMPLATE_URL);
        when(ph.getDeploymentName()).thenReturn(DEPLOYMENT_NAME);
        when(ph.getTemplateParametersUrl()).thenReturn(TEMPLATE_PARAMETER_URL);
        when(ph.getInstanceCount()).thenReturn("1");
        when(ph.getVirtualMachineImageID()).thenReturn(IMAGE_ID_WINDOWS_SERVER_2012);
        when(ph.getVMName()).thenReturn(VMNAME);
        azureComm = prepareAzureCommWithMocks();
        // when
        final AccessInfo state = azureComm.getAccessInfo("state");
        // then
        final String output = state.getOutput("en");
        Assert.assertTrue(output.contains("state"));
        Assert.assertTrue(output.contains("vmname"));
    }


    private AzureCommunication prepareAzureCommWithMocks() {
        return new AzureCommunication(ph) {


            @Override
            protected AuthenticationResult acquireToken(AuthenticationContext context, String clientId,
                                                        String apiUserName, String apiPassword)
                    throws ExecutionException, InterruptedException {
                return prepareAuthResult();
            }

            @Override
            protected AuthenticationResult acquireToken(AuthenticationContext context,
                                                        ClientCredential clientCredential) throws ExecutionException, InterruptedException {
                return tokenNull ? null : prepareAuthResult();
            }

            private AuthenticationResult prepareAuthResult() {
                String accessTokenType = "accessTokenType";
                String accessToken = "accessToken";
                String refreshToken = "refreshToken";
                long expiresIn = 1000000L;
                String idToken = "idToken";
                boolean isMultipleResourcesRefreshToken = false;
                AuthenticationResult mockAuthRes = new AuthenticationResult(accessTokenType, accessToken, refreshToken,
                        expiresIn, idToken, null, isMultipleResourcesRefreshToken);
                return mockAuthRes;
            }

            @Override
            protected void createOrUpdate() throws ServiceException, IOException, URISyntaxException {
                if (throwCreateOrUpdateEx) {
                    throw new IOException("ex");
                }
            }

            @Override
            protected Iterator<StorageAccount> getStorageAccounts() throws ServiceException, IOException, URISyntaxException {
                Iterator<StorageAccount> storageAccs = new AbstractIterator<StorageAccount>() {

                    int howMany = 0;

                    @Override
                    protected StorageAccount computeNext() {
                        if (howMany >= 1) {
                            return null;
                        }
                        StorageAccount sa = mock(StorageAccount.class);
                        when(sa.getName()).thenReturn(STORAGE_ACCOUNT_NAME);
                        howMany++;
                        return sa;
                    }
                };
                return storageAccs;
            }

            @Override
            protected BOMInputStream getBOMInputStream(URL source, String url) throws IOException {
                BOMInputStream in = mock(BOMInputStream.class);
                return in;
            }

            @Override
            protected String getBOMtoString(BOMInputStream in) throws IOException {
                if (throwBOMEx) {
                    throw new IOException("ex");
                }
                return "bomToString";
            }

            @Override
            public ResourceManagementClient getResourceClient() {
                DeploymentOperations operations = mock(DeploymentOperations.class);
                try {
                    DeploymentOperationsCreateResult value = mock(DeploymentOperationsCreateResult.class);
                    when(operations.createOrUpdate(any(String.class), any(String.class), any(Deployment.class)))
                            .thenReturn(value);
                    LongRunningOperationResponse result = mock(LongRunningOperationResponse.class);
                    when(operations.beginDeleting(any(String.class), any(String.class))).thenReturn(result);
                    DeploymentExistsResult check = mock(DeploymentExistsResult.class);
                    when(check.isExists()).thenReturn(false);
                    when(operations.checkExistence(any(String.class), any(String.class))).thenReturn(check);
                    DeploymentGetResult deploymentGetResult = mock(DeploymentGetResult.class);
                    DeploymentExtended deployment = mock(DeploymentExtended.class);
                    DeploymentPropertiesExtended deplPropsExtended = mock(DeploymentPropertiesExtended.class);
                    when(deplPropsExtended.getProvisioningState()).thenReturn(state);
                    when(deployment.getProperties()).thenReturn(deplPropsExtended);
                    when(deploymentGetResult.getDeployment()).thenReturn(deployment);
                    if (throwResourceClientEx) {
                        when(operations.get(any(String.class), any(String.class))).thenThrow(new IOException("ex"));
                    }
                    when(operations.get(any(String.class), any(String.class))).thenReturn(deploymentGetResult);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                when(resourceManagementClient.getDeploymentsOperations()).thenReturn(operations);
                DeploymentOperationOperations operationsOperations = mock(DeploymentOperationOperations.class);
                try {
                    DeploymentOperationsListResult listResult = mock(DeploymentOperationsListResult.class);
                    ArrayList<DeploymentOperation> deploymentOperations = new ArrayList<>();
                    DeploymentOperation dOp = mock(DeploymentOperation.class);
                    DeploymentOperationProperties mockDepProperties = mock(DeploymentOperationProperties.class);
                    when(mockDepProperties.getProvisioningState()).thenReturn(state);
                    when(dOp.getProperties()).thenReturn(mockDepProperties);
                    deploymentOperations.add(dOp);
                    when(listResult.getOperations()).thenReturn(deploymentOperations);
                    when(operationsOperations.list(any(String.class), any(String.class), any(DeploymentOperationsListParameters.class))).thenReturn(listResult);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                when(resourceManagementClient.getDeploymentOperationsOperations()).thenReturn(operationsOperations);
                ResourceGroupOperations resOps = mock(ResourceGroupOperations.class);
                try {
                    ResourceGroupExistsResult resGroupExistResult = mock(ResourceGroupExistsResult.class);
                    when(resGroupExistResult.isExists()).thenReturn(doesResourceGroupExist);
                    when(resOps.checkExistence(any(String.class))).thenReturn(resGroupExistResult);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                when(resourceManagementClient.getResourceGroupsOperations()).thenReturn(resOps);

                ProviderOperations provOperations = mock(ProviderOperations.class);
                try {
                    ProviderGetResult provGetResult = mock(ProviderGetResult.class);
                    Provider provider = mock(Provider.class);
                    ArrayList<ProviderResourceType> resourceTypes = new ArrayList<>();
                    ProviderResourceType provResType = mock(ProviderResourceType.class);
                    when(provResType.getName()).thenReturn("resourceGroups");
                    if (!throwEx) {
                        resourceTypes.add(provResType);
                    }
                    when(provider.getResourceTypes()).thenReturn(resourceTypes);
                    when(provGetResult.getProvider()).thenReturn(provider);
                    when(provOperations.get(any(String.class))).thenReturn(provGetResult);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                when(resourceManagementClient.getProvidersOperations()).thenReturn(provOperations);
                return resourceManagementClient;
            }

            @Override
            public ComputeManagementClient getComputeClient() {
                computeManagementClient = mock(ComputeManagementClient.class);
                VirtualMachineOperations vmOps = mock(VirtualMachineOperations.class);
                try {
                    VirtualMachineGetResponse responseValue = mock(VirtualMachineGetResponse.class);
                    VirtualMachine vmMock = mock(VirtualMachine.class);
                    when(vmMock.getName()).thenReturn(VMNAME);
                    when(vmMock.getProvisioningState()).thenReturn("Deleted");
                    NetworkProfile networkProfile = mock(NetworkProfile.class);
                    ArrayList<NetworkInterfaceReference> networkInterfaces = new ArrayList<>();
                    NetworkInterfaceReference netInterface = mock(NetworkInterfaceReference.class);
                    when(netInterface.getReferenceUri()).thenReturn("http://reference.uri");
                    networkInterfaces.add(netInterface);
                    when(networkProfile.getNetworkInterfaces()).thenReturn(networkInterfaces);
                    when(vmMock.getNetworkProfile()).thenReturn(networkProfile);
                    VirtualMachineInstanceView vmInstView = mock(VirtualMachineInstanceView.class);
                    ArrayList<InstanceViewStatus> statuses = new ArrayList<>();
                    InstanceViewStatus mockStatus = mock(InstanceViewStatus.class);
                    when(mockStatus.getCode()).thenReturn(powerstate);
                    statuses.add(mockStatus);
                    when(vmInstView.getStatuses()).thenReturn(statuses);
                    when(vmMock.getInstanceView()).thenReturn(vmInstView);
                    when(responseValue.getVirtualMachine()).thenReturn(vmMock);
                    when(vmOps.get(any(String.class), any(String.class))).thenReturn(responseValue);
                    when(vmOps.beginDeleting(any(String.class), any(String.class))).thenReturn(null);
                    VirtualMachineGetResponse vmGetResponse = mock(VirtualMachineGetResponse.class);
                    when(vmGetResponse.getVirtualMachine()).thenReturn(vmMock);
                    when(vmOps.getWithInstanceView(any(String.class), any(String.class))).thenReturn(vmGetResponse);
                    if (throwInstanceStartingEx) {
                        when(vmOps.beginStarting(any(String.class), any(String.class))).thenThrow(new IOException("ex"));
                    }
                    if (throwInstanceStoppingEx) {
                        when(vmOps.beginDeallocating(any(String.class), any(String.class))).thenThrow(new IOException("ex"));
                    }
                    when(computeManagementClient.getVirtualMachinesOperations()).thenReturn(vmOps);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return computeManagementClient;
            }

            @Override
            public StorageManagementClient getStorageClient() {
                StorageManagementClient mockStorageClient = mock(StorageManagementClient.class);
                StorageAccountOperations operations = mock(StorageAccountOperations.class);
                try {
                    StorageAccountListKeysResponse response = mock(StorageAccountListKeysResponse.class);
                    StorageAccountKeys keys = mock(StorageAccountKeys.class);
                    String key = "someKey";
                    when(keys.getKey1()).thenReturn(key);
                    when(response.getStorageAccountKeys()).thenReturn(keys);
                    when(operations.listKeys(any(String.class), any(String.class))).thenReturn(response);
                    OperationResponse result = mock(OperationResponse.class);
                    when(operations.delete(any(String.class), any(String.class))).thenReturn(result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                when(mockStorageClient.getStorageAccountsOperations()).thenReturn(operations);
                return mockStorageClient;
            }

            @Override
            protected Map<String, Object> getParametersMap(String parameters) {
                Map<String, Object> parametersMap = prepareParametersMap();
                return parametersMap;
            }

            @Override
            protected CloudStorageAccount parseConnectionString(String connectionString) throws URISyntaxException, InvalidKeyException {
                if (throwEx) {
                    throw new InvalidKeyException();
                }
                StorageCredentials storageCredentials = mock(StorageCredentials.class);
                when(storageCredentials.getAccountName()).thenReturn("accname");
                return new CloudStorageAccount(storageCredentials);
            }

            @Override
            protected Iterator<CloudBlobContainer> getCloudBlobContainerIterator(CloudBlobClient client) {
                Iterator<CloudBlobContainer> mockIterator = mock(Iterator.class);
                return mockIterator;
            }

            @Override
            public NetworkResourceProviderClient getNetworkClient() {
                networkClient = mock(NetworkResourceProviderClient.class);
                NetworkInterfaceOperations networkInterfacesOperations = mock(NetworkInterfaceOperations.class);
                VirtualNetworkOperations virtualNetworkOperations = mock(VirtualNetworkOperations.class);
                try {
                    NetworkInterfaceGetResponse response = mock(NetworkInterfaceGetResponse.class);
                    Future vnResponse = mock(Future.class);
                    when(vnResponse.isDone()).thenReturn(true);
                    NetworkInterface networkInterface = mock(NetworkInterface.class);
                    ArrayList<NetworkInterfaceIpConfiguration> configurations = new ArrayList<>();
                    NetworkInterfaceIpConfiguration networkInterfaceConfiguration = mock(NetworkInterfaceIpConfiguration.class);
                    if (!skipPublicIp) {
                        ResourceId resourceId = mock(ResourceId.class);
                        when(resourceId.getId()).thenReturn("someIp");
                        when(networkInterfaceConfiguration.getPublicIpAddress()).thenReturn(resourceId);
                    } else if (skipPublicIp && !skipBothIps) {
                        when(networkInterfaceConfiguration.getPrivateIpAddress()).thenReturn("somePrvIp");
                    } else {
                        when(networkInterfaceConfiguration.getPublicIpAddress()).thenReturn(null);
                        when(networkInterfaceConfiguration.getPrivateIpAddress()).thenReturn(null);
                    }
                    configurations.add(networkInterfaceConfiguration);
                    when(networkInterface.getIpConfigurations()).thenReturn(configurations);
                    when(networkInterface.isPrimary()).thenReturn(true);
                    when(response.getNetworkInterface()).thenReturn(networkInterface);
                    when(networkInterfacesOperations.get(any(String.class), any(String.class))).thenReturn(response);
                    when(virtualNetworkOperations.deleteAsync(any(String.class), any(String.class))).thenReturn(vnResponse);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ServiceException e) {
                    e.printStackTrace();
                }
                PublicIpAddressOperations pubIpOperations = mock(PublicIpAddressOperations.class);
                try {
                    PublicIpAddressGetResponse ipOperations = mock(PublicIpAddressGetResponse.class);
                    PublicIpAddress ipAddress = mock(PublicIpAddress.class);
                    when(ipAddress.getIpAddress()).thenReturn("someIp");
                    when(ipOperations.getPublicIpAddress()).thenReturn(ipAddress);
                    when(pubIpOperations.get(any(String.class), any(String.class))).thenReturn(ipOperations);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ServiceException e) {
                    e.printStackTrace();
                }
                when(networkClient.getPublicIpAddressesOperations()).thenReturn(pubIpOperations);
                when(networkClient.getNetworkInterfacesOperations()).thenReturn(networkInterfacesOperations);
                when(networkClient.getVirtualNetworksOperations()).thenReturn(virtualNetworkOperations);
                return networkClient;
            }
        };
    }

    private Map<String, Object> prepareParametersMap() {
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> parametersMap = new HashMap<>();
        Map<String, String> mockMap = new HashMap<>();
        parametersMap.put("network", mockMap);
        parametersMap.put("subnet", mockMap);
        parametersMap.put("imagePublisher", mockMap);
        parametersMap.put("imageOffer", mockMap);
        parametersMap.put("imageSku", mockMap);
        parametersMap.put("vmName", mockMap);
        parametersMap.put("networkInterface", mockMap);
        parametersMap.put("storageAccountName", mockMap);
        parametersMap.put("numberOfInstances", mockMap);
        map.put("parameters", parametersMap);
        return map;
    }
}
