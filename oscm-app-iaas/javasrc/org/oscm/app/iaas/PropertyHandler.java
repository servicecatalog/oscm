/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.app.iaas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.LexerNoViableAltException;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;
import org.oscm.app.iaas.data.FWPolicy;
import org.oscm.app.iaas.data.FlowState;
import org.oscm.app.iaas.data.IaasContext;
import org.oscm.app.iaas.data.Operation;
import org.oscm.app.iaas.data.VServerConfiguration;
import org.oscm.app.iaas.fwpolicy.FWPolicyErrorStrategy;
import org.oscm.app.iaas.fwpolicy.FWPolicyLexer;
import org.oscm.app.iaas.fwpolicy.FWPolicyParser;
import org.oscm.app.iaas.fwpolicy.FWPolicyParser.PoliciesContext;
import org.oscm.app.iaas.i18n.Messages;
import org.oscm.app.v2_0.data.PasswordAuthentication;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.data.Setting;
import org.oscm.app.v2_0.exceptions.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handling of service parameters and properties.
 */
public class PropertyHandler {

    /**
     * The URL and credentials to be used for connections to the IaaS API.
     */
    public static final String IAAS_API_URI = "IAAS_API_URI";
    public static final String IAAS_API_USER = "IAAS_API_USER";
    public static final String IAAS_API_PWD = "IAAS_API_PWD";
    public static final String IAAS_API_TENANT = "IAAS_API_TENANT";
    public static final String IAAS_API_LOCALE = "IAAS_API_LOCALE";
    public static final String IAAS_API_KEYSTORE_TYPE = "IAAS_API_KEYSTORE_TYPE";
    public static final String IAAS_API_KEYSTORE_PASS = "IAAS_API_KEYSTORE_PASS";
    public static final String IAAS_API_KEYSTORE = "IAAS_API_KEYSTORE";

    /**
     * If this value is set, the controller execution will not continue until
     * System.currentTimeMillis() returns a larger value.
     */
    public static final String SUSPEND_UNTIL = "SUSPEND_UNTIL";

    /**
     * The vsys id param key.
     */
    public static final String VSYS_ID = "VSYS_ID";

    /**
     * The current status of instance creation.
     */
    public static final String API_STATUS = "API_STATUS";

    /**
     * The custom name of the new instance
     */
    public static final String INSTANCENAME_CUSTOM = "INSTANCENAME";

    /**
     * The predefined prefix of the new instance
     */
    public static final String INSTANCENAME_PREFIX = "INSTANCENAME_PREFIX";

    /**
     * The regular expression for validating the new instance name
     */
    public static final String INSTANCENAME_PATTERN = "INSTANCENAME_PATTERN";

    /**
     * Describes the current overall operation (e.g. creation, deletion, ...).
     * deployment for the customer.
     */
    public static final String OPERATION = "OPERATION";

    /**
     * The vserver id param key.
     */
    public static final String VSERVER_ID = "VSERVER_ID";

    /**
     * The diskimage id param key.
     */
    public static final String DISKIMG_ID = "DISKIMG_ID";

    /**
     * The vserver type param key.
     */
    public static final String VSERVER_TYPE = "VSERVER_TYPE";

    /**
     * The network id param key.
     */
    public static final String NETWORK_ID = "NETWORK_ID";

    /**
     * The host pool param key.
     */
    public static final String VM_POOL = "VM_POOL";

    /**
     * The storage pool param key.
     */
    public static final String STORAGE_POOL = "STORAGE_POOL";

    /**
     * The VDISK name param key.
     */
    public static final String VDISK_NAME = "VDISK_NAME";

    /**
     * The VDISK id param key.
     */
    public static final String VDISK_ID = "VDISK_ID";

    /**
     * The VDISK size param key.
     */
    public static final String VDISK_SIZE = "VDISK_SIZE";

    /**
     * Defines whether manual steps are required before activation the
     * deployment for the customer.
     */
    public static final String MAIL_FOR_COMPLETION = "MAIL_FOR_COMPLETION";

    /**
     * If defined the successful modification of the system will be confirmed to
     * the customer using this mail address.
     */
    public static final String MAIL_FOR_NOTIFICATION = "MAIL_FOR_NOTIFICATION";

