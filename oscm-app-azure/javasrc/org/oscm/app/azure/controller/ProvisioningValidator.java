/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 2016-07-29
 *
 *******************************************************************************/
package org.oscm.app.azure.controller;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.oscm.app.azure.AzureCommunication;
import org.oscm.app.azure.i18n.Messages;
import org.oscm.app.v1_0.exceptions.APPlatformException;

public class ProvisioningValidator {

    /**
     * Logger.
     */
    private static final Logger logger = LoggerFactory
            .getLogger(ProvisioningValidator.class);

    /**
     * @param ph a property handler for reading and writing service parameters
     *           and controller configuration settings
     * @throws APPlatformException
     */
    public static void validateParameters(PropertyHandler ph, AzureCommunication azureComm)
            throws APPlatformException {
        String resourceGroupName = ph.getResourceGroupName();
        if (StringUtils.isBlank(resourceGroupName)) {
            throw new APPlatformException(Messages.getAll("error_invalid_name",
                    new Object[]{resourceGroupName}));
        }
        String regex = "^[^_\\W][\\w-._]{1,29}";
        Matcher m = Pattern.compile(regex).matcher(resourceGroupName);
        if (!m.matches()) {
            logger.error("Validation error on resource group name: ["
                    + resourceGroupName + "/" + regex + "]");
            throw new APPlatformException(Messages.getAll("error_invalid_name",
                    new Object[]{resourceGroupName}));
        }

        String region = ph.getRegion();
        List<String> regions = azureComm.getAvailableRegions();
        if (regions.indexOf(region) < 0) {
            logger.error("Validation error on region: [" + region + "/"
                    + StringUtils.join(regions, ", ") + "]");
            throw new APPlatformException(Messages.getAll(
                    "error_invalid_region", new Object[]{region}));
        }
    }

    /**
     * @param ph a property handler for reading and writing service parameters
     *           and controller configuration settings
     * @throws APPlatformException     if a general problem occurs in accessing APP
     */
    public static void validateTimeout(PropertyHandler ph) throws APPlatformException {
        if (ph.getStartTime() == null) {
            throw new RuntimeException(
                    "Controller must be set the start time at the beginning of the request");
        } else if ("suspended".equals(ph.getStartTime())) {
            logger.debug("Resume request, reset start time");
            ph.setStartTime(String.valueOf(System.currentTimeMillis()));
            return;
        }
    }
}
