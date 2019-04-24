/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2018
 *
 * <p>Creation Date: 2016-05-24
 *
 * <p>*****************************************************************************
 */
package org.oscm.app.vmware.business;

import com.vmware.vim25.LocalizableMessage;
import com.vmware.vim25.TaskInfo;
import com.vmware.vim25.TaskInfoState;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.xml.datatype.XMLGregorianCalendar;
import org.oscm.app.v2_0.data.PasswordAuthentication;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.data.ServiceUser;
import org.oscm.app.v2_0.data.Setting;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.vmware.business.VMwareValue.Unit;
import org.oscm.app.vmware.business.model.Cluster;
import org.oscm.app.vmware.business.model.IPPool;
import org.oscm.app.vmware.business.model.VCenter;
import org.oscm.app.vmware.business.model.VLAN;
import org.oscm.app.vmware.i18n.Messages;
import org.oscm.app.vmware.persistence.APPDataAccessService;
import org.oscm.app.vmware.persistence.DataAccessService;
import org.oscm.app.vmware.persistence.VMwareNetwork;
import org.oscm.app.vmware.remote.bes.Credentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Class to read and return the VMware specific properties. */
public class VMPropertyHandler {

  private static final Logger logger = LoggerFactory.getLogger(VMPropertyHandler.class);

  private double templateDiskSpace;
  private final ProvisioningSettings settings;
  DataAccessService das_stub = null;

  public static final String TS_GUEST_READY_TIMEOUT = "READY_TIMEOUT";

  public static final String GUEST_READY_TIMEOUT_REF = "READY_TIMEOUT_REF";

  public static final String SNAPSHOT_ID = "SNAPSHOT_ID";

  public static final String TS_SERVICE_TYPE = "SERVICE_TYPE";

  /** for target folder generation */
  private static final String PLACEHOLDER_ORGID = "${ORGID}";

  /** for instance name generation, see column identifier in table site */
  private static final String PLACEHOLDER_VCENTER = "${VC}";

  private static final String PLACEHOLDER_DATACENTER = "${DC}";
  private static final String PLACEHOLDER_ID3 = "${ID3}";
  private static final String PLACEHOLDER_ID4 = "${ID4}";
  private static final String PLACEHOLDER_ID5 = "${ID5}";
  private static final String PLACEHOLDER_ID6 = "${ID6}";
  private static final String PLACEHOLDER_ID7 = "${ID7}";
  private static final String PLACEHOLDER_ID8 = "${ID8}";
  private static final String PLACEHOLDER_ID10 = "${ID10}";
  private static final String PLACEHOLDER_ID12 = "${ID12}";

  /** The key for accessing the BES webservice. */
  public static final String BSS_USER_KEY = "BSS_USER_KEY";

  /** The id for accessing the BES webservice. */
  public static final String BSS_USER_ID = "BSS_USER_ID";

  /** The password for accessing the BES webservice key. */
  public static final String BSS_USER_PWD = "BSS_USER_PWD";

  /** Boolean which indicates whether SSO should be used. */
  public static final String BSS_USER_SSO = "BSS_USER_SSO";

  /** The timer for evaluating the subscription end date is initialized with this value. */
  public static final String CTL_TIMER_SCHEDULE_DAY = "TIMER_SCHEDULE_DAY";

  /** The timer for evaluating the subscription end date is initialized with this value. */
  public static final String CTL_TIMER_SCHEDULE_HOUR = "TIMER_SCHEDULE_HOUR";

  /** The timer for evaluating the subscription end date is initialized with this value. */
  public static final String CTL_TIMER_SCHEDULE_MINUTE = "TIMER_SCHEDULE_MINUTE";

  /** Folder with shell scripts for report command */
  public static final String CTL_REPORT_FOLDER = "REPORT_FOLDER";

  /**
   * This URL is used to create approval tasks when the service parameter
   * START_PROCESS_AFTER_CREATION is defined and set to true
   */
  public static final String CTL_APPROVAL_URL = "APPROVAL_URL";

  public static final String CTL_APPROVAL_USER_ID = "APPROVAL_USER_ID";

  public static final String CTL_APPROVAL_USER_PWD = "APPROVAL_USER_PWD";

  /** NOW-IT CMDB integration. Folder for csv files */
  public static final String CTL_CMDB_FOLDER = "CMDB_FOLDER";

  /** NOW-IT Nagios integration */
  public static final String CTL_NAGIOS_SERVER = "NAGIOS_SERVER";

  /** NOW-IT Nagios integration */
  public static final String CTL_NAGIOS_SITE = "NAGIOS_SITE";

  /**
   * The key of the last invoked vSphere task. This vSphere identifier references an asynchronous
   * task in order to retrieve the status of that task later on.
   */
  public static final String TASK_KEY = "TASK_KEY";

  /** The time of starting the vSphere task. */
  public static final String TASK_STARTTIME = "TASK_STARTTIME";

  /**
   * When the subscription end date is set the subscription is going to several stages until it is
   * automatically deleted
   */
  public enum SubscriptionEndStatus {
    UNDEFINED,
    SCHEDULED_FOR_NOTIFICATION,
    SCHEDULED_FOR_DEACTIVATION,
    SCHEDULED_FOR_DELETION
  };

  public static final String SUBSCRIPTION_END_STATUS = "SUBSCRIPTION_END_STATUS";

  /** Fixed target host name (if not calculated by the balancer) */
  public static final String TS_TARGET_HOST = "TARGET_HOST";

  /** Fixed target storage name (if not calculated by the balancer) */
  public static final String TS_TARGET_STORAGE = "TARGET_STORAGE";

  /**
   * The cluster where the VM is instantiated. Do not use together with TARGET_LOCATION as service
   * parameter.
   */
  public static final String TS_TARGET_CLUSTER = "TARGET_CLUSTER";

  /**
   * The cluster where the VM is instantiated. Do not use together with TARGET_LOCATION as service
   * parameter.
   */
  public static final String TS_TARGET_VCENTER_SERVER = "TARGET_VCENTER_SERVER";

  /**
   * The cluster where the VM is instantiated. Do not use together with TARGET_LOCATION as service
   * parameter.
   */
  public static final String TS_TARGET_DATACENTER = "TARGET_DATACENTER";

  /** The vSphere folder where the VM will be located */
  public static final String TS_TARGET_FOLDER = "TARGET_FOLDER";

  /**
   * When the target folder can be chosen during the subscription process TARGET_FOLDER_ROOT
   * determines all the sub folders that the user can choose from.
   */
  public static final String TS_TARGET_FOLDER_ROOT = "TARGET_FOLDER_ROOT";

  /** The custom name of the new instance */
  public static final String TS_INSTANCENAME = "INSTANCENAME";

  /** The predefined prefix of the new instance */
  public static final String TS_INSTANCENAME_PREFIX = "INSTANCENAME_PREFIX";

  /** The regular expression for validating the new instance name */
  public static final String TS_INSTANCENAME_PATTERN = "INSTANCENAME_PATTERN";

  /** Size of main memory (MB). */
  public static final String TS_AMOUNT_OF_RAM = "AMOUNT_OF_RAM";

  /** Number of CPUs. */
  public static final String TS_NUMBER_OF_CPU = "NUMBER_OF_CPU";