    /**
     * The parameter key for the user name used to call web services in BES.
     */
    public static final String BSS_USER = "APP_BSS_USER";

    /**
     * The parameter key for the user's password.
     */
    public static final String BSS_USER_PWD = "APP_BSS_USER_PWD";

    /**
     * The number of CPUs.
     */
    public static final String COUNT_CPU = "COUNT_CPU";

    /**
     * The virtual system template id required to create virtual system.
     */
    public static final String SYSTEM_TEMPLATE_ID = "SYSTEM_TEMPLATE_ID";

    /**
     * The master virtual server disk image id.
     */
    public static final String MASTER_TEMPLATE_ID = "MASTER_TEMPLATE_ID";

    /**
     * The master virtual server disk image id.
     */
    public static final String SLAVE_TEMPLATE_ID = "SLAVE_TEMPLATE_ID";

    /**
     * The cluster size of virtual servers.
     */
    public static final String CLUSTER_SIZE = "CLUSTER_SIZE";

    /**
     * Prefix for additional defined VServers
     */
    public static final String ADD_VSERVER_PREFIX = "VSERVER_#";

    /**
     * Firewall configuration string
     */
    public static final String FIREWALL_CONFIG = "FIREWALL_CONFIG";

    /**
     * Prefix for additional defined firewall configurations
     */
    public static final String ADD_FIREWALL_CONFIG_PREFIX = "FIREWALL_CONFIG_#";

    /**
     * Prefix for defined firewall configuration variables
     */
    public static final String FIREWALL_VARIABLE_PREFIX = "FIREWALL_";

    /**
     * The URL of an (optional) admin agent callable by REST calls.
     */
    public static final String ADMIN_REST_URL = "ADMIN_REST_URL";

    /**
     * The set of public IP addresses that are managed by the controller.
     */
    public static final String MANAGED_PUBLIC_IPS = "MANAGED_PUBLIC_IPS";

    /**
     * The set of firewall rules (IDs) that are managed by the controller.
     */
    public static final String MANAGED_FW_POLICIES = "MANAGED_FW_POLICIES";

    /**
     * If set to true, the provisioning processors will not use locking to
     * serialize provisioning calls. The usage of this parameter is intended for
     * testing purposes only and is not officially supported.
     */
    public static final String ENABLE_PARALLEL_PROVISIONING = "ENABLE_PARALLEL_PROVISIONING";

    public static final String CONTROLLER_WAIT_TIME = "CONTROLLER_WAIT_TIME";
    /**
     * The set of VServers (IDs) that need to be started (again) after current
     * process is completed.
     */
    public static final String VSERVERS_TO_BE_STARTED = "VSERVERS_TO_BE_STARTED";

    /**
     * The set of VServers (IDs) that were already processed in the recent
     * creation/modification round.
     */
    public static final String VSERVERS_TOUCHED = "VSERVERS_TOUCHED";

    private final ProvisioningSettings settings;
    private final PropertyReader props;
    private IaasContext iaasContext;

    private static final Logger logger = LoggerFactory
            .getLogger(PropertyHandler.class);

    /**
     * Constructor
     * 
     * @throws ConfigurationException
     */
    public PropertyHandler(ProvisioningSettings settings)
            throws ConfigurationException {
        this(settings, new PropertyReader(settings.getParameters(), null));
    }

    /**
     * Internal constructor (for sub entities)
     * 
     * @throws ConfigurationException
     */
    PropertyHandler(ProvisioningSettings settings, PropertyReader reader)
            throws ConfigurationException {
        this.settings = settings;
        this.props = reader;
        validateClusterDefinition();
    }

    /**
     * Returns the vdisk size.
     * 
     * @return the vdisk size
     */
    public String getVDiskSize() {
        return props.getProperty(VDISK_SIZE);
    }

    /**
     * Update the vdisk size.
     */
    public void setVDiskSize(String newState) {
        props.setProperty(VDISK_SIZE, newState);
    }

    /**
     * Update the vdisk name.
     */
    public void setVDiskName(String newState) {
        props.setProperty(VDISK_NAME, newState);
    }

    /**
     * Returns the vdisk id.
     * 
     * @return the vdisk id.
     */
    public String getVDiskId() {
        return props.getValidatedProperty(VDISK_ID);
    }

