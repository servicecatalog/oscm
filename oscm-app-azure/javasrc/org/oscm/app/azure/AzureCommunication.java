/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *******************************************************************************/
package org.oscm.app.azure;

import com.google.gson.Gson;
import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;
import com.microsoft.azure.management.compute.ComputeManagementClient;
import com.microsoft.azure.management.compute.ComputeManagementService;
import com.microsoft.azure.management.compute.models.InstanceViewStatus;
import com.microsoft.azure.management.compute.models.NetworkInterfaceReference;
import com.microsoft.azure.management.compute.models.VirtualMachine;
import com.microsoft.azure.management.compute.models.VirtualMachineInstanceView;
import com.microsoft.azure.management.network.NetworkResourceProviderClient;
import com.microsoft.azure.management.network.NetworkResourceProviderService;
import com.microsoft.azure.management.network.models.NetworkInterface;
import com.microsoft.azure.management.network.models.NetworkInterfaceIpConfiguration;
import com.microsoft.azure.management.network.models.PublicIpAddress;
import com.microsoft.azure.management.resources.ResourceManagementClient;
import com.microsoft.azure.management.resources.ResourceManagementService;
import com.microsoft.azure.management.resources.models.*;
import com.microsoft.azure.management.storage.StorageManagementClient;
import com.microsoft.azure.management.storage.StorageManagementService;
import com.microsoft.azure.management.storage.models.StorageAccount;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.core.OperationResponse;
import com.microsoft.windowsazure.core.utils.BOMInputStream;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.management.configuration.ManagementConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.oscm.app.azure.controller.PropertyHandler;
import org.oscm.app.azure.data.AccessInfo;
import org.oscm.app.azure.data.AzureAccess;
import org.oscm.app.azure.data.AzureState;
import org.oscm.app.azure.data.PowerState;
import org.oscm.app.azure.exception.AzureClientException;
import org.oscm.app.azure.exception.AzureServiceException;
import org.oscm.app.azure.i18n.Messages;
import org.oscm.app.azure.proxy.ProxyAuthenticator;
import org.oscm.app.azure.proxy.ProxySettings;
import org.oscm.app.v2_0.exceptions.AbortException;
import org.oscm.app.v2_0.exceptions.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.ServiceUnavailableException;
import java.io.IOException;
import java.net.*;
import java.security.InvalidKeyException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AzureCommunication {

    /***
     * Logger.
     */
    private static final Logger logger = LoggerFactory
            .getLogger(AzureCommunication.class);

    /***
     * Azure authority base url.
     */
    private static final String AUTHORITY_BASE_URL = "https://login.microsoftonline.com/";

    /***
     * Azure resource base url.
     */
    private static final String RESOURCE_BASE_URL = "https://management.core.windows.net/";

    /***
     * PropertyHandler.
     */
    private final PropertyHandler ph;

    /***
     * Azure resource management client.
     */
    private final ResourceManagementClient resourceClient;

    /***
     * Azure compute management client.
     */
    private final ComputeManagementClient computeClient;

    /***
     * Azure network resource provider client.
     */
    private final NetworkResourceProviderClient networkClient;

    /***
     * Azure storage resource provider client.
     */
    private final StorageManagementClient storageClient;

    private static boolean exisitingStorageAccount = false;

    /***
     *
     *
     * Connection With Azure and initializing the clients
     *
     */
    public AzureCommunication(PropertyHandler ph) {
        // Proxy Authenticator
        String proxyUser = System.getProperty(ProxySettings.HTTPS_PROXY_USER);
        String proxyPassword = System
                .getProperty(ProxySettings.HTTPS_PROXY_PASSWORD);
        if (StringUtils.isNotEmpty(proxyUser)
                && StringUtils.isNotEmpty(proxyPassword)) {
            Authenticator.setDefault(new ProxyAuthenticator(proxyUser,
                    proxyPassword));
        }

        this.clearProxyProperties();

        this.ph = ph;
        Configuration config = createConfiguration();
        this.resourceClient = ResourceManagementService.create(config);
        this.computeClient = ComputeManagementService.create(config);
        this.networkClient = NetworkResourceProviderService.create(config);
        this.storageClient = StorageManagementService.create(config);
    }

    public PropertyHandler getPh() {
        return this.ph;
    }

    private void clearProxyProperties() {
        List<String> properties = Arrays.asList("http.proxyHost", "http.proxyPort", "https.proxyHost", "https.proxyPort");

        for(String prop : properties) {
           if(System.getProperty(prop) == null || System.getProperty(prop).length() == 0) {
               System.clearProperty(prop);
           }
        }
    }

    /***
     *
     *
     * Creating the configuration for connection
     *
     *
     */
    private Configuration createConfiguration() {
        AuthenticationResult result;
        try {
            if (ph.getClientSecret() == null) {
                result = getAccessTokenFromUserCredentials(ph.getTenantId(), ph.getClientId(), ph.getUserName(),
                        ph.getPassword());
            } else {
                result = getAccessTokenFromClientCredentials(ph.getTenantId(), ph.getClientId(), ph.getClientSecret());
            }
        } catch (MalformedURLException | ServiceUnavailableException | InterruptedException | ExecutionException | AuthenticationException e) {
            throw createAndLogAzureException("Get api access token failed: "
                    + e.getMessage(), e);
        }
        Configuration config;
        try {
            config = ManagementConfiguration.configure(null, new URI(
                    RESOURCE_BASE_URL), ph.getSubscriptionId(), result
                    .getAccessToken());
        } catch (IOException | URISyntaxException e) {
            throw createAndLogAzureException(
                    "Create api client configuration failed: " + e.getMessage(),
                    e);
        }

        // Proxy
        if (ProxySettings.useProxy(RESOURCE_BASE_URL)) {
            config.setProperty(Configuration.PROPERTY_HTTP_PROXY_HOST,
                    System.getProperty(ProxySettings.HTTPS_PROXY_HOST));
            config.setProperty(Configuration.PROPERTY_HTTP_PROXY_PORT,
                    System.getProperty(ProxySettings.HTTPS_PROXY_PORT));
        }
        return config;
    }

    /**
     * Retrieving the access token using user credentials
     */
    private AuthenticationResult getAccessTokenFromUserCredentials(
            String tenantId, String clientId, String apiUserName,
            String apiPassword) throws MalformedURLException,
            InterruptedException, ExecutionException,
            ServiceUnavailableException {
        AuthenticationResult result = null;
        ExecutorService service = Executors.newFixedThreadPool(1);
        try {
            AuthenticationContext context = new AuthenticationContext(
                    AUTHORITY_BASE_URL + tenantId, false, service);
            context.setProxy(ProxySettings.getProxy(AUTHORITY_BASE_URL));
            result = acquireToken(context, clientId, apiUserName, apiPassword);
        } finally {
            service.shutdown();
        }

        if (result == null) {
            throw new ServiceUnavailableException(
                    "Authentication result was null");
        }
        return result;
    }


    /**
     * Retrieving the access token using client credentials
     */
    protected AuthenticationResult getAccessTokenFromClientCredentials(
            String tenantId, String clientId,
            String clientSecret) throws ServiceUnavailableException, AuthenticationException {
        AuthenticationResult result = null;
        ExecutorService service = Executors.newFixedThreadPool(1);
        try {
            AuthenticationContext context = new AuthenticationContext(AUTHORITY_BASE_URL + tenantId, false, service);
            context.setProxy(ProxySettings.getProxy(AUTHORITY_BASE_URL));
            ClientCredential clientCredential = new ClientCredential(clientId, clientSecret);
            result = acquireToken(context, clientCredential);
        } catch (Exception e) {
            logger.error("Exception while authentication: " + e);
            throw new AuthenticationException(e.getMessage(), e.getCause());
        } finally {
            service.shutdown();
        }

        if (result == null) {
            throw new ServiceUnavailableException(
                    "Authentication result was null");
        }
        return result;
    }


    /**
     * Accessing the Template
     */
    private String getTemplate(String url, String operation) throws AbortException {
        logger.debug("Template URL: " + url);
        try {
            URL source = new URL(url);
            BOMInputStream in;
            in = getBOMInputStream(source, url);
            try {
                return getBOMtoString(in);
            } finally {
                IOUtils.closeQuietly(in);
            }
        } catch (Exception e) {
            logger.error("Get template failed. url='" + url + "' operation='"
                    + operation + "'", e);
            throw new AbortException(Messages.getAll("error_" + operation
                    + "_failed_customer"), Messages.getAll("error_" + operation
                    + "_template_read_failed_provider", url, e.getMessage()));
        }
    }

    protected String getBOMtoString(BOMInputStream in) throws IOException {
        return IOUtils.toString(in);
    }

    protected BOMInputStream getBOMInputStream(URL source, String url) throws IOException {
        return new BOMInputStream(source.openConnection(ProxySettings.getProxy(url)).getInputStream());
    }

    /**
     * Updating the template parameters
     */
    @SuppressWarnings("unchecked")
    private String getTemplateParameters(String url, String operation)
            throws AbortException {
        String parameters = getTemplate(url, operation);
        int numberOfInstances = Integer.parseInt(ph.getInstanceCount());
        try {
            Map<String, Object> parametersMap = getParametersMap(parameters);

            //  updateParameter(parametersMap);
            String virtualMachineImageId = getVirtualMachineImageID();

            Map<String, Object> map = (Map<String, Object>) parametersMap.get("parameters");

            Map<String, String> virtualNetworkMap = (Map<String, String>) map.get("network");
            virtualNetworkMap.put("value", ph.getVirtualNetwork());

            Map<String, String> subnetMap = (Map<String, String>) map.get("subnet");
            subnetMap.put("value", ph.getSubnet());

            Map<String, String> imagePublisherMap = (Map<String, String>) map.get("imagePublisher");
            imagePublisherMap.put("value", virtualMachineImageId.split(" ")[0]);

            Map<String, String> imageOfferMap = (Map<String, String>) map.get("imageOffer");
            imageOfferMap.put("value", virtualMachineImageId.split(" ")[1]);

            Map<String, String> windowsOSVersionMap = (Map<String, String>) map.get("imageSku");
            windowsOSVersionMap.put("value", virtualMachineImageId.split(" ")[2]);

            Map<String, String> vmMap = (Map<String, String>) map.get("vmName");
            vmMap.put("value", ph.getVMName());

            Map<String, String> networkInterfaceMap = (Map<String, String>) map.get("networkInterface");
            networkInterfaceMap.put("value", "NIC" + System.currentTimeMillis());

            Map<String, String> storageAccountMap = (Map<String, String>) map.get("storageAccountName");
            storageAccountMap.put("value", ph.getStorageAccount());

            if (numberOfInstances > 1) {
                Map<String, Integer> numberOfInstancesMap = (Map<String, Integer>) map.get("numberOfInstances");
                numberOfInstancesMap.put("value", Integer.valueOf(ph.getInstanceCount()));
            }

            logger.info("value of updated parameter is " + parametersMap.get("parameters"));
            return new Gson().toJson(parametersMap.get("parameters"));
        } catch (Exception e) {
            logger.error("Get template parameters failed. url='" + url
                    + "' operation='" + operation + "'", e);
            throw new AbortException(Messages.getAll("error_" + operation
                    + "_failed_customer"), Messages.getAll("error_" + operation
                    + "_template_read_failed_provider", url, e.getMessage()));
        }
    }

    @SuppressWarnings("unchecked")
    protected Map<String, Object> getParametersMap(String parameters) {
        return new Gson().fromJson(parameters, HashMap.class);
    }

    /**
     * Accessing the virtual machine ID
     */
    private String getVirtualMachineImageID() {

        switch (ph.getVirtualMachineImageID()) {
            case "WindowsServer 2012-R2-Datacenter":
                return "MicrosoftWindowsServer WindowsServer 2012-R2-Datacenter";

            case "WindowsServer 2016-Datacenter":
                return "MicrosoftWindowsServer WindowsServer 2016-Datacenter";

            case "Linux RedHat":
                return "RedHat RHEL 7.3";

            default:
                return "MicrosoftWindowsServer WindowsServer 2012-R2-Datacenter";
        }
    }

    /**
     * Creating custom Azure exceptions
     */
    protected AzureClientException createAndLogAzureException(
            String message, Throwable t) {
        logger.debug("createAndLogAzureException Name: {}, Message: {}", t
                .getClass().getName(), t.getMessage());

        int index = ExceptionUtils.indexOfThrowable(t, ServiceException.class);
        if (index != -1) {
            ServiceException ex = (ServiceException) ExceptionUtils
                    .getThrowableList(t).get(index);
            logger.error("Wrapping AzureServiceException for: ", ex);
            return new AzureServiceException(message, ex);
        }
        logger.error("Wrapping AzureClientException for: ", t);
        return new AzureClientException(message + t.getMessage(), t);
    }

    /**
     * Creating the instance (Azure VMs)
     */
    public void createInstance() {
        logger.debug("AzureCommunication.createInstance entered");
        logger.debug("ResourceGroupName: {}, Region: {}",
                ph.getResourceGroupName(), ph.getRegion());
        try {
            createOrUpdate();
        } catch (IOException | URISyntaxException | ServiceException e) {
            throw createAndLogAzureException("Create a resource group failed: "
                    + e.getMessage(), e);
        }

        try {
            deploymentTemplate("create");
        } catch (Exception e) {
            throw createAndLogAzureException("Template Deployment failed: "
                    + e.getMessage(), e);
        }
    }

    protected void createOrUpdate() throws ServiceException, IOException, URISyntaxException {
        resourceClient.getResourceGroupsOperations().createOrUpdate(ph.getResourceGroupName(), new ResourceGroupExtended(ph.getRegion()));
    }

    /***
     *
     * Updating the instance (new deployment)
     *
     */
    public void updateInstance() throws AbortException {
        logger.debug("AzureCommunication.updateInstance entered");

        deploymentTemplate("update");
    }

    /**
     * Deleting the instance (Azure VMs and resources)
     */
    public void deleteInstance() {
        logger.debug("AzureCommunication.deleteInstance entered");
        List<VirtualMachine> virtualMachines = getVirtualMachines();
        Iterator<VirtualMachine> iterator = virtualMachines.iterator();
        List<String> nicNames = new ArrayList<>();

        try {
            while (iterator.hasNext()) {
                VirtualMachine machine = iterator.next();

                getComputeClient().getVirtualMachinesOperations().beginDeleting(ph.getResourceGroupName(), machine.getName());

                logger.info("Deleting VM-" + machine.getName() + "...");

                List<NetworkInterfaceReference> nics = machine.getNetworkProfile().getNetworkInterfaces();
                for (NetworkInterfaceReference nicReference : nics) {
                    String[] nicURI = nicReference.getReferenceUri().split("/");
                    NetworkInterface nic = getNetworkClient().getNetworkInterfacesOperations()
                            .get(ph.getResourceGroupName(), nicURI[nicURI.length - 1]).getNetworkInterface();
                    nicNames.add(nic.getName());

                }
                if (!iterator.hasNext()) {
                    String state = getComputeClient().getVirtualMachinesOperations().get(ph.getResourceGroupName(), machine.getName()).getVirtualMachine().getProvisioningState();
                    while (!state.equals(ProvisioningState.DELETED)) {
                        try {
                            state = computeClient.getVirtualMachinesOperations().get(ph.getResourceGroupName(), machine.getName()).getVirtualMachine().getProvisioningState();
                            Thread.sleep(5000);
                        } catch (Exception exception) {
                            logger.info("All VMs deleted");
                            break;
                        }
                    }
                }
            }
            Iterator<String> nicIterator = nicNames.iterator();
            while (nicIterator.hasNext()) {
                String nicName = nicIterator.next();
                //Deleting Network Interface
                logger.info("Deleting Network Interface- " + nicName + "...");
                getNetworkClient().getNetworkInterfacesOperations().beginDeleting(ph.getResourceGroupName(), nicName);
                logger.info("Network Interface deleted !!");
            }

            // Delete virtual network
            Future<OperationResponse> response = getNetworkClient().getVirtualNetworksOperations().deleteAsync(ph.getResourceGroupName(), ph.getVirtualNetwork());
            while(!response.isDone()) {
                Thread.sleep(5000);
            }

            //Deleting Storage Account
            if (!exisitingStorageAccount) {
                String key1 = getStorageClient().getStorageAccountsOperations().listKeys(ph.getResourceGroupName(), ph.getStorageAccount()).getStorageAccountKeys().getKey1();
                String connectionString = "DefaultEndpointsProtocol=https;AccountName=" + ph.getStorageAccount() + ";AccountKey=" + key1 + ";EndpointSuffix=core.windows.net";
                try {
                    CloudStorageAccount account = parseConnectionString(connectionString);
                    CloudBlobClient client = account.createCloudBlobClient();
                    Iterator<CloudBlobContainer> containerIterator = getCloudBlobContainerIterator(client);
                    int i = 0;
                    while (containerIterator.hasNext()) {
                        i++;
                        containerIterator.next();
                    }
                    if (i > 1) {  //checking whether the storage account contains more than 1 container
                        deleteVMContainer();
                    } else {
                        getStorageClient().getStorageAccountsOperations().delete(ph.getResourceGroupName(), ph.getStorageAccount());
                        logger.info("Deleting Storage Account-" + ph.getStorageAccount() + "...");
                        logger.info("Storage Account deleted !!");
                    }
                } catch (InvalidKeyException e) {
                    logger.debug("Invalid Key !!");
                    e.printStackTrace();
                }
            } else {
                deleteVMContainer();
            }

            int n = Integer.parseInt(ph.getInstanceCount());
            if (n > 1) {
                //Deleting Availability Set
                String availabilitySetName = ph.getVMName() + "_AvailabilitySet";
                logger.info("Deleting Availability Set-" + availabilitySetName + "...");
                computeClient.getAvailabilitySetsOperations().delete(ph.getResourceGroupName(), availabilitySetName);
                logger.info("Availability Set deleted !!");
            }

            //Deleting Deployment
            logger.info("Deleting deployment-" + ph.getDeploymentName() + "..");
            getResourceClient().getDeploymentsOperations().beginDeleting(ph.getResourceGroupName(), ph.getDeploymentName());
            boolean exist = getResourceClient().getDeploymentsOperations().checkExistence(ph.getResourceGroupName(), ph.getDeploymentName()).isExists();
            while (exist) {
                exist = getResourceClient().getDeploymentsOperations().checkExistence(ph.getResourceGroupName(), ph.getDeploymentName()).isExists();
            }
            logger.info("Deployment deleted !!");
        } catch (IOException | ServiceException | URISyntaxException | ExecutionException | InterruptedException | InvalidKeyException e) {
            throw createAndLogAzureException("Delete Resources failed: ", e);
        }

    }

    protected Iterator<CloudBlobContainer> getCloudBlobContainerIterator(CloudBlobClient client) {
        return client.listContainers().iterator();
    }

    protected CloudStorageAccount parseConnectionString(String connectionString) throws URISyntaxException, InvalidKeyException {
        return CloudStorageAccount.parse(connectionString);
    }

    /***
     *
     * Deleting VM Container
     *
     */
    public void deleteVMContainer() throws IOException, ServiceException, URISyntaxException, InvalidKeyException {
        //Deleting VM Container
        logger.info("Deleting VM Container inside Storage Account: " + ph.getStorageAccount());
        String key1 = getStorageClient().getStorageAccountsOperations().listKeys(ph.getResourceGroupName(), ph.getStorageAccount()).getStorageAccountKeys().getKey1();

        String connectionString = "DefaultEndpointsProtocol=https;AccountName=" + ph.getStorageAccount() + ";AccountKey=" + key1 + ";EndpointSuffix=core.windows.net";
        CloudStorageAccount account = null;
        try {
            account = parseConnectionString(connectionString);
        } catch (InvalidKeyException e) {
            logger.debug("Invalid Key !!");
            throw e;
        }
        CloudBlobClient client = account.createCloudBlobClient();

        Iterator<CloudBlobContainer> containerIterator = getCloudBlobContainerIterator(client);
        boolean isDelete = false;
        while (containerIterator.hasNext()) {
            CloudBlobContainer cloudBlobContainer = containerIterator.next();
            try {
                if (cloudBlobContainer.getName().contains(ph.getVMName().toLowerCase())) {
                    isDelete = cloudBlobContainer.deleteIfExists();
                }
            } catch (StorageException e) {
                logger.debug("Storage Exception while Deleting container !!");
                e.printStackTrace();
            }
        }
        if (isDelete) {
            logger.info("VM Container deleted !!");
        } else {
            logger.error("Something Went wrong while deleting VM Container !!");
        }
    }

    /***
     *
     * Starting the Azure instances (Azure VMs)
     *
     */
    public void startInstance() {
        logger.debug("AzureCommunication.startInstance entered");

        List<VirtualMachine> vms = getVirtualMachines();
        try {
            for (VirtualMachine vm : vms) {
                getComputeClient().getVirtualMachinesOperations().beginStarting(
                        ph.getResourceGroupName(), vm.getName());
            }
        } catch (IOException | ServiceException e) {
            throw createAndLogAzureException("Start virtual machine failed: "
                    + e.getMessage(), e);
        }
    }

    /***
     *
     * Stopping the Azure instances (Azure VMs)
     *
     */
    public void stopInstance() {
        logger.debug("AzureCommunication.stopInstance entered");

        List<VirtualMachine> vms = getVirtualMachines();
        try {
            for (VirtualMachine vm : vms) {
                getComputeClient().getVirtualMachinesOperations().beginDeallocating(
                        ph.getResourceGroupName(), vm.getName());
            }
        } catch (IOException | ServiceException e) {
            throw createAndLogAzureException("Stop virtual machine failed: "
                    + e.getMessage(), e);
        }
    }

    /***
     *
     * Retrieving the Azure state (synchronously)
     *
     */
    public AzureState getDeploymentState() {
        logger.debug("AzureCommunication.getDeploymentState entered: " + ph.getDeploymentName());

        try {
            DeploymentPropertiesExtended deploymentProperties = getResourceClient()
                    .getDeploymentsOperations()
                    .get(ph.getResourceGroupName(), ph.getDeploymentName())
                    .getDeployment().getProperties();
            String provisioningState = deploymentProperties
                    .getProvisioningState();
            logger.debug("DeploymentProperties ProvisioningState: "
                    + provisioningState);

            AzureState azureState = new AzureState(provisioningState);
            if (azureState.isFailed()) {
                List<DeploymentOperation> deploymentOperations = getResourceClient()
                        .getDeploymentOperationsOperations()
                        .list(ph.getResourceGroupName(),
                                ph.getDeploymentName(), null).getOperations();
                for (DeploymentOperation deploymentOperation : deploymentOperations) {
                    DeploymentOperationProperties deploymentOperationProperties = deploymentOperation
                            .getProperties();
                    if (ProvisioningState.FAILED
                            .equals(deploymentOperationProperties
                                    .getProvisioningState())) {
                        String statusCode = deploymentOperationProperties
                                .getStatusCode();
                        logger.debug("DeploymentOperationProperties StatusCode: "
                                + statusCode);

                        azureState.setStatusCode(statusCode);
                        break;
                    }
                }
            }
            return azureState;
        } catch (IOException | URISyntaxException | ServiceException e) {
            throw createAndLogAzureException(
                    "Get deployment provisioning state failed: "
                            + e.getMessage(), e);
        }
    }

    /***
     *
     * Retrieving the deleting state of Azure VMs
     *
     */
    public AzureState getDeletingState() {
        logger.debug("AzureCommunication.getDeletingState() entered");

        if (!isExistsResourceGroup()) {
            return new AzureState(ProvisioningState.DELETED);
        }
        return new AzureState(ProvisioningState.DELETING);
    }

    /***
     *
     * Retrieving the starting state of Azure VM
     *
     */
    public AzureState getStartingState() {
        logger.debug("AzureCommunication.getStartingState() entered");

        Iterator<PowerState> powerStatesItr = getPowerStates().iterator();
        while (powerStatesItr.hasNext()) {
            PowerState powerState = powerStatesItr.next();
            switch (powerState) {
                case RUNNING:
                    // while continue
                    break;

                default:
                    return new AzureState(ProvisioningState.RUNNING);
            }
        }
        return new AzureState(ProvisioningState.SUCCEEDED);
    }

    /***
     *
     * Retrieving the stopping state of Azure VMs
     *
     */
    public AzureState getStoppingState() {
        logger.debug("AzureCommunication.getStoppingState() entered");

        Iterator<PowerState> powerStatesItr = getPowerStates().iterator();
        while (powerStatesItr.hasNext()) {
            PowerState powerState = powerStatesItr.next();
            switch (powerState) {
                case DEALLOCATED:
                    // while continue
                    break;

                default:
                    return new AzureState(ProvisioningState.RUNNING);
            }
        }
        return new AzureState(ProvisioningState.SUCCEEDED);
    }

    /***
     *
     * Retrieving the access info of VMs
     *
     */
    public AccessInfo getAccessInfo(String state) {
        logger.debug("AzureCommunication.getAccessInfo() entered");

        AccessInfo accessInfo = new AccessInfo();
        AzureAccess access = new AzureAccess();
        List<String> accesVMs = new ArrayList<>();
        List<String> accessPublicIPs = new ArrayList<>();
        List<String> accessPrivateIPs = new ArrayList<>();
        List<String> accessStates = new ArrayList<>();

        List<VirtualMachine> vms = getVirtualMachines();
        try {
            for (VirtualMachine vm : vms) {
                logger.debug("VM: " + vm.getName());
                accesVMs.add(vm.getName());
                PowerState powerState = getPowerState(vm);
                if (!PowerState.RUNNING.equals(powerState)) {
                    continue;
                }

                List<NetworkInterfaceReference> nics = vm.getNetworkProfile()
                        .getNetworkInterfaces();
                for (NetworkInterfaceReference nicReference : nics) {
                    String[] nicURI = nicReference.getReferenceUri().split("/");
                    NetworkInterface nic = getNetworkClient()
                            .getNetworkInterfacesOperations()
                            .get(ph.getResourceGroupName(),
                                    nicURI[nicURI.length - 1])
                            .getNetworkInterface();

                    logger.debug("NIC: {}, Is primary: {}", nic.getName(),
                            nic.isPrimary());
                    if (nic.isPrimary() == Boolean.TRUE) {
                        // find public ip address
                        List<NetworkInterfaceIpConfiguration> ips = nic
                                .getIpConfigurations();
                        for (NetworkInterfaceIpConfiguration ipConfiguration : ips) {
                            if (ipConfiguration.getPublicIpAddress() != null) {
                                String[] pipID = ipConfiguration
                                        .getPublicIpAddress().getId()
                                        .split("/");
                                PublicIpAddress pip = getNetworkClient()
                                        .getPublicIpAddressesOperations()
                                        .get(ph.getResourceGroupName(),
                                                pipID[pipID.length - 1])
                                        .getPublicIpAddress();
                                String publicIp = pip.getIpAddress();
                                logger.debug("Public IP address: " + publicIp);


                                accessPublicIPs.add(publicIp);
                                break;
                            } else {
                                logger.info("Public IP address not assigned ");
                            }
                            if (ipConfiguration.getPrivateIpAddress() != null) {
                                logger.debug("Private IP address: " + ipConfiguration.getPrivateIpAddress());
                                accessPrivateIPs.add(ipConfiguration.getPrivateIpAddress());
                            } else {
                                logger.info("Private IP address not assigned ");
                            }
                        }
                        break;
                    }
                }
            }
            accessStates.add(state);

            access.setVmName(accesVMs);
            access.setPrivateIpAddress(accessPrivateIPs);
            access.setPublicIpAddress(accessPublicIPs);
            access.setState(accessStates);
            accessInfo.getAzureAccesses().add(access);
        } catch (IOException | ServiceException e) {
            throw createAndLogAzureException("Getting VM name,Public IP and Private IP Address failed: "
                    + e.getMessage(), e);
        }
        return accessInfo;
    }

    /***
     *
     * Retrieving all the available regions
     *
     */
    public ArrayList<String> getAvailableRegions() {
        List<ProviderResourceType> resourceTypes;
        try {
            resourceTypes = getResourceClient().getProvidersOperations()
                    .get("Microsoft.Resources").getProvider()
                    .getResourceTypes();
        } catch (IOException | URISyntaxException | ServiceException e) {
            throw createAndLogAzureException("Get available regions failed: "
                    + e.getMessage(), e);
        }
        for (ProviderResourceType resourceType : resourceTypes) {
            if ("resourceGroups".equalsIgnoreCase(resourceType.getName())
                    || "subscriptions/resourceGroups"
                    .equalsIgnoreCase(resourceType.getName())) {
                return resourceType.getLocations();
            }
        }
        String massage = "ProviderResourceType 'resourceGroups' and 'subscriptions/resourceGroups' not found";
        throw new AzureClientException("Get Azure regions failed: " + massage);
    }

    /***
     *
     * Deploying the Azure templates
     *
     */
    private void deploymentTemplate(String operation) throws AbortException {
        // set deployment name

        ph.setDeploymentName("Deployment" + System.currentTimeMillis() + ph.getVMName());

        //check whether Storage account exists or a new one before deployement.(For deleting purpose)
        exisitingStorageAccount = checkStorageAccountExists(ph.getStorageAccount());

        // create the template deployment
        DeploymentProperties deploymentProperties = new DeploymentProperties();
        deploymentProperties.setMode(DeploymentMode.Incremental);

        // get template from url
        String template = getTemplate(ph.getTemplateUrl(), operation);
        deploymentProperties.setTemplate(template);

        // get template parameters form url
        if (ph.getTemplateParametersUrl() != null) {
            String templateParameters = getTemplateParameters(ph.getTemplateParametersUrl(), operation);
            deploymentProperties.setParameters(templateParameters);
        }

        // kick off the deployment
        Deployment deployment = new Deployment();
        deployment.setProperties(deploymentProperties);
        try {
            getResourceClient().getDeploymentsOperations().createOrUpdate(ph.getResourceGroupName(), ph.getDeploymentName(), deployment).getDeployment();
        } catch (IOException | URISyntaxException | ServiceException e) {
            throw createAndLogAzureException(
                    "Deployment template failed: " + e.getMessage(), e);
        }
    }

    /***
     *
     * Checking whether the Resource Group exists or not
     *
     */
    private boolean isExistsResourceGroup() {
        try {
            return getResourceClient().getResourceGroupsOperations()
                    .checkExistence(ph.getResourceGroupName()).isExists();
        } catch (IOException | ServiceException e) {
            throw createAndLogAzureException(
                    "Check exists resource group failed: " + e.getMessage(), e);
        }
    }

    /***
     *
     * Retrieving all the Virtual Machines created
     *
     */
    private List<VirtualMachine> getVirtualMachines() {
        try {
            List<VirtualMachine> vmList = new ArrayList<>();
            int n = Integer.parseInt(ph.getInstanceCount());
            if (n > 1) {
                for (int i = 1; i <= n; i++) {
                    vmList.add(getComputeClient().getVirtualMachinesOperations().get(ph.getResourceGroupName(), ph.getVMName() + i).getVirtualMachine());
                }
            } else {
                vmList.add(getComputeClient().getVirtualMachinesOperations().get(ph.getResourceGroupName(), ph.getVMName()).getVirtualMachine());
            }
            logger.info("Size of final VM list : " + vmList.size());
            return vmList;
        } catch (IOException | URISyntaxException | ServiceException e) {
            if(e instanceof ServiceException && ((ServiceException) e).getHttpStatusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                return Collections.emptyList();
            }
            throw createAndLogAzureException("Get virtual machines failed: "
                    + e.getMessage(), e);
        }
    }

    /***
     *
     * Retrieving the Power State of particular Virtual Machine
     *
     */
    private PowerState getPowerState(VirtualMachine vm) {
        VirtualMachineInstanceView instanceView;
        try {
            instanceView = getComputeClient()
                    .getVirtualMachinesOperations()
                    .getWithInstanceView(ph.getResourceGroupName(),
                            vm.getName()).getVirtualMachine().getInstanceView();
        } catch (IOException | URISyntaxException | ServiceException e) {
            throw createAndLogAzureException(
                    "Get virtual machine power state failed: ", e);
        }
        List<InstanceViewStatus> ivss = instanceView.getStatuses();
        for (InstanceViewStatus ivs : ivss) {
            String code[] = ivs.getCode().split("/");
            if ("PowerState".equalsIgnoreCase(code[0]) && (code.length >= 2)) {
                return PowerState.valueOfIgnoreCase(code[1]);
            }
        }
        return PowerState.UNKNOWN;
    }

    /***
     *
     * Retrieving the Power State of all the Virtual Machines
     *
     */
    protected ArrayList<PowerState> getPowerStates() {
        List<VirtualMachine> vms = getVirtualMachines();
        ArrayList<PowerState> powerStates = new ArrayList<>();
        for (VirtualMachine vm : vms) {
            PowerState powerState = getPowerState(vm);
            logger.debug("VM: {}, PowerState: {}", vm.getName(),
                    powerState.name());
            powerStates.add(powerState);
        }
        return powerStates;
    }

    /***
     *
     * Retrieving the Power State of all the Virtual Machines
     *
     */
    public boolean checkStorageAccountExists(String storageAccount) {
        try {
            Iterator<StorageAccount> iterator = getStorageAccounts();
            while (iterator.hasNext()) {
                StorageAccount strAcc = iterator.next();
                if (strAcc.getName().equals(storageAccount)) {
                    logger.info("Existing Storage account: true");
                    return true;
                }
            }
        } catch (IOException | ServiceException | URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        logger.info("Existing Storage account: false");
        return false;
    }

    protected Iterator<StorageAccount> getStorageAccounts() throws ServiceException, IOException, URISyntaxException {
        return storageClient.getStorageAccountsOperations().listByResourceGroup(ph.getResourceGroupName()).getStorageAccounts().iterator();
    }

    protected AuthenticationResult acquireToken(AuthenticationContext context, String clientId,
                                                String apiUserName, String apiPassword)
            throws ExecutionException, InterruptedException {
        return context.acquireToken(RESOURCE_BASE_URL, clientId,
                apiUserName, apiPassword, null).get();
    }

    protected AuthenticationResult acquireToken(AuthenticationContext context,
                                                ClientCredential clientCredential) throws ExecutionException, InterruptedException {
        Future<AuthenticationResult> future = context.acquireToken(RESOURCE_BASE_URL, clientCredential, null);
        return future.get();
    }

    public ResourceManagementClient getResourceClient() {
        return resourceClient;
    }

    public ComputeManagementClient getComputeClient() {
        return computeClient;
    }

    public NetworkResourceProviderClient getNetworkClient() {
        return networkClient;
    }

    public StorageManagementClient getStorageClient() {
        return storageClient;
    }
}