  /** Size of system disk (GB). */
  public static final String TS_DISK_SIZE = "DISK_SIZE";

  /** Size of custom data disk (GB). */
  public static final String TS_DATA_DISK_SIZE = "DATA_DISK_SIZE_#";

  /** Target location for the data disk, e.g. /home/user/data for Linux VM or d: for Windows VM. */
  public static final String TS_DATA_DISK_TARGET = "DATA_DISK_TARGET_#";

  /** Regular expression pattern used to validate the target for the data disk. */
  public static final String TS_DATA_DISK_TARGET_VALIDATION = "DATA_DISK_TARGET_VALIDATION_#";

  /** Saves the data disk mapping. Internal mapping of index to VMware device key. */
  public static final String DATA_DISK_KEY = "DATA_DISK_KEY_";

  /** The template that will be used to clone a VM. */
  public static final String TS_TEMPLATENAME = "TEMPLATENAME";

  /**
   * This parameter contains a filename. The file content describes the user interface for the
   * external configuration tool.
   */
  public static final String TS_WEBUI_CONFIG = "WEBUI_CONFIG";

  /**
   * Timezone setting for Windows operating systems. See
   * http://msdn.microsoft.com/en-us/library/ms912391(v=winembedded.11).aspx
   */
  public static final String CTL_TIMEZONE_WINDOWS = "TIMEZONE_WINDOWS";

  /**
   * Timezone setting for Linux operating systems. See http://pubs.vmware.com/vsphere
   * -55/index.jsp?topic=%2Fcom.vmware.wssdk.smssdk.doc%2Ftimezone.html
   */
  public static final String CTL_TIMEZONE_LINUX = "TIMEZONE_LINUX";

  /**
   * A URL that points to a shell script that will be retrieved and executed after the VM has been
   * created and reconfigured
   */
  public static final String TS_SCRIPT_URL = "SCRIPT_URL";

  public static final String TS_SCRIPT_USERID = "SCRIPT_USERID";

  public static final String TS_SCRIPT_PWD = "SCRIPT_PWD";

  /** for Linux this is the FQDN without the hostname, for Windows this is the Windows domain */
  public static final String TS_DOMAIN_NAME = "DOMAIN_NAME";

  public static final String TS_WINDOWS_DOMAIN_JOIN = "WINDOWS_DOMAIN_JOIN";
  public static final String TS_WINDOWS_DOMAIN_ADMIN = "WINDOWS_DOMAIN_ADMIN";
  public static final String TS_WINDOWS_DOMAIN_ADMIN_PWD = "WINDOWS_DOMAIN_ADMIN_PWD";
  public static final String TS_WINDOWS_WORKGROUP = "WINDOWS_WORKGROUP";
  public static final String TS_WINDOWS_LICENSE_KEY = "WINDOWS_LICENSE_KEY";
  public static final String TS_WINDOWS_LOCAL_ADMIN_PWD = "WINDOWS_LOCAL_ADMIN_PWD";
  public static final String TS_SYSPREP_RUNONCE_COMMAND = "SYSPREP_RUNONCE_COMMAND";

  public static final String TS_LINUX_ROOT_PWD = "LINUX_ROOT_PWD";

  /** The number of NICs is determined from the given template (see TS_TEMPLATENAME). */
  public static final String TS_NUMBER_OF_NICS = "NUMBER_OF_NICS";

  public static final String NETWORK_SETTING_DHCP = "DHCP";
  public static final String NETWORK_SETTING_MANUAL = "MANUAL";
  public static final String NETWORK_SETTING_DATABASE = "DATABASE";

  public static final String TS_NIC1_NETWORK_ADAPTER = "NIC1_NETWORK_ADAPTER";
  public static final String TS_NIC1_NETWORK_SETTINGS = "NIC1_NETWORK_SETTINGS";
  public static final String TS_NIC1_IP_ADDRESS = "NIC1_IP_ADDRESS";
  public static final String TS_NIC1_SUBNET_MASK = "NIC1_SUBNET_MASK";
  public static final String TS_NIC1_GATEWAY = "NIC1_GATEWAY";
  public static final String TS_NIC1_DNS_SERVER = "NIC1_DNS_SERVER";
  public static final String TS_NIC1_DNS_SUFFIX = "NIC1_DNS_SUFFIX";

  public static final String TS_NIC2_NETWORK_ADAPTER = "NIC2_NETWORK_ADAPTER";
  public static final String TS_NIC2_NETWORK_SETTINGS = "NIC2_NETWORK_SETTINGS";
  public static final String TS_NIC2_IP_ADDRESS = "NIC2_IP_ADDRESS";
  public static final String TS_NIC2_SUBNET_MASK = "NIC2_SUBNET_MASK";
  public static final String TS_NIC2_GATEWAY = "NIC2_GATEWAY";
  public static final String TS_NIC2_DNS_SERVER = "NIC2_DNS_SERVER";
  public static final String TS_NIC2_DNS_SUFFIX = "NIC2_DNS_SUFFIX";

  public static final String TS_NIC3_NETWORK_ADAPTER = "NIC3_NETWORK_ADAPTER";
  public static final String TS_NIC3_NETWORK_SETTINGS = "NIC3_NETWORK_SETTINGS";
  public static final String TS_NIC3_IP_ADDRESS = "NIC3_IP_ADDRESS";
  public static final String TS_NIC3_SUBNET_MASK = "NIC3_SUBNET_MASK";
  public static final String TS_NIC3_GATEWAY = "NIC3_GATEWAY";
  public static final String TS_NIC3_DNS_SERVER = "NIC3_DNS_SERVER";
  public static final String TS_NIC3_DNS_SUFFIX = "NIC3_DNS_SUFFIX";

  public static final String TS_NIC4_NETWORK_ADAPTER = "NIC4_NETWORK_ADAPTER";
  public static final String TS_NIC4_NETWORK_SETTINGS = "NIC4_NETWORK_SETTINGS";
  public static final String TS_NIC4_IP_ADDRESS = "NIC4_IP_ADDRESS";
  public static final String TS_NIC4_SUBNET_MASK = "NIC4_SUBNET_MASK";
  public static final String TS_NIC4_GATEWAY = "NIC4_GATEWAY";
  public static final String TS_NIC4_DNS_SERVER = "NIC4_DNS_SERVER";
  public static final String TS_NIC4_DNS_SUFFIX = "NIC4_DNS_SUFFIX";

  /** Signals whether an existing VM instance should be imported (mapped to the subscription). */
  public static final String TS_IMPORT_EXISTING_VM = "IMPORT_EXISTING_VM";

  /** If set to true then process triggers are approved automatically */
  public static final String TS_AUTO_APPROVE_TRIGGER = "AUTO_APPROVE_TRIGGER";

  /**
   * The id of requesting user. Can only be retrieved in VMwareController.createInstance() and
   * VMwareController.modifyInstance() and is stored there as service instance parameter for later
   * use.
   */
  public static final String REQUESTING_USER = "REQUESTING_USER";

  /**
   * The mail of requesting user. Can only be retrieved in VMwareController.createInstance() and
   * VMwareController.modifyInstance() and is stored there as service instance parameter for later
   * use.
   */
  public static final String REQUESTING_USER_EMAIL = "REQUESTING_USER_EMAIL";