    /**
     * Update the vdisk id.
     */
    public void setVDiskId(String newState) {
        props.setProperty(VDISK_ID, newState);
    }

    /**
     * Returns the URL of the IaaS-API of the back-end system.
     * 
     * @return the URL
     */
    public String getURL() {
        return getValidatedConfigurationProperty(IAAS_API_URI);
    }

    /**
     * Returns the user name to be used for the IaaS-API of the back-end system.
     * 
     * @return the user name
     */
    public String getUser() {
        return getValidatedConfigurationProperty(IAAS_API_USER);
    }

    /**
     * Returns the password to be used for the IaaS-API of the back-end system.
     * 
     * @return the password
     */
    public String getPassword() {
        return getValidatedConfigurationProperty(IAAS_API_PWD);
    }

    /**
     * Returns users tenant ID within the back-end system.
     * 
     * @return the tenant ID
     */
    public String getTenantId() {
        return getValidatedConfigurationProperty(IAAS_API_TENANT);
    }

    /**
     * Returns the locale to be used for calling the IaaS API.
     * 
     * @return the API locale
     */
    public String getAPILocale() {
        return getValidatedConfigurationProperty(IAAS_API_LOCALE);
    }

    /**
     * Returns the locale set as default for the customer organization.
     * 
     * @return the customer locale
     */
    public String getCustomerLocale() {
        String locale = settings.getLocale();
        if (isNullOrEmpty(locale)) {
            locale = "en";
        }
        return locale;
    }

    /**
     * Returns the vsys id.
     * 
     * @return the vsys id
     */
    public String getVsysId() {
        return getValue(VSYS_ID, settings.getParameters());
    }

    /**
     * Returns the vserver id.
     * 
     * @return the vserver id
     */
    public String getVserverId() {
        return props.getValidatedProperty(VSERVER_ID);
    }

    /**
     * Returns the vserver id if set.
     * 
     * @return the vserver id
     */
    public String getVserverIdIfPresent() {
        return props.getProperty(VSERVER_ID);
    }

    /**
     * Update the vserver id.
     */
    public void setVserverId(String serverId) {
        props.setProperty(VSERVER_ID, serverId);
    }

    /**
     * Returns the diskimage id.
     * 
     * @return the diskimage id
     */
    public String getDiskImageId() {
        return props.getValidatedProperty(DISKIMG_ID);
    }

    /**
     * Sets the diskimage id.
     */
    protected void setDiskImageId(String value) {
        props.setProperty(DISKIMG_ID, value);
    }

    /**
     * Returns the vserver type.
     * 
     * @return the vserver type
     */
    public String getVserverType() {
        return props.getValidatedProperty(VSERVER_TYPE);
    }

    /**
     * Sets the vserver type.
     */
    protected void setVserverType(String value) {
        props.setProperty(VSERVER_TYPE, value);
    }

    /**
     * Returns a list of additionally defined virtual servers.
     * 
     * @return a list of property handlers reflecting each defined additional
     *         LServer
     * @throws ConfigurationException
     */
    public SubPropertyHandler[] getVserverList() throws ConfigurationException {
        List<SubPropertyHandler> vsl = new ArrayList<>();
        for (int i = 1; i <= 999; i++) {
            String propPrefix = ADD_VSERVER_PREFIX.replace("#",
                    Integer.toString(i));
            if (props.getProperty(propPrefix) != null) {
                // Next defined VSERVER found?
                boolean isEnabled = Boolean.parseBoolean(props
                        .getProperty(propPrefix));
                vsl.add(new SubPropertyHandler(settings, propPrefix + "_",
                        isEnabled, getIaasContext()));
            } else {
                // No more VSERVER definition found! => exit loop
                break;
            }
        }
        return vsl.toArray(new SubPropertyHandler[vsl.size()]);
    }

    public SubPropertyHandler getTemporaryVserver(VServerConfiguration server)
            throws ConfigurationException {
        String propPrefix = ADD_VSERVER_PREFIX.replace("#", "X");
        props.setProperty(propPrefix, "TRUE");
        SubPropertyHandler subHandler = new SubPropertyHandler(settings,
                propPrefix + "_", true, getIaasContext());
        if (server != null) {
            subHandler.setVserverId(server.getServerId());
            subHandler.setDiskImageId(server.getDiskImageId());
            subHandler.setVserverType(server.getServerType());
            subHandler.setNetworkId(server.getNetworkId());
        }
        return subHandler;
    }

