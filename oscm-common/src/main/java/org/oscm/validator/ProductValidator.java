/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 31.07.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.validator;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.converter.TimeStampUtil;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.exception.ServiceStateException;

/**
 * Contains utility methods for validating Product domain model objects.
 * 
 * @author barzu
 */
public class ProductValidator {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(ProductValidator.class);

    /**
     * Checks if the given product state is {@link ServiceStatus#INACTIVE} or
     * {@link ServiceStatus#SUSPENDED} or {@link ServiceStatus#ACTIVE}
     * 
     * @param productId
     *            the ID of the product to be validated
     * @param status
     *            the product state to be checked
     * @throws ServiceStateException
     *             in case the state is not {@link ServiceStatus#INACTIVE} or
     *             {@link ServiceStatus#SUSPENDED} or
     *             {@link ServiceStatus#ACTIVE}
     */
    public static void validateActiveOrInactiveOrSuspended(String productId,
            ServiceStatus status) throws ServiceStateException {
        if (status != ServiceStatus.INACTIVE
                && status != ServiceStatus.SUSPENDED
                && status != ServiceStatus.ACTIVE) {
            String expected = "[" + ServiceStatus.ACTIVE.name()
                    + ServiceStatus.INACTIVE.name() + ", "
                    + ServiceStatus.SUSPENDED.name() + "]";
            ServiceStateException e = new ServiceStateException(status,
                    expected,
                    TimeStampUtil.removeTimestampFromId(productId));
            logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.WARN_INVALID_SERVICE_STATUS, expected,
                    status.name(), productId);
            throw e;
        }
    }

    /**
     * Checks if the given product state is {@link ServiceStatus#INACTIVE} or
     * {@link ServiceStatus#SUSPENDED}
     * 
     * @param productId
     *            the ID of the product to be validated
     * @param status
     *            the product state to be checked
     * @throws ServiceStateException
     *             in case the state is not {@link ServiceStatus#INACTIVE} or
     *             {@link ServiceStatus#SUSPENDED}
     */
    public static void validateInactiveOrSuspended(String productId,
            ServiceStatus status) throws ServiceStateException {
        if (status != ServiceStatus.INACTIVE
                && status != ServiceStatus.SUSPENDED) {
            String expected = "[" + ServiceStatus.INACTIVE.name() + ", "
                    + ServiceStatus.SUSPENDED.name() + "]";
            ServiceStateException e = new ServiceStateException(status,
                    expected,
                    TimeStampUtil.removeTimestampFromId(productId));
            logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.WARN_INVALID_SERVICE_STATUS, expected,
                    status.name(), productId);
            throw e;
        }
    }

    /**
     * Checks if the resale permission was removed for the given product.
     * 
     * @param productId
     *            the ID of the product to be validated
     * @param status
     *            the product state to be checked
     * @throws ServiceStateException
     *             in case the resale permission was removed for the specified
     *             product.
     */
    public static void validateResalePermission(String productId,
            ServiceStatus status) throws ServiceStateException {
        if (status == null) {
            throw new NullPointerException("status is not allowed to be null");
        }
        if (status == ServiceStatus.DELETED) {
            String expected = "[" + ServiceStatus.INACTIVE.name() + ", "
                    + ServiceStatus.SUSPENDED.name() + "]";
            ServiceStateException e = new ServiceStateException(status,
                    expected, productId);
            logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.WARN_INVALID_SERVICE_STATUS, expected,
                    status.name(), productId);
            throw e;
        }
    }

    /**
     * Checks if the given product state is {@link ServiceStatus#INACTIVE} or
     * {@link ServiceStatus#ACTIVE}
     * 
     * @param productId
     *            the ID of the product to be validated
     * @param status
     *            the product state to be checked
     * @throws ServiceStateException
     *             in case the state is not {@link ServiceStatus#INACTIVE} or
     *             {@link ServiceStatus#ACTIVE}
     */
    public static void validateInactiveOrActive(String productId,
            ServiceStatus status) throws ServiceStateException {
        if (status != ServiceStatus.INACTIVE && status != ServiceStatus.ACTIVE) {
            String expected = "[" + ServiceStatus.INACTIVE.name() + ", "
                    + ServiceStatus.ACTIVE.name() + "]";
            ServiceStateException e = new ServiceStateException(status,
                    expected, productId);
            logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.WARN_INVALID_SERVICE_STATUS, expected,
                    status.name(), productId);
            throw e;
        }
    }

}