  /**
   * Pauses provisioning. It works like a synchronous trigger. This can be used for configuration
   * steps that must be done by administrators after a VM is created like installing antivirus
   * software. This person gets notified if the provisioning has paused. The person receives an
   * email with a link in it to continue provisioning.
   */
  public static final String TS_MAIL_FOR_COMPLETION = "MAIL_FOR_COMPLETION";

  /**
   * Identifies the person that is responsible for a VM. Can be used together with the LDAP to
   * retrieve other information about that person. Then this identifier is used as commonName
   * attribute.
   */
  public static final String TS_RESPONSIBLE_PERSON = "RESPONSIBLE_PERSON";

  /** A pattern which describes the naming rules for the access info. */
  public static final String TS_ACCESS_INFO = "ACCESS_INFO";

  /**
   * If distributed virtual switches are used, this parameter is mandatory. It requires <code>
   * TS_PORTGROUP</code>. Give the UUID of the switch.
   */
  public static final String TS_NIC1_SWITCH_UUID = "NIC1_SWITCH_UUID";

  public static final String TS_NIC2_SWITCH_UUID = "NIC2_SWITCH_UUID";
  public static final String TS_NIC3_SWITCH_UUID = "NIC3_SWITCH_UUID";
  public static final String TS_NIC4_SWITCH_UUID = "NIC4_SWITCH_UUID";

  private static final String TS_NIC1_PORTGROUP = "NIC1_PORTGROUP";
  private static final String TS_NIC2_PORTGROUP = "NIC2_PORTGROUP";
  private static final String TS_NIC3_PORTGROUP = "NIC3_PORTGROUP";
  private static final String TS_NIC4_PORTGROUP = "NIC4_PORTGROUP";

  /** */

  /** internal settings for state machine execution */
  public static final String SM_STATE = "SM_STATE";

  public static final String SM_STATE_HISTORY = "SM_STATE_HISTORY";
  public static final String SM_STATE_MACHINE = "SM_STATE_MACHINE";
  public static final String SM_ERROR_MESSAGE = "SM_ERROR_MESSAGE";

  /** The port which is used to access the vm remote console */
  public static final String TS_VSPHERE_CONSOLE_PORT = "VSPHERE_CONSOLE_PORT";

  public static final String TS_IPPOOL_FOR_PORTGROUP = "IPPOOL_FOR_PORTGROUP";

  public VMPropertyHandler(ProvisioningSettings settings) {
    this.settings = settings;
  }

  public ProvisioningSettings getSettings() {
    return settings;
  }

  public void setSetting(String key, String value) {
    if (value != null) {
      settings.getParameters().put(key, new Setting(key, value));
    } else {
      logger.warn("Setting not set because null value. key:" + key);
    }
  }

  /** @return parameters keys for the data disk mount point, the list is sorted ascending */
  public List<String> getDataDiskMountPointParameterKeys() {
    String regex = TS_DATA_DISK_TARGET.replace("#", "").concat("\\d+");
    List<String> result = new ArrayList<>();
    for (String key : settings.getParameters().keySet()) {
      if (key.matches(regex)) {
        result.add(key);
      }
    }
    Collections.sort(result);
    return result;
  }

  /** @return parameters keys for the data disk size, the list is sorted ascending */
  public List<String> getDataDiskSizeParameterKeys() {
    String regex = TS_DATA_DISK_SIZE.replace("#", "").concat("\\d+");
    List<String> result = new ArrayList<>();
    for (String key : settings.getParameters().keySet()) {
      if (key.matches(regex)) {
        result.add(key);
      }
    }
    Collections.sort(result);
    return result;
  }

  public String getMountPointValidationPattern(String mointPointKey) {
    String patternKey = mointPointKey.replace("TARGET_", "TARGET_VALIDATION_");
    return getValue(patternKey, settings.getParameters());
  }

  public String getGuestReadyTimeout(String key) {
    if (settings.getParameters().containsKey(key)) {
      return getValue(key, settings.getParameters());
    }
    return getValue(key, settings.getConfigSettings());
  }

  public int getNumberOfNetworkAdapter() {
    return Integer.parseInt(getServiceSetting(VMPropertyHandler.TS_NUMBER_OF_NICS));
  }

  /**
   * Release IP address for later usage by another VM instance. Only valid for manually assigned IP
   * addresses. Does not make sense for DHCP.
   */
  public void releaseManuallyDefinedIPAddresses() throws Exception {
    logger.debug("");
    int numNIC = Integer.parseInt(getServiceSetting(VMPropertyHandler.TS_NUMBER_OF_NICS));
    for (int i = 1; i <= numNIC; i++) {
      if (isAdapterConfiguredByDatabase(i)) {
        String ipAddress = getIpAddress(i);

        if (ipAddress != null) {
          String vcenter = getTargetVCenterServer();
          String datacenter = getTargetDatacenter();
          String cluster = getTargetCluster();
          String vlan = getVLAN(i);
          try {
            DataAccessService das = getDataAccessService();
            das.releaseIPAddress(vcenter, datacenter, cluster, vlan, ipAddress);
          } catch (Exception e) {
            logger.error("Failed to release IP address " + ipAddress, e);
          }
        }
      }
    }
  }

  /**
   * For all NICs that get there network configuration from the database:
   *
   * <ul>
   *   <li>Reserve an IP address
   *   <li>Retrieve the network settings for the given cluster
   *   <li>Set the NIC related technical service parameter
   * </ul>
   */
  public void getNetworkSettingsFromDatabase() throws APPlatformException {

    int numberOfNICs = Integer.parseInt(getServiceSetting(VMPropertyHandler.TS_NUMBER_OF_NICS));
    for (int i = 1; i <= numberOfNICs; i++) {

      if (isAdapterConfiguredByDatabase(i)) {
        configureByDatabase(i);
      } else if (!getIpPoolForPortgroup().isEmpty()) {
        PortgroupIpSettings pis = new PortgroupIpSettings(this);
        String ipAddress = pis.getIpAdressFromIpPool();
        if (i == 1) {
          settings
              .getParameters()
              .put(TS_NIC1_IP_ADDRESS, new Setting(TS_NIC1_IP_ADDRESS, ipAddress));
        }
      }
    }
  }