    public void removeTemporaryVserver() {
        String propPrefix = ADD_VSERVER_PREFIX.replace("#", "X");
        for (String key : props.getPropertyKeys(propPrefix)) {
            props.clearProperty(key);
        }
    }

    /**
     * Returns the network ID.
     * 
     * @return the network id
     */
    public String getNetworkId() {
        return props.getProperty(NETWORK_ID);
    }

    /**
     * Sets the network ID.
     * 
     * @param networkId
     *            the networkId
     */
    protected void setNetworkId(String networkId) {
        props.setProperty(NETWORK_ID, networkId);
    }

    /**
     * Returns the VM pool parameter.
     * 
     * @return the VM pool or <code>null</code> if none is set.
     */
    public String getVMPool() {
        return props.getProperty(VM_POOL);
    }

    /**
     * Returns the storage pool parameter.
     * 
     * @return the storage pool or <code>null</code> if none is set.
     */
    public String getStoragePool() {
        return props.getProperty(STORAGE_POOL);
    }

    /**
     * Returns the full name of the new instance (including the prefix).
     * 
     * @return the full name of the new instance
     */
    public String getInstanceName() {
        StringBuffer b = new StringBuffer();
        String prefix = getInstanceNamePrefix();
        if (prefix != null)
            b.append(prefix);
        b.append(getInstanceNameIfExists());
        return b.toString();
    }

    /**
     * Returns the custom name of the new instance.
     * 
     * @return the name of the custom defined instance name
     */
    public String getInstanceNameCustom() {
        return props.getValidatedProperty(INSTANCENAME_CUSTOM);
    }

    /**
     * Returns the custom name of the new instance, if not exists, return empty
     * string.
     * 
     * @return the name of the custom defined instance name
     */
    public String getInstanceNameIfExists() {
        String instanceName = props.getProperty(INSTANCENAME_CUSTOM);
        if (instanceName == null || instanceName.trim().length() == 0) {
            return "";
        }
        return instanceName;
    }

    /**
     * Returns the predefined prefix for the instance name or NULL if not
     * defined.
     * 
     * @return the name of the instance name prefix
     */
    public String getInstanceNamePrefix() {
        return props.getProperty(INSTANCENAME_PREFIX);
    }

    /**
     * Returns the regular expression for validating the instance name.
     * 
     * @return the regular expression for the instance name
     */
    public String getInstanceNamePattern() {
        return props.getProperty(INSTANCENAME_PATTERN);
    }

    /**
     * Returns the current state of the update process
     */
    public FlowState getState() {
        String status = props.getProperty(API_STATUS);
        return (status != null) ? FlowState.valueOf(status) : FlowState.FAILED;
    }

    /**
     * Updates the current state of the creation process.
     */
    public void setState(FlowState newState) {
        props.setProperty(API_STATUS, newState.toString());
    }

    /**
     * Returns the current operation.
     */
    public Operation getOperation() {
        String operation = props.getProperty(OPERATION);
        return (operation != null) ? Operation.valueOf(operation)
                : Operation.UNKNOWN;
    }

    /**
     * Updates the current operation.
     */
    public void setOperation(Operation newState) {
        props.setProperty(OPERATION, newState.toString());
    }

    /**
     * Returns the mail address to be used for completion events (provisioned,
     * deleted). If not set, no events are required.
     * 
     * @return the mail address or <code>null</code> if no events are required
     */
    public String getMailForCompletion() {
        return nullWhenEmpty(props.getProperty(MAIL_FOR_COMPLETION));
    }

    /**
     * Returns the mail address to be used for notifying the customer about
     * successful modification of the instance. If not set, no notification is
     * requested.
     * 
     * @return the mail address or <code>null</code> if no notification is
     *         required
     */
    public String getMailForNotification() {
        return nullWhenEmpty(props.getProperty(MAIL_FOR_NOTIFICATION));
    }

    /**
     * Returns printable configuration
     */
    public String getConfigurationAsString() {
        StringBuffer details = new StringBuffer();
        details.append("\t\r\nServerType: ");
        details.append(getVserverType());
        details.append("\t\r\nVServer Name: ");
        details.append(getInstanceName());
        details.append("\t\r\nVSYS ID: ");
        details.append(getVsysId());
        details.append("\t\r\nVServer Image Id: ");
        details.append(getDiskImageId());
        details.append("\t\r\n");
        return details.toString();
    }

