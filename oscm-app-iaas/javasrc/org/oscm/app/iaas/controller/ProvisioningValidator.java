/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 01.03.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.iaas.controller;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.oscm.app.iaas.PropertyHandler;
import org.oscm.app.iaas.SubPropertyHandler;
import org.oscm.app.iaas.i18n.Messages;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.intf.APPlatformService;

public abstract class ProvisioningValidator {

    private static final Logger logger = LoggerFactory
            .getLogger(ProvisioningValidator.class);

    public abstract void validateDiskName(PropertyHandler paramHandler)
            throws APPlatformException;

    public void validateExistingInstance(APPlatformService platformService,
            String controllerID, PropertyHandler paramHandler)
            throws APPlatformException {
        String instanceName = paramHandler.getInstanceName();
        if (platformService.exists(controllerID, instanceName)) {
            logger.error("Other instance with same name already registered: ["
                    + instanceName + "]");
            throw new APPlatformException(Messages.getAll(
                    "error_instance_exists", new Object[] { instanceName }));
        }
    }

    public void validateInstanceName(PropertyHandler paramHandler)
            throws APPlatformException {
        String regex = paramHandler.getInstanceNamePattern();
        String instanceName = paramHandler.getInstanceName();
        if (isNullOrEmpty(instanceName)) {
            throw new APPlatformException(Messages.getAll("error_invalid_name",
                    new Object[] { instanceName }));
        }
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(instanceName);
        if (!m.matches()) {
            logger.error("Validation error on instance name: [" + instanceName
                    + "/" + regex + "]");
            throw new APPlatformException(Messages.getAll("error_invalid_name",
                    new Object[] { instanceName }));
        }
    }

    /**
     * Validates the given parameters before contacting the IaaS API. When both
     * oldParams and newParams are set, the network, server disk image and name
     * are checked.
     * 
     * @param oldParams
     *            the existing parameters (optional)
     * @param newParams
     *            the requested parameters
     * @throws APPlatformException
     *             thrown when validation fails
     */
    public void validateParametersForVserverProvisioning(
            PropertyHandler oldParams, PropertyHandler newParams,
            boolean isAdditionalDiskSelected) throws APPlatformException {
        String vsysId = newParams.getVsysId();
        if (isNullOrEmpty(vsysId)) {
            throw new APPlatformException(Messages.getAll(
                    "error_invalid_sysid", new Object[] { vsysId }));
        }
        String vserverType = newParams.getVserverType();
        if (isNullOrEmpty(vserverType)) {
            throw new APPlatformException(Messages.getAll(
                    "error_invalid_servertype", new Object[] { vserverType }));
        }
        String diskImageId = newParams.getDiskImageId();
        if (isNullOrEmpty(diskImageId)) {
            throw new APPlatformException(Messages.getAll(
                    "error_invalid_diskimageid", new Object[] { diskImageId }));
        }
        String networkId = newParams.getNetworkId();
        if (isNullOrEmpty(networkId)) {
            throw new APPlatformException(Messages.getAll(
                    "error_invalid_networkid", new Object[] { networkId }));
        }
        if (isAdditionalDiskSelected) {
            // throw exception after validating name
            // otherwise set the disk name as mandatory in technical service
            // xml
            String diskName = newParams.getVDiskNameCustom();
            if (diskName == null) {
                throw new APPlatformException(Messages.getAll(
                        "error_invalid_vdiskname", new Object[] { diskName }));
            }
        }
        // permit changing the host name (not yet supported)
        if (oldParams != null) {
            if (!oldParams.getInstanceName()
                    .equals(newParams.getInstanceName())) {
                logger.debug("Unsupported instance name modification ("
                        + newParams.getInstanceName() + ")");
                throw new APPlatformException(
                        Messages.getAll("error_invalid_hostrename"));
            }
            // permit changing the sever disk image (not yet supported)
            if (!oldParams.getDiskImageId().equals(newParams.getDiskImageId())) {
                logger.debug("Unsupported disk image modification ("
                        + newParams.getDiskImageId() + ")");
                throw new APPlatformException(
                        Messages.getAll("error_invalid_diskimagerename"));
            }
            // permit changing the sever network (not yet supported)
            if (!oldParams.getNetworkId().equals(newParams.getNetworkId())) {
                logger.debug("Unsupported network modification ("
                        + newParams.getNetworkId() + ")");
                throw new APPlatformException(
                        Messages.getAll("error_invalid_networkrename"));
            }
            if (isAdditionalDiskSelected) {
                // permit changing the disk name after creation(not yet
                // supported), otherwise yes
                if ((oldParams.getVDiskNameCustom() != null)) {
                    if ((oldParams.getVDiskNameCustom().length() != 0)) {
                        if (!oldParams.getVDiskNameCustom().equals(
                                newParams.getVDiskName())) {
                            logger.debug("Unsupported disk name modification ("
                                    + newParams.getVDiskName() + ")");
                            throw new APPlatformException(
                                    Messages.getAll("error_invalid_diskrename"));
                        }
                    }
                }
                // permit changing the disk size (only up gradation and
                // degradation not supported)
                if (!oldParams.getVDiskSize().equals(newParams.getVDiskSize())) {
                    logger.debug("Unsupported disk size modification ("
                            + newParams.getVDiskSize() + ")");
                    int size = Integer.parseInt(oldParams.getVDiskSize());
                    if (size != 0) {
                        int sizeNew = Integer
                                .parseInt(newParams.getVDiskSize());
                        if (sizeNew != 0) {
                            throw new APPlatformException(
                                    Messages.getAll("error_invalid_diskresize"));
                        }
                    }
                }
            }
        }
    }