  public void configureByDatabase(int i) throws APPlatformException {

    DataAccessService das = getDataAccessService();
    String vcenter = getTargetVCenterServer();
    String datacenter = getTargetDatacenter();
    String cluster = getTargetCluster();
    logger.debug("vcenter: " + vcenter + " datacenter: " + datacenter + " cluster: " + cluster);
    String vlan = das.getVLANwithMostIPs(vcenter, datacenter, cluster);
    if (vlan == null) {
      throw new APPlatformException(
          Messages.getAll("error_read_vlans", new Object[] {vcenter, datacenter, cluster})
              .get(0)
              .getText());
    }

    settings
        .getParameters()
        .put("NIC" + i + "_NETWORK_ADAPTER", new Setting("NIC" + i + "_NETWORK_ADAPTER", vlan));

    String ipAddress;
    VMwareNetwork nw;
    try {
      ipAddress = das.reserveIPAddress(vcenter, datacenter, cluster, vlan);
      nw = das.getNetworkSettings(vcenter, datacenter, cluster, vlan);
    } catch (Exception e) {
      throw new APPlatformException(
          Messages.getAll(
                  "error_read_static_network_config",
                  new Object[] {Integer.valueOf(i), e.getMessage()})
              .get(0)
              .getText());
    }

    logger.debug(
        "NIC"
            + i
            + " VLAN: "
            + vlan
            + " IP address: "
            + ipAddress
            + " SubnetMask: "
            + nw.getSubnetMask()
            + " Gateway: "
            + nw.getGateway()
            + " DNS Server: "
            + nw.getDnsServer()
            + " DNS Suffix: "
            + nw.getDnsSuffix());

    if (i == 1) {
      settings.getParameters().put(TS_NIC1_IP_ADDRESS, new Setting(TS_NIC1_IP_ADDRESS, ipAddress));
      settings
          .getParameters()
          .put(TS_NIC1_SUBNET_MASK, new Setting(TS_NIC1_SUBNET_MASK, nw.getSubnetMask()));
      settings.getParameters().put(TS_NIC1_GATEWAY, new Setting(TS_NIC1_GATEWAY, nw.getGateway()));
      settings
          .getParameters()
          .put(TS_NIC1_DNS_SERVER, new Setting(TS_NIC1_DNS_SERVER, nw.getDnsServer()));
      settings
          .getParameters()
          .put(TS_NIC1_DNS_SUFFIX, new Setting(TS_NIC1_DNS_SUFFIX, nw.getDnsSuffix()));
    } else if (i == 2) {
      settings.getParameters().put(TS_NIC2_IP_ADDRESS, new Setting(TS_NIC1_IP_ADDRESS, ipAddress));
      settings
          .getParameters()
          .put(TS_NIC2_SUBNET_MASK, new Setting(TS_NIC1_SUBNET_MASK, nw.getSubnetMask()));
      settings.getParameters().put(TS_NIC2_GATEWAY, new Setting(TS_NIC1_GATEWAY, nw.getGateway()));
      settings
          .getParameters()
          .put(TS_NIC2_DNS_SERVER, new Setting(TS_NIC1_DNS_SERVER, nw.getDnsServer()));
      settings
          .getParameters()
          .put(TS_NIC2_DNS_SUFFIX, new Setting(TS_NIC1_DNS_SUFFIX, nw.getDnsSuffix()));
    } else if (i == 3) {
      settings.getParameters().put(TS_NIC3_IP_ADDRESS, new Setting(TS_NIC1_IP_ADDRESS, ipAddress));
      settings
          .getParameters()
          .put(TS_NIC3_SUBNET_MASK, new Setting(TS_NIC1_SUBNET_MASK, nw.getSubnetMask()));
      settings.getParameters().put(TS_NIC3_GATEWAY, new Setting(TS_NIC1_GATEWAY, nw.getGateway()));
      settings
          .getParameters()
          .put(TS_NIC3_DNS_SERVER, new Setting(TS_NIC1_DNS_SERVER, nw.getDnsServer()));
      settings
          .getParameters()
          .put(TS_NIC3_DNS_SUFFIX, new Setting(TS_NIC1_DNS_SUFFIX, nw.getDnsSuffix()));
    } else if (i == 4) {
      settings.getParameters().put(TS_NIC4_IP_ADDRESS, new Setting(TS_NIC1_IP_ADDRESS, ipAddress));
      settings
          .getParameters()
          .put(TS_NIC4_SUBNET_MASK, new Setting(TS_NIC1_SUBNET_MASK, nw.getSubnetMask()));
      settings.getParameters().put(TS_NIC4_GATEWAY, new Setting(TS_NIC1_GATEWAY, nw.getGateway()));
      settings
          .getParameters()
          .put(TS_NIC4_DNS_SERVER, new Setting(TS_NIC1_DNS_SERVER, nw.getDnsServer()));
      settings
          .getParameters()
          .put(TS_NIC4_DNS_SUFFIX, new Setting(TS_NIC1_DNS_SUFFIX, nw.getDnsSuffix()));
    }
  }

  /**
   * Returns the defined amount of memory (MB).
   *
   * @return the memory
   */
  public long getConfigMemoryMB() {
    return Long.parseLong(getServiceSettingValidated(TS_AMOUNT_OF_RAM));
  }

  /**
   * Returns the defined number of CPUs.
   *
   * @return the number of CPUs
   */
  public int getConfigCPUs() {
    return Integer.parseInt(getServiceSettingValidated(TS_NUMBER_OF_CPU));
  }

  /**
   * Returns the defined disk size in GB.
   *
   * @return the disk size in GBs or .0 if not defined
   */
  public double getConfigDiskSpaceMB() throws APPlatformException {
    String value = getServiceSetting(TS_DISK_SIZE);
    try {
      return (value != null) ? 1024.0 * (Long.parseLong(value)) : .0;
    } catch (NumberFormatException ne) {
      throw new APPlatformException(
          Messages.getAll("error_invalid_diskspacenum", new Object[] {value}), ne);
    }
  }

  /**
   * Returns the list of additionally defined data disks (sizes in MB).
   *
   * @return a list of Long values for all defined data disks
   */
  public Double[] getDataDisksMB() {
    List<Double> ddlist = new ArrayList<>();
    for (int i = 1; i <= 999; i++) {
      String diskPrefix = TS_DATA_DISK_SIZE.replace("#", Integer.toString(i));
      String value = getServiceSetting(diskPrefix);
      if (value != null && value.length() > 0) {
        double diskSize = 1024.0 * Long.parseLong(value);
        ddlist.add(Double.valueOf(diskSize));
      } else {
        break;
      }
    }

    return ddlist.toArray(new Double[ddlist.size()]);
  }

  /**
   * Returns the list of additionally defined data disks as comparable string.
   *
   * @return a string with all defined data disks
   */
  public String getDataDisksMBAsString() {
    Double[] ddisks = getDataDisksMB();
    StringBuffer rc = new StringBuffer();
    for (Double ddisk : ddisks) {
      rc.append(ddisk.toString());
      rc.append("#");
    }
    return rc.toString();
  }

  /** Returns the key of a custom virtual disk or "0" if not defined */
  public int getDataDiskKey(int index) {
    String val = getValue(DATA_DISK_KEY + Integer.toString(index), settings.getParameters());
    return (val != null && val.length() > 0) ? Integer.parseInt(val) : 0;
  }

  /** Saves the key of a custom virtual disk. Internal mapping of index to VMware device key. */
  public void setDataDiskKey(int index, int key) {
    setValue(
        DATA_DISK_KEY + Integer.toString(index), Integer.toString(key), settings.getParameters());
  }

  public List<VLAN> getVLANs(Cluster cluster) {
    try {
      DataAccessService das = getDataAccessService();
      return das.getVLANs(cluster);
    } catch (Exception e) {
      logger.error("Failed to retrieve VLAN list.", e);
      return new ArrayList<>();
    }
  }

  public int addVLAN(VLAN vlan) {
    int newTKey = -1;
    DataAccessService das = getDataAccessService();
    try {
      newTKey = das.addVLAN(vlan);
    } catch (Exception e) {
      logger.error("Failed to add VLAN " + vlan.getName(), e);
    }
    return newTKey;
  }

  public void deleteVLAN(VLAN vlan) {
    DataAccessService das = getDataAccessService();
    try {
      das.deleteVLAN(vlan);
    } catch (Exception e) {
      logger.error("Failed to delete VLAN " + vlan.getName(), e);
    }
  }