    /**
     * Returns the instance or controller specific technology manager
     * authentication.
     */
    public PasswordAuthentication getTPAuthentication() {
        return settings.getAuthentication();
    }

    /**
     * Returns the full name of the new virtual disk (including the prefix).
     * 
     * @return the full name of the new virtual disk
     */
    public String getVDiskName() {
        StringBuffer b = new StringBuffer();
        String prefix = getInstanceName();
        if (prefix != null)
            b.append(prefix);
        b.append(getVDiskNameCustom());
        return b.toString();
    }

    /**
     * Returns the custom name of the virtual disk.
     * 
     * @return the name of the custom defined virtual disk
     */
    public String getVDiskNameCustom() {
        return props.getProperty(VDISK_NAME);
    }

    /**
     * Returns the number of CPUs.
     * 
     * @return the number of CPUs
     */
    public String getCountCPU() {
        return props.getProperty(COUNT_CPU);
    }

    /**
     * Sets the number of CPUs.
     */
    public void setCountCPU(String value) {
        props.setProperty(COUNT_CPU, value);
    }

    /**
     * Returns the master disk image id to be used in creation of virtual
     * server.
     * 
     * @return the masterTemplateId
     */
    public String getMasterTemplateId() {
        return props.getProperty(MASTER_TEMPLATE_ID);

    }

    /**
     * Returns the cluster size of virtual servers.
     * 
     * @return the clusterSize
     */
    public String getClusterSize() {
        return props.getProperty(CLUSTER_SIZE);

    }

    /**
     * Returns the slave disk image id to be used in creation of virtual server.
     * 
     * @return the slaveDTemplateId
     */
    public String getSlaveTemplateId() {
        return props.getProperty(SLAVE_TEMPLATE_ID);

    }

    public void setVsysId(String virtualSystemId) {
        props.setProperty(VSYS_ID, virtualSystemId);
    }

    /**
     * Returns the ID of the system template to be used in case of virtual
     * system provisioning.
     * 
     * @return the system template ID
     */
    public String getSystemTemplateId() {
        return props.getProperty(SYSTEM_TEMPLATE_ID);
    }

    /**
     * Returns the URL of an (optional) admin agent.
     * 
     * @return the admin agent URL
     */
    public String getAdminRestURL() {
        return nullWhenEmpty(props.getProperty(ADMIN_REST_URL));
    }