    /**
     * Validates the given parameters for virtual system provisioning before
     * contacting IAAS API. When both oldParams and newParams are set, the
     * naming change rules are checked.
     * 
     * @param oldParams
     *            the existing parameters (optional)
     * @param newParams
     *            the requested parameters
     * @throws APPlatformException
     *             thrown when validation fails
     */
    public void validateParametersForVsysProvisioning(
            PropertyHandler oldParams, PropertyHandler newParams)
            throws APPlatformException {
        String templateId = newParams.getSystemTemplateId();
        if (isNullOrEmpty(templateId)) {
            throw new APPlatformException(Messages.getAll(
                    "error_invalid_templateid", new Object[] { templateId }));
        }
        if (newParams.isClusterDefined()) {
            String masterDiskImageId = newParams.getMasterTemplateId();
            if (isNullOrEmpty(masterDiskImageId)) {
                throw new APPlatformException(Messages.getAll(
                        "error_invalid_masterdiskimageid",
                        new Object[] { masterDiskImageId }));
            }
            String slaveDiskImageId = newParams.getSlaveTemplateId();
            if (isNullOrEmpty(slaveDiskImageId)) {
                throw new APPlatformException(Messages.getAll(
                        "error_invalid_slavediskimageid",
                        new Object[] { slaveDiskImageId }));
            }
            String clusterSize = newParams.getClusterSize();
            if (isNullOrEmpty(clusterSize) || !isNumeric(clusterSize)) {
                throw new APPlatformException(Messages.getAll(
                        "error_invalid_clustersize",
                        new Object[] { clusterSize }));
            }
        }
        // permit changing the virtual system name (not yet supported)
        if (oldParams != null) {
            String oldInstanceName = oldParams.getInstanceName();
            String newInstanceNameForCustom = newParams
                    .getInstanceNameIfExists();
            String newInstanceNamePrefix = newParams.getInstanceNamePrefix();
            String newInstanceName = newParams.getInstanceName();
            if (isNullOrEmpty(newInstanceNameForCustom)
                    && isNullOrEmpty(newInstanceNamePrefix)) {
                newInstanceName = oldInstanceName;
            }
            if (!oldInstanceName.equals(newInstanceName)) {
                logger.debug("Unsupported instance name modification ("
                        + newInstanceName + ")");
                throw new APPlatformException(
                        Messages.getAll("error_invalid_vsysrename"));
            }

            // don't permit changing the cluster setup
            if (oldParams.isClusterDefined() != newParams.isClusterDefined()) {
                logger.debug("Unsupported change of cluster configuration (adding/removing of parameters");
                throw new APPlatformException(
                        Messages.getAll("error_invalid_clusterchange"));
            }
            if (oldParams.isClusterDefined()) {
                // don't permit changing the master server disk image
                if (!oldParams.getMasterTemplateId().equals(
                        newParams.getMasterTemplateId())) {
                    logger.debug("Unsupported master disk image modification ("
                            + newParams.getMasterTemplateId() + ")");
                    throw new APPlatformException(
                            Messages.getAll("error_invalid_masterdiskimagerename"));
                }
                // don't permit changing the slave server disk image
                if (!oldParams.getSlaveTemplateId().equals(
                        newParams.getSlaveTemplateId())) {
                    logger.debug("Unsupported slave disk image modification ("
                            + newParams.getSlaveTemplateId() + ")");
                    throw new APPlatformException(
                            Messages.getAll("error_invalid_slavediskimagerename"));
                }
            }

        }
    }

    /**
     * Validates the syntax of the firewall configuration.
     */
    public void validateFirewallConfiguration(PropertyHandler paramHandler)
            throws APPlatformException {

        try {
            // Simply call get method (will throw exception in case of
            // exceptions)
            paramHandler.getFirewallPolicies();

            // Also check all subsystem configurations
            SubPropertyHandler[] vsList = paramHandler.getVserverList();
            for (SubPropertyHandler vsys : vsList) {
                vsys.getFirewallPolicies();
            }

        } catch (Exception ex) {
            throw new APPlatformException(ex.getMessage(), ex);
        }
    }

    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    private boolean isNullOrEmpty(String value) {
        return value == null || value.trim().length() == 0;
    }
}