  public void updateVLANs(List<VLAN> vlans) {
    DataAccessService das = getDataAccessService();
    try {
      das.updateVLANs(vlans);
    } catch (Exception e) {
      logger.error("Failed to update VLANs.", e);
    }
  }

  public String getVsphereConsolePort() {
    return getServiceSetting(VMPropertyHandler.TS_VSPHERE_CONSOLE_PORT);
  }

  public String getTargetDatacenter() {
    return getServiceSetting(VMPropertyHandler.TS_TARGET_DATACENTER);
  }

  private String getDatacenterId() throws APPlatformException {

    String vcenter = getTargetVCenterServer();
    String datacenter = getTargetDatacenter();

    try {
      DataAccessService das = getDataAccessService();
      return das.getDatacenterId(vcenter, datacenter);
    } catch (Exception e) {
      throw new APPlatformException(e.getMessage());
    }
  }

  /**
   * Get all vCenter server from database (with datacenters and clusters)
   *
   * @return list of vCenter server or empty list if not defined
   */
  public List<VCenter> getTargetVCenter() {
    List<VCenter> vcenter;
    DataAccessService das = getDataAccessService();
    try {
      vcenter = das.getVCenter();
    } catch (Exception e) {
      logger.error("Failed to retrieve vCenter server list.", e);
      vcenter = new ArrayList<>();
    }

    return vcenter;
  }

  public List<Cluster> getClusters() {
    List<Cluster> clusters;
    DataAccessService das = getDataAccessService();
    try {
      clusters = das.getCluster();
    } catch (Exception e) {
      logger.error("Failed to retrieve vCenter server list.", e);
      clusters = new ArrayList<>();
    }
    return clusters;
  }

  public List<IPPool> getIPPool(VLAN vlan) {
    List<IPPool> ipPools;
    DataAccessService das = getDataAccessService();
    try {
      ipPools = das.getIPPool(vlan);
    } catch (Exception e) {
      logger.error("Failed to retrieve ipPool list.", e);
      ipPools = new ArrayList<>();
    }
    return ipPools;
  }

  public void saveTargetVCenter(VCenter vcenter) {
    DataAccessService das = getDataAccessService();
    try {
      das.setVCenter(vcenter);
    } catch (Exception e) {
      logger.error("Failed to save vCenter server configuration.", e);
    }
  }

  /**
   * Returns the ippools.
   *
   * @return the ip adresses defined in the portgroup
   */
  public String getIpPoolForPortgroup() {
    String value = "";
    value = getServiceSetting(TS_IPPOOL_FOR_PORTGROUP);
    return value;
  }

  public String getTargetVCenterServer() {
    return getServiceSetting(VMPropertyHandler.TS_TARGET_VCENTER_SERVER);
  }

  public String getTargetCluster() {
    return getServiceSetting(VMPropertyHandler.TS_TARGET_CLUSTER);
  }

  /** @return the full name of the new instance (including the prefix) */
  public String getInstanceName() throws APPlatformException {
    StringBuffer b = new StringBuffer();
    String prefix = getServiceSetting(VMPropertyHandler.TS_INSTANCENAME_PREFIX);
    String name = getServiceSettingValidated(TS_INSTANCENAME);
    if (prefix != null && !name.startsWith(prefix) && !isImportOfExistingVM()) {
      b.append(prefix);
    }
    b.append(getInstanceNameCustom(name));
    return b.toString();
  }

  public String getTargetFolder() {
    String targetFolder = getServiceSetting(TS_TARGET_FOLDER);
    if (targetFolder != null && targetFolder.indexOf(PLACEHOLDER_ORGID) >= 0) {
      String orgId = settings.getOrganizationId();
      targetFolder = targetFolder.replace(PLACEHOLDER_ORGID, orgId);
      setValue(TS_TARGET_FOLDER, targetFolder, settings.getParameters());
    }
    return targetFolder;
  }

  /**
   * Returns the custom name of the new instance.
   *
   * @return the name of the custom defined instance name
   */
  private String getInstanceNameCustom(String name) throws APPlatformException {

    boolean contains_vcenter = (name.indexOf(PLACEHOLDER_VCENTER) >= 0);
    boolean contains_datacenter = (name.indexOf(PLACEHOLDER_DATACENTER) >= 0);
    boolean contains_id3 = (name.indexOf(PLACEHOLDER_ID3) >= 0);
    boolean contains_id4 = (name.indexOf(PLACEHOLDER_ID4) >= 0);
    boolean contains_id5 = (name.indexOf(PLACEHOLDER_ID5) >= 0);
    boolean contains_id6 = (name.indexOf(PLACEHOLDER_ID6) >= 0);
    boolean contains_id7 = (name.indexOf(PLACEHOLDER_ID7) >= 0);
    boolean contains_id8 = (name.indexOf(PLACEHOLDER_ID8) >= 0);
    boolean contains_id10 = (name.indexOf(PLACEHOLDER_ID10) >= 0);
    boolean contains_id12 = (name.indexOf(PLACEHOLDER_ID12) >= 0);

    try {
      DataAccessService das = getDataAccessService();
      String vcenter = getTargetVCenterServer();
      String vcenterId = das.getVCenterIdentifier(vcenter);

      if (contains_vcenter) {
        name = name.replace(PLACEHOLDER_VCENTER, vcenterId);
      }

      if (contains_datacenter) {
        String datacenterId = getDatacenterId();
        name = name.replace(PLACEHOLDER_DATACENTER, datacenterId);
      }

      if (contains_id3) {
        String seqNum = das.getNextSequenceNumber(3, vcenterId);
        name = name.replace(PLACEHOLDER_ID3, seqNum);
      }
      if (contains_id4) {
        String seqNum = das.getNextSequenceNumber(4, vcenterId);
        name = name.replace(PLACEHOLDER_ID4, seqNum);
      }
      if (contains_id5) {
        String seqNum = das.getNextSequenceNumber(5, vcenterId);
        name = name.replace(PLACEHOLDER_ID5, seqNum);
      }
      if (contains_id6) {
        String seqNum = das.getNextSequenceNumber(6, vcenterId);
        name = name.replace(PLACEHOLDER_ID6, seqNum);
      }
      if (contains_id7) {
        String seqNum = das.getNextSequenceNumber(7, vcenterId);
        name = name.replace(PLACEHOLDER_ID7, seqNum);
      }
      if (contains_id8) {
        String seqNum = das.getNextSequenceNumber(8, vcenterId);
        name = name.replace(PLACEHOLDER_ID8, seqNum);
      }
      if (contains_id10) {
        String seqNum = das.getNextSequenceNumber(10, vcenterId);
        name = name.replace(PLACEHOLDER_ID10, seqNum);
      }
      if (contains_id12) {
        String seqNum = das.getNextSequenceNumber(12, vcenterId);
        name = name.replace(PLACEHOLDER_ID12, seqNum);
      }

      setValue(TS_INSTANCENAME, name, settings.getParameters());

    } catch (Exception e) {
      logger.error("Failed to generate instance name", e);
      String message = Messages.get(getLocale(), "error_generate_instancename");
      throw new APPlatformException(message, e);
    }

    return name;
  }