    /**
     * Returns the firewall configuration settings as string.
     * <p>
     * Variables within the configuration string will be replaced by using the
     * value of specified firewall setting:<br>
     * FIREWALL_CONFIG=DMZ()>INET(<b>SIPVAR</b>:25)<br>
     * FIREWALL_<b>SIPVAR</b>=85.123.112.0/24<br>
     * <p>
     * If the configuration string exceeds the max. length of 256 characters,
     * additional configurations can be specified by using an increasing
     * sequence of numbers as index:<br>
     * FIREWALL_CONFIG=...<br>
     * FIREWALL_CONFIG_<b>1</b>=...<br>
     * FIREWALL_CONFIG_<b>2</b>=...<br>
     * FIREWALL_CONFIG_<b>#</b>=...<br>
     * <p>
     * 
     * @return the firewall configuration
     */
    public String getFirewallConfiguration() {
        // Use String instead of StringBuffer because we need "replace" methods
        String fw = props.getProperty(FIREWALL_CONFIG);
        if (fw != null && fw.trim().length() != 0) {

            // Append multilines
            for (int i = 1; i < 999; i++) {
                String propPrefix = ADD_FIREWALL_CONFIG_PREFIX.replace("#",
                        Integer.toString(i));
                if (props.getProperty(propPrefix) == null
                        || props.getProperty(propPrefix).trim().length() == 0) {
                    // No more firewall definitions found! => exit loop
                    break;
                }

                // Add next found definition
                if (!fw.endsWith(";")) {
                    fw += ";";
                }
                fw += props.getProperty(propPrefix);
            }

            // Replace variables
            Pattern pattern = Pattern.compile("\\$\\{([a-z0-9]*)\\}",
                    Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(fw);
            while (matcher.find()) {
                String varName = matcher.group(1);
                String varValue = getPropertyValue(FIREWALL_VARIABLE_PREFIX
                        + varName);
                if (varValue == null) {
                    varValue = "";
                }
                fw = fw.replaceAll("\\$\\{" + varName + "\\}", varValue);
            }
        }

        return fw;
    }

    /**
     * Returns the firewall configuration as set of policies.
     * 
     * @return the set of policies - may be empty but not <code>null</code>
     */
    public Set<FWPolicy> getFirewallPolicies() throws Exception {

        String config = getFirewallConfiguration();
        if (notNullNorEmpty(config)) {
            logger.debug("Given firewall configuration: " + config);

            ANTLRInputStream input = new ANTLRInputStream(config);
            FWPolicyLexer lexer = null;
            try {
                lexer = new FWPolicyLexer(input) {
                    @Override
                    public void recover(LexerNoViableAltException e) {
                        Interval interval = new Interval(e.getStartIndex(), e
                                .getInputStream().size() - 1);

                        String policy = e.getInputStream().getText(interval);
                        String message = Messages.get(
                                Messages.DEFAULT_LOCALE,
                                "error_invalid_firewallconfig_character",
                                new Object[] {
                                        Integer.valueOf(e.getStartIndex()),
                                        policy });

                        throw new RuntimeException(message);
                    }
                };
            } catch (RuntimeException rex) {
                throw new Exception(rex);
            }
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            FWPolicyParser parser = new FWPolicyParser(tokens);
            parser.setBuildParseTree(true);
            parser.setErrorHandler(new FWPolicyErrorStrategy());
            ParseTree tree = parser.policies();
            PoliciesContext context = (PoliciesContext) tree.getPayload();
            return Collections.unmodifiableSet(new HashSet<>(context.pList));
        }
        // return unmodifiable to signal the the result is not backed by the
        // handler
        return Collections.unmodifiableSet(new HashSet<FWPolicy>());
    }

    /**
     * Returns the set of public IP addresses that are managed by the
     * controller.
     * 
     * @return the set of IPs (may be empty but not <code>null</code>)
     */
    public Set<String> getManagedPublicIPs() {
        String value = props.getProperty(MANAGED_PUBLIC_IPS);
        if (value != null) {
            return Collections.unmodifiableSet(new HashSet<>(Arrays
                    .asList(value.split(","))));
        }
        return Collections.unmodifiableSet(new HashSet<String>());
    }

    /**
     * Stores the set of public IP addresses that are managed by the controller.
     * 
     * @param ips
     *            the set of IPs
     */
    public void setManagedPublicIPs(Set<String> ips) {
        props.setProperty(MANAGED_PUBLIC_IPS, getCSVString(ips));
    }

    /**
     * Returns the IDs of the firewall policies that are managed by the
     * controller.
     * 
     * @return the set of rule IDs (may be empty but not <code>null</code>)
     */
    public Set<String> getManagedFirewallPolicies() {
        String value = props.getProperty(MANAGED_FW_POLICIES);
        if (value != null) {
            return Collections.unmodifiableSet(new HashSet<>(Arrays
                    .asList(value.split(","))));
        }
        return Collections.unmodifiableSet(new HashSet<String>());
    }

    /**
     * Stores the IDs of the firewall policies that are managed by the
     * controller.
     * 
     * @param ruleIDs
     *            the set of IDs of the policies
     */
    public void setManagedFirewallPolicies(Set<String> ruleIDs) {
        props.setProperty(MANAGED_FW_POLICIES, getCSVString(ruleIDs));
    }

    /**
     * Returns a printable virtual system configuration string.
     */
    public String getVSystemConfigurationAsString() {
        StringBuffer details = new StringBuffer();
        details.append("\t\r\nVirtualSystemTemplate: ");
        details.append(getSystemTemplateId());
        details.append("\t\r\nVSYS ID: ");
        details.append(getVsysId());
        if (isClusterDefined()) {
            details.append("\r\nClustering defined:");
            details.append("\r\n\tVirtualServerMasterTemplate: ");
            details.append(getMasterTemplateId());
            details.append("\r\n\tVirtualServerSlaveTemplate: ");
            details.append(getSlaveTemplateId());
            details.append("\r\n\tClusterSize: ");
            details.append(getClusterSize());
        } else {
            details.append("\t\r\nNo clustering defined");
        }
        details.append("\t\r\n");
        return details.toString();
    }

    /**
     * Sets a suspend time to this instance. Instance processing will be paused
     * until this time is over.
     * 
     * @param seconds
     *            the time to sleep in seconds
     */
    public void suspendProcessInstanceFor(long seconds) {
        props.setProperty(SUSPEND_UNTIL, ""
                + (System.currentTimeMillis() + (1000L * seconds)));
    }

    public boolean isVirtualSystemProvisioning() {
        return (getSystemTemplateId() != null)
                && (getSystemTemplateId().length() != 0);
    }

    public boolean isVirtualServerProvisioning() {
        return !isVirtualSystemProvisioning();
    }

    /**
     * Checks whether the controller should pause processing the instance due to
     * a present suspend time frame.
     * 
     * @return <code>true<code>
     */
    public boolean isInstanceSuspended() {
        String suspendUntil = props.getProperty(SUSPEND_UNTIL);
        if (notNullNorEmpty(suspendUntil)) {
            try {
                long untilMillis = Long.parseLong(suspendUntil);
                if (untilMillis > System.currentTimeMillis()) {
                    return true;
                }
                props.setProperty(SUSPEND_UNTIL, "");
            } catch (NumberFormatException e) {
                logger.debug("Invalid value for SUSPEND_UNTIL: " + suspendUntil);
                props.setProperty(SUSPEND_UNTIL, "");
            }
        }
        return false;
    }

    /**
     * Reads the requested property from the available parameters. If no value
     * can be found, a RuntimeException will be thrown.
     * 
     * @param key
     *            The key to retrieve the setting for
     * @return the parameter value corresponding to the provided key
     */
    private String getValidatedConfigurationProperty(String key) {
        String value = getValue(key, settings.getConfigSettings());
        if (value == null) {
            String message = String.format("No value set for property '%s'",
                    key);
            logger.error(message);
            throw new RuntimeException(message);
        }
        return value;
    }

    public Set<String> getVserversToBeStarted() {
        String value = props.getProperty(VSERVERS_TO_BE_STARTED);
        if (value != null) {
            HashSet<String> set = new HashSet<>(Arrays.asList(value.split(",")));
            set.remove("");
            return Collections.unmodifiableSet(set);
        }
        return Collections.unmodifiableSet(new HashSet<String>());
    }

    public void addVserverToBeStarted(String vServerId) {
        if (vServerId != null) {
            String value = props.getProperty(VSERVERS_TO_BE_STARTED);
            HashSet<String> result = new HashSet<>();
            if (value != null) {
                result.addAll((Arrays.asList(value.split(","))));
            }
            result.add(vServerId.trim());
            props.setProperty(VSERVERS_TO_BE_STARTED, getCSVString(result));
        }
    }

    public void setVserverToBeStarted(List<String> vServerIds) {
        if (vServerIds != null) {
            HashSet<String> result = new HashSet<>();
            for (String id : vServerIds) {
                result.add(id.trim());
            }
            props.setProperty(VSERVERS_TO_BE_STARTED, getCSVString(result));
        }
    }

    public void removeVserverToBeStarted(String vServerId) {
        if (vServerId != null) {
            String value = props.getProperty(VSERVERS_TO_BE_STARTED);
            HashSet<String> result = new HashSet<>();
            if (value != null) {
                result.addAll((Arrays.asList(value.split(","))));
                result.remove(vServerId.trim());
                props.setProperty(VSERVERS_TO_BE_STARTED, getCSVString(result));
            }
        }
    }

    /**
     * Returns the set of VServers (IDs) that were already processed in the
     * recent creation/modification round.
     */
    public Set<String> getVserversTouched() {
        String value = props.getProperty(VSERVERS_TOUCHED);
        if (value != null) {
            return Collections.unmodifiableSet(new HashSet<>(Arrays
                    .asList(value.split(","))));
        }
        return Collections.unmodifiableSet(new HashSet<String>());
    }

    /**
     * Adds a server to the set of VServers (IDs) that were already processed in
     * the recent creation/modification round.
     */
    public void addTouchedVserver(String vServerId) {
        if (vServerId != null) {
            String value = props.getProperty(VSERVERS_TOUCHED);
            HashSet<String> result = new HashSet<>();
            if (value != null) {
                result.addAll((Arrays.asList(value.split(","))));
            }
            result.add(vServerId.trim());
            props.setProperty(VSERVERS_TOUCHED, getCSVString(result));
        }
    }

    /**
     * Clears set of VServers (IDs) that were already processed in the recent
     * creation/modification round. This method should be called when the
     * controller has finished the current operation.
     */
    public void resetTouchedVservers() {
        props.setProperty(VSERVERS_TOUCHED, "");
    }

    /**
     * Returns whether all necessary settings for clustering (horizontal
     * scaling) are defined.
     * 
     * @return <code>true</code> if the necessary configuration parameters are
     *         present
     */
    public boolean isClusterDefined() {
        return notNullNorEmpty(getClusterSize())
                && notNullNorEmpty(getMasterTemplateId())
                && notNullNorEmpty(getSlaveTemplateId());
    }

    /*
     * Internal checking method to detect incomplete cluster definition
     */
    private void validateClusterDefinition() throws ConfigurationException {
        if (isClusterDefined()) {
            return;
        }
        if (notNullNorEmpty(getClusterSize())
                || notNullNorEmpty(getMasterTemplateId())
                || notNullNorEmpty(getSlaveTemplateId())) {
            throw new ConfigurationException(Messages.getAll(
                    "error_provider_clusterdef_incomplete", getClusterSize(),
                    getMasterTemplateId(), getSlaveTemplateId()));
        }
    }

    private boolean notNullNorEmpty(String value) {
        return value != null && value.trim().length() > 0;
    }

    private boolean isNullOrEmpty(String value) {
        return value == null || value.trim().length() == 0;
    }

    private String nullWhenEmpty(String value) {
        return isNullOrEmpty(value) ? null : value;
    }

    /**
     * Returns the property value of the specified property.
     * 
     * @return the property value or NULL of not defined
     */
    public String getPropertyValue(String key) {
        return props.getProperty(key);
    }

    /**
     * Returns the current IaaS context object.
     * 
     * @return the context
     */
    public IaasContext getIaasContext() {
        if (iaasContext == null) {
            iaasContext = new IaasContext();
        }
        return iaasContext;
    }

    /**
     * Returns whether parallel provisioning is enabled. This is currently only
     * supported for testing purpose.
     * 
     * @return <code>true</code> when parallel provisioning is enabled or not
     *         set
     */
    public boolean isParallelProvisioningEnabled() {
        String enabled = getValue(ENABLE_PARALLEL_PROVISIONING,
                settings.getConfigSettings());
        if (enabled == null) {
            return true;
        }
        return Boolean.parseBoolean(enabled);
    }

    /**
     * Returns the time, the controller waits before issuing the next request.
     * 
     * @return wait time in milliseconds
     */
    public long getControllerWaitTime() {
        long waitTime = 0;
        try {
            waitTime = Long
                    .valueOf(
                            getValue(CONTROLLER_WAIT_TIME,
                                    settings.getConfigSettings())).longValue();
        } catch (NumberFormatException e) {
        }
        return waitTime;
    }

    public ProvisioningSettings getSettings() {
        return settings;
    }

    public String getKeystoreType() {
        return getValidatedConfigurationProperty(IAAS_API_KEYSTORE_TYPE);
    }

    public String getKeystore() {
        return getValidatedConfigurationProperty(IAAS_API_KEYSTORE);
    }

    public String getKeystorePassword() {
        return getValidatedConfigurationProperty(IAAS_API_KEYSTORE_PASS);
    }

    /**
     * Convert a set of strings into a single CSV string.
     * 
     * @param values
     * @return
     */
    private String getCSVString(Set<String> values) {
        String result = null;
        if (values != null) {
            StringBuffer sb = new StringBuffer();
            for (String ip : values) {
                if (notNullNorEmpty(ip)) {
                    if (sb.length() > 0) {
                        sb.append(",");
                    }
                    sb.append(ip.trim());
                }
            }
            if (sb.length() > 0) {
                result = sb.toString();
            }
        }
        return result;
    }

    private String getValue(String key, Map<String, Setting> source) {
        Setting setting = source.get(key);
        return setting != null ? setting.getValue() : null;
    }
}