  /**
   * Returns the pattern for creating the access info of the instance.
   *
   * <p>After the instance has been created the real access info will be stored.
   *
   * @return the pattern or value of the access info
   */
  public String getAccessInfo() {
    return getServiceSetting(TS_ACCESS_INFO);
  }

  public void setAccessInfo(String value) {
    setValue(TS_ACCESS_INFO, value, settings.getParameters());
  }

  public void setRequestingUser(ServiceUser userInfo) {
    if (userInfo != null && userInfo.getUserId() != null) {
      setValue(REQUESTING_USER, userInfo.getUserId(), settings.getParameters());
    }

    if (userInfo != null && userInfo.getEmail() != null) {
      setValue(REQUESTING_USER_EMAIL, userInfo.getEmail(), settings.getParameters());
    }
  }

  /**
   * Returns the name of the matching template.
   *
   * @return the name of the matching template
   */
  public String getTemplateName() {
    return getServiceSettingValidated(TS_TEMPLATENAME);
  }

  public ProvisioningSettings getProvisioningSettings() {
    return settings;
  }

  public boolean isServiceSettingTrue(String serviceparameter) {
    String value = getServiceSetting(serviceparameter);
    boolean isTrue = (value != null ? value.equalsIgnoreCase("true") : false);
    logger.debug(serviceparameter + ": " + isTrue);
    return isTrue;
  }

  public boolean isControllerSettingTrue(String serviceparameter) {
    String value = getControllerSetting(serviceparameter);
    boolean isTrue = (value != null ? value.equalsIgnoreCase("true") : false);
    logger.debug(serviceparameter + ": " + isTrue);
    return isTrue;
  }

  /** Updates the key of the last created task. */
  public void setTask(TaskInfo info) {
    if (info != null) {
      Setting s = new Setting(TASK_KEY, info.getKey());
      settings.getParameters().put(TASK_KEY, s);
    } else {
      Setting s = new Setting(TASK_KEY, "");
      settings.getParameters().put(TASK_KEY, s);
    }
    Setting s = new Setting(TASK_STARTTIME, Long.toString(System.currentTimeMillis()));
    settings.getParameters().put(TASK_STARTTIME, s);
    logTaskInfo(info);
  }

  private void logTaskInfo(TaskInfo info) {
    if (info == null) {
      logger.debug("Deleted task info key");
      return;
    }

    TaskInfoState state = info.getState();

    Integer progress = info.getProgress();
    if (state == TaskInfoState.SUCCESS) {
      progress = Integer.valueOf(100);
    } else if (progress == null) {
      progress = Integer.valueOf(0);
    }

    LocalizableMessage desc = info.getDescription();
    String description = desc != null ? desc.getMessage() : "";

    XMLGregorianCalendar queueT = info.getQueueTime();
    String queueTime = queueT != null ? queueT.toGregorianCalendar().getTime().toString() : "";

    XMLGregorianCalendar startT = info.getStartTime();
    String startTime = startT != null ? startT.toGregorianCalendar().getTime().toString() : "";

    XMLGregorianCalendar completeT = info.getCompleteTime();
    String completeTime =
        completeT != null ? completeT.toGregorianCalendar().getTime().toString() : "";

    logger.debug(
        "Save task info key: "
            + info.getKey()
            + " name: "
            + info.getName()
            + " target: "
            + info.getEntityName()
            + " state: "
            + state.name()
            + " progress: "
            + progress
            + "% description: "
            + description
            + " queue-time: "
            + queueTime
            + " start-time: "
            + startTime
            + " complete-time: "
            + completeTime);
  }

  /** Returns the start time of the last created task. */
  public Date getTaskStartTime() {
    String dateVal = getValue(TASK_STARTTIME, settings.getParameters());
    return (dateVal != null) ? new Date(Long.parseLong(dateVal)) : null;
  }

  /** Returns the host configuration */
  public String getHostLoadBalancerConfig() {
    String xml = "";
    String vcenter = getTargetVCenterServer();
    String datacenter = getTargetDatacenter();
    String cluster = getTargetCluster();

    try {

      DataAccessService das = getDataAccessService();
      xml = das.getHostLoadBalancerConfig(vcenter, datacenter, cluster);
    } catch (Exception e) {
      logger.error("VMwarePropertyHandler.getHostLoadBalancerConfig() " + e.getMessage(), e);
      throw new RuntimeException(
          "Failed to retrieve host load balancing configuration for cluster " + cluster);
    }

    if ("".equals(xml)) {
      logger.error(
          "VMwarePropertyHandler.getHostLoadBalancerConfig() The retrieved host load balancing configuration for cluster "
              + cluster
              + " is empty");
      throw new RuntimeException(
          "The retrieved host load balancing configuration for cluster " + cluster + " is empty");
    }

    return xml;
  }

  /** Returns the preferred locale of the user interface. */
  public String getLocale() {
    return settings.getLocale();
  }

  /**
   * Returns the required disk space to create a virtual machine from the requested template in MB.
   */
  public double getTemplateDiskSpaceMB() {
    return templateDiskSpace;
  }

  /** Sets the disk space (MB) required to create a virtual machine from the requested template. */
  public void setTemplateDiskSpaceMB(double templateDiskSpace) {
    this.templateDiskSpace = templateDiskSpace;
  }

  /** Returns printable configuration */
  public String getConfigurationAsString(String locale) throws APPlatformException {
    String config =
        Messages.get(
            locale,
            "mail_VM_configuration.text",
            new Object[] {
              getInstanceName(),
              getTemplateName(),
              settings.getSubscriptionId(),
              Integer.toString(getConfigCPUs()),
              formatMBasGB(getConfigMemoryMB()),
              getDataDisksAsString()
            });
    return config;
  }

  /** Returns printable configuration of data disks */
  public String getDataDisksAsString() throws APPlatformException {
    StringBuffer disksDisplay = new StringBuffer();

    disksDisplay.append(formatMBasGB(getConfigDiskSpaceMB()));

    Double[] ddisks = getDataDisksMB();
    for (Double ddisk : ddisks) {
      disksDisplay.append("/");
      disksDisplay.append(formatMBasGB(ddisk.doubleValue()));
    }

    return disksDisplay.toString();
  }

  /** Returns printable responsible user */
  public String getResponsibleUserAsString(String locale) {
    logger.debug("locale: " + locale);
    String respUser = getServiceSetting(TS_RESPONSIBLE_PERSON);
    if (respUser == null) {
      return null;
    }

    return Messages.get(locale, "mail_VM_configuration_user.text", new Object[] {respUser});
  }

  public String formatMBasGB(double valueMB) {
    DecimalFormat format = new DecimalFormat("#0.# GB");
    return format.format(VMwareValue.fromMegaBytes(valueMB).getValue(Unit.GB));
  }

  /** Returns the user key of the instance or controller specific technology manager. */
  public long getTPUserKey() {
    String configUserKey = getControllerSetting(BSS_USER_KEY);
    return Long.parseLong(configUserKey);
  }

  /** Returns the user Id of the controller specific technology manager. */
  public String getTPUserId() {
    return getControllerSetting(BSS_USER_ID);
  }

  /** Returns the password of the instance or controller specific technology manager. */
  public String getTPUserPassword() {
    String configUserPwd = getControllerSetting(BSS_USER_PWD);
    if (notNullNorEmpty(configUserPwd)) {
      return configUserPwd;
    }
    return getValue(BSS_USER_PWD, settings.getParameters());
  }

  /** Returns the instance or controller specific technology manager. */
  public Credentials getTPUser() {
    Credentials user = new Credentials(isSSO(), getTPUserKey(), getTPUserPassword());
    user.setUserId(getTPUserId());
    return user;
  }

  public PasswordAuthentication getTechnologyProviderCredentials() {
    return getTPUser().toPasswordAuthentication();
  }

  /** Returns whether SSO has been defined. */
  public boolean isSSO() {
    String sso = getControllerSetting(BSS_USER_SSO);
    return (sso != null) ? sso.equalsIgnoreCase("true") : false;
  }

  /**
   * Returns whether the given VM instance should be imported instead of being created.
   *
   * @return true if the defined instance should be imported
   */
  public boolean isImportOfExistingVM() {
    String rc = getValue(TS_IMPORT_EXISTING_VM, settings.getParameters());
    return rc != null && rc.toLowerCase().equals("true");
  }

  /**
   * Sets the marker which defines whether this is an imported VM.
   *
   * <p>Will be cleared after upgrade operations.
   */
  public void setImportOfExistingVM(boolean value) {
    setValue(TS_IMPORT_EXISTING_VM, value ? "true" : "false", settings.getParameters());
  }

  /**
   * Is DHCP defined for the given NIC.
   *
   * @param adapter NIC identifier
   * @return true if DHCP is defined for the given NIC
   * @exception IllegalArgumentException if identifier is out of range
   */
  public boolean isAdapterConfiguredByDhcp(int adapter) {
    return NETWORK_SETTING_DHCP.equals(getNicSetting(adapter));
  }

  public boolean isAdapterConfiguredManually(int adapter) {
    return NETWORK_SETTING_MANUAL.equals(getNicSetting(adapter));
  }

  public boolean isAdapterConfiguredByDatabase(int i) {
    return NETWORK_SETTING_DATABASE.equals(getNicSetting(i));
  }

  public String getNicSetting(int adapter) {
    switch (adapter) {
      case 1:
        return getServiceSettingValidated(TS_NIC1_NETWORK_SETTINGS);
      case 2:
        return getServiceSettingValidated(TS_NIC2_NETWORK_SETTINGS);
      case 3:
        return getServiceSettingValidated(TS_NIC3_NETWORK_SETTINGS);
      case 4:
        return getServiceSettingValidated(TS_NIC4_NETWORK_SETTINGS);
      default:
        throw new IllegalArgumentException(
            "NIC identifier " + adapter + " is out of range. Valid range is [1-4].");
    }
  }

  /**
   * Determines the network adapter for the given NIC.
   *
   * @param i NIC identifier
   */
  String getVLAN(int i) {
    String vlan = null;

    switch (i) {
      case 1:
        vlan = getServiceSetting(TS_NIC1_NETWORK_ADAPTER);
        break;
      case 2:
        vlan = getServiceSetting(TS_NIC2_NETWORK_ADAPTER);
        break;
      case 3:
        vlan = getServiceSetting(TS_NIC3_NETWORK_ADAPTER);
        break;
      case 4:
        vlan = getServiceSetting(TS_NIC4_NETWORK_ADAPTER);
        break;
      default:
        throw new IllegalArgumentException(
            "NIC identifier " + i + " is out of range. Valid range is [1-4].");
    }

    return vlan;
  }

  /**
   * Get the gateway IP addresses for the given NIC. The IP addresses are comma separated.
   *
   * @param i NIC identifier
   * @return a comma separated list of gateway IP addresses
   * @exception IllegalArgumentException if identifier is out of range
   * @exception RuntimeException if the technical service parameter is not defined
   */
  public String getGateway(int i) {
    String gateway = null;
    switch (i) {
      case 1:
        gateway = getServiceSettingValidated(TS_NIC1_GATEWAY);
        break;
      case 2:
        gateway = getServiceSettingValidated(TS_NIC2_GATEWAY);
        break;
      case 3:
        gateway = getServiceSettingValidated(TS_NIC3_GATEWAY);
        break;
      case 4:
        gateway = getServiceSettingValidated(TS_NIC4_GATEWAY);
        break;
      default:
        throw new IllegalArgumentException(
            "NIC identifier " + i + " is out of range. Valid range is [1-4].");
    }

    return gateway;
  }

  /**
   * Get the IP address of the given NIC.
   *
   * @param adapter NIC identifier
   * @return the IP address
   * @exception IllegalArgumentException if identifier is out of range
   * @exception RuntimeException if the technical service parameter is not defined
   */
  public String getIpAddress(int adapter) {
    switch (adapter) {
      case 1:
        return getServiceSetting(TS_NIC1_IP_ADDRESS);
      case 2:
        return getServiceSetting(TS_NIC2_IP_ADDRESS);
      case 3:
        return getServiceSetting(TS_NIC3_IP_ADDRESS);
      case 4:
        return getServiceSetting(TS_NIC4_IP_ADDRESS);
      default:
        throw new IllegalArgumentException(
            "NIC identifier " + adapter + " is out of range. Valid range is [1-4].");
    }
  }

  /**
   * Get the network adapter (VLAN) for the given NIC.
   *
   * @param i NIC identifier, i=[1,4]
   * @return the name of the network adapter
   * @exception IllegalArgumentException if identifier is out of range
   */
  public String getNetworkAdapter(int i) {
    String adapter = "";
    switch (i) {
      case 1:
        adapter = getServiceSetting(TS_NIC1_NETWORK_ADAPTER);
        break;
      case 2:
        adapter = getServiceSetting(TS_NIC2_NETWORK_ADAPTER);
        break;
      case 3:
        adapter = getServiceSetting(TS_NIC3_NETWORK_ADAPTER);
        break;
      case 4:
        adapter = getServiceSetting(TS_NIC4_NETWORK_ADAPTER);
        break;
      default:
        throw new IllegalArgumentException(
            "NIC identifier " + i + " is out of range. Valid range is [1-4].");
    }

    if (adapter == null) {
      adapter = "";
    }

    return adapter;
  }

  /**
   * Get the port group for the given NIC.
   *
   * @param i NIC identifier, i=[1,4]
   * @return the UUID of the port group
   * @exception IllegalArgumentException if identifier is out of range
   */
  public String getPortGroup(int i) {
    String group = "";
    switch (i) {
      case 1:
        group = getServiceSetting(TS_NIC1_PORTGROUP);
        break;
      case 2:
        group = getServiceSetting(TS_NIC2_PORTGROUP);
        break;
      case 3:
        group = getServiceSetting(TS_NIC3_PORTGROUP);
        break;
      case 4:
        group = getServiceSetting(TS_NIC4_PORTGROUP);
        break;
      default:
        throw new IllegalArgumentException(
            "NIC identifier " + i + " is out of range. Valid range is [1-4].");
    }

    if (group == null) {
      group = "";
    }

    return group;
  }

  /**
   * Get the distributed switch UUID for the given NIC.
   *
   * @param i NIC identifier, i=[1,4]
   * @return the UUID of the port group
   * @exception IllegalArgumentException if identifier is out of range
   */
  public String getSwitchUUID(int i) {
    String switchUUID = "";
    switch (i) {
      case 1:
        switchUUID = getServiceSetting(TS_NIC1_SWITCH_UUID);
        break;
      case 2:
        switchUUID = getServiceSetting(TS_NIC2_SWITCH_UUID);
        break;
      case 3:
        switchUUID = getServiceSetting(TS_NIC3_SWITCH_UUID);
        break;
      case 4:
        switchUUID = getServiceSetting(TS_NIC4_SWITCH_UUID);
        break;
      default:
        throw new IllegalArgumentException(
            "NIC identifier " + i + " is out of range. Valid range is [1-4].");
    }

    if (switchUUID == null) {
      switchUUID = "";
    }

    return switchUUID;
  }

  /**
   * Get a comma separated list of DNS servers for the given NIC.
   *
   * @param i NIC identifier, range [1..4]
   * @return a comma separated list of DNS servers
   * @exception IllegalArgumentException if identifier is out of range
   * @exception RuntimeException if the technical service parameter is not defined
   */
  public String getDNSServer(int i) {
    String dnsserver = "";
    switch (i) {
      case 1:
        dnsserver = getServiceSetting(TS_NIC1_DNS_SERVER);
        break;
      case 2:
        dnsserver = getServiceSetting(TS_NIC2_DNS_SERVER);
        break;
      case 3:
        dnsserver = getServiceSetting(TS_NIC3_DNS_SERVER);
        break;
      case 4:
        dnsserver = getServiceSetting(TS_NIC4_DNS_SERVER);
        break;
      default:
        throw new IllegalArgumentException(
            "NIC identifier " + i + " is out of range. Valid range is [1-4].");
    }

    if (dnsserver == null) {
      dnsserver = "";
    }

    return dnsserver;
  }

  /**
   * Get a comma separated list of DNS suffixes for the given NIC.
   *
   * @param i NIC identifier, range [1..4]
   * @return a comma separated list of DNS suffixes
   * @exception IllegalArgumentException if identifier is out of range
   * @exception RuntimeException if the technical service parameter is not defined
   */
  public String getDNSSuffix(int i) {
    String dnssuffix = "";
    switch (i) {
      case 1:
        dnssuffix = getServiceSetting(TS_NIC1_DNS_SUFFIX);
        break;
      case 2:
        dnssuffix = getServiceSetting(TS_NIC2_DNS_SUFFIX);
        break;
      case 3:
        dnssuffix = getServiceSetting(TS_NIC3_DNS_SUFFIX);
        break;
      case 4:
        dnssuffix = getServiceSetting(TS_NIC4_DNS_SUFFIX);
        break;
      default:
        throw new IllegalArgumentException(
            "NIC identifier " + i + " is out of range. Valid range is [1-4].");
    }

    if (dnssuffix == null) {
      dnssuffix = "";
    }
    return dnssuffix;
  }

  /**
   * Get the subnet mask for the given NIC.
   *
   * @param i NIC identifier
   * @return the subnet mask
   * @exception IllegalArgumentException if identifier is out of range
   * @exception RuntimeException if the technical service parameter is not defined
   */
  public String getSubnetMask(int i) {
    String subnetmask = null;
    switch (i) {
      case 1:
        subnetmask = getServiceSettingValidated(TS_NIC1_SUBNET_MASK);
        break;
      case 2:
        subnetmask = getServiceSettingValidated(TS_NIC2_SUBNET_MASK);
        break;
      case 3:
        subnetmask = getServiceSettingValidated(TS_NIC3_SUBNET_MASK);
        break;
      case 4:
        subnetmask = getServiceSettingValidated(TS_NIC4_SUBNET_MASK);
        break;
      default:
        throw new IllegalArgumentException(
            "NIC identifier " + i + " is out of range. Valid range is [1-4].");
    }

    return subnetmask;
  }

  public SubscriptionEndStatus getSubscriptionEndStatus() {
    SubscriptionEndStatus state = SubscriptionEndStatus.UNDEFINED;
    String status = getServiceSetting(VMPropertyHandler.SUBSCRIPTION_END_STATUS);
    logger.debug("status: " + status);
    if (status != null && status.equals(SubscriptionEndStatus.SCHEDULED_FOR_DEACTIVATION.name())) {
      state = SubscriptionEndStatus.SCHEDULED_FOR_DEACTIVATION;
    } else if (status != null
        && status.equals(SubscriptionEndStatus.SCHEDULED_FOR_DELETION.name())) {
      state = SubscriptionEndStatus.SCHEDULED_FOR_DELETION;
    } else if (status != null
        && status.equals(SubscriptionEndStatus.SCHEDULED_FOR_NOTIFICATION.name())) {
      state = SubscriptionEndStatus.SCHEDULED_FOR_NOTIFICATION;
    }

    return state;
  }

  /**
   * Is called from executeOperation and is not storing settings. So therefore the status is written
   * directly to the database.
   */
  public void setSubscriptionEndStatus(String instanceId, SubscriptionEndStatus status)
      throws Exception {

    APPDataAccessService das = new APPDataAccessService();
    das.setSubscriptionEndStatus(instanceId, status);
  }

  public void setSubscriptionEndStatus(SubscriptionEndStatus status) {
    setValue(SUBSCRIPTION_END_STATUS, status.name(), settings.getParameters());
  }

  /**
   * Find the state that was executed before the given state. The given state can occur several
   * times in the state history.
   *
   * @param state
   * @return the state that was executed before the given state
   * @exception if previous state does not exist
   */
  public String getPreviousStateFromHistory(VMPropertyHandler ph, String state)
      throws APPlatformException {
    logger.debug("state: " + state);
    String previousState = null;
    String stateHistory = getServiceSetting(SM_STATE_HISTORY);
    String[] states = stateHistory.split(",");
    for (int i = states.length - 1; i >= 0; i--) {
      if (!states[i].equals(state)) {
        previousState = states[i];
        break;
      }
    }

    if (previousState == null) {
      String message =
          Messages.get(ph.getLocale(), "error_no_previous_state", new Object[] {state});
      throw new APPlatformException(message);
    }

    logger.debug("previousState: " + previousState);
    return previousState;
  }

  private boolean notNullNorEmpty(String value) {
    return value != null && value.trim().length() > 0;
  }

  /** Returns a controller setting or NULL of not defined */
  public String getControllerSetting(String key) {
    return getValue(key, settings.getConfigSettings());
  }

  /** Returns a service setting. If not set a RuntimeException is thrown. */
  public String getServiceSettingValidated(String key) {
    String value = getValue(key, settings.getParameters());
    if (value == null) {
      String message = String.format("No value set for property '%s'", key);
      throw new RuntimeException(message);
    }
    return value;
  }

  /** Returns a service setting or NULL if not set */
  public String getServiceSetting(String key) {
    return getValue(key, settings.getParameters());
  }

  public void useMock(DataAccessService das) {
    das_stub = das;
  }

  public DataAccessService getDataAccessService() {
    if (das_stub != null) {
      return das_stub;
    } else {
      return new DataAccessService(getLocale());
    }
  }

  private String getValue(String key, Map<String, Setting> source) {
    Setting setting = source.get(key);
    return setting != null ? setting.getValue() : null;
  }

  private void setValue(String key, String value, Map<String, Setting> target) {
    target.put(key, new Setting(key, value));
  }
}
