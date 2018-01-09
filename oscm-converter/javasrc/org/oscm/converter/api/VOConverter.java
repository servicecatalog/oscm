/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.converter.api;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Query;

import org.oscm.converter.ParameterizedTypes;
import org.oscm.converter.strategy.ConversionStrategy;
import org.oscm.converter.strategy.TPPConversionStrategyFactory;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.LocalizedResource;
import org.oscm.domobjects.Uda;
import org.oscm.domobjects.UdaDefinition;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.OperationParameterType;
import org.oscm.types.enumtypes.TriggerProcessParameterType;
import org.oscm.vo.VOUdaDefinition;

public class VOConverter {

    private static final String METHOD_CONVERT_TO_API = "convertToApi";
    private static final String METHOD_CONVERT_TO_INTERNAL = "convertToUp";
    private static final String VERSION_INTERNAL = "internal";
    private static final Log4jLogger LOGGER = LoggerFactory
            .getLogger(VOConverter.class);
    private static DataService ds;

    @SuppressWarnings("unchecked")
    public static <T, U> T reflectiveConvert(U objectToConvert) {

        return reflectiveConvert(objectToConvert, null);
    }

    @SuppressWarnings("unchecked")
    public static <T, U> T reflectiveConvert(U objectToConvert,
            DataService ds) {
        VOConverter.ds = ds;
        String methodName;
        if (objectToConvert.getClass().getPackage().getName()
                .contains(VERSION_INTERNAL)) {
            methodName = METHOD_CONVERT_TO_API;
        } else {
            methodName = METHOD_CONVERT_TO_INTERNAL;
        }

        Method m;
        try {
            m = VOConverter.class.getDeclaredMethod(methodName,
                    objectToConvert.getClass());
            return (T) m.invoke(VOConverter.class, objectToConvert);
        } catch (Exception e) {
            org.oscm.internal.types.exception.SaaSSystemException exc = new org.oscm.internal.types.exception.SaaSSystemException(
                    e);
            LOGGER.logError(Log4jLogger.SYSTEM_LOG, exc,
                    org.oscm.types.enumtypes.LogMessageIdentifier.ERROR_VO_CONVERSION_1_6);
            throw exc;
        }
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.internal.vo.VOMarketplace convertToUp(
            org.oscm.vo.VOMarketplace oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.internal.vo.VOMarketplace newVO = new org.oscm.internal.vo.VOMarketplace();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setCategoriesEnabled(oldVO.isCategoriesEnabled());
        newVO.setMarketplaceId(oldVO.getMarketplaceId());
        newVO.setName(oldVO.getName());
        newVO.setOpen(oldVO.isOpen());
        newVO.setOwningOrganizationId(oldVO.getOwningOrganizationId());
        newVO.setOwningOrganizationName(oldVO.getOwningOrganizationName());
        newVO.setReviewEnabled(oldVO.isReviewEnabled());
        newVO.setSocialBookmarkEnabled(oldVO.isSocialBookmarkEnabled());
        newVO.setTaggingEnabled(oldVO.isTaggingEnabled());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.vo.VOMarketplace convertToApi(
            org.oscm.internal.vo.VOMarketplace oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.vo.VOMarketplace newVO = new org.oscm.vo.VOMarketplace();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setCategoriesEnabled(oldVO.isCategoriesEnabled());
        newVO.setMarketplaceId(oldVO.getMarketplaceId());
        newVO.setName(oldVO.getName());
        newVO.setOpen(oldVO.isOpen());
        newVO.setOwningOrganizationId(oldVO.getOwningOrganizationId());
        newVO.setOwningOrganizationName(oldVO.getOwningOrganizationName());
        newVO.setReviewEnabled(oldVO.isReviewEnabled());
        newVO.setSocialBookmarkEnabled(oldVO.isSocialBookmarkEnabled());
        newVO.setTaggingEnabled(oldVO.isTaggingEnabled());
        return newVO;
    }

    /**
     * Convert list of LdapProperties.
     * 
     * @param oldVO
     *            List of VO to convert.
     * @return Converted list of VO.
     */
    public static List<org.oscm.internal.vo.VOCatalogEntry> convertToUpVOCatalogEntry(
            List<org.oscm.vo.VOCatalogEntry> oldVO) {
        if (oldVO == null) {
            return null;
        }
        List<org.oscm.internal.vo.VOCatalogEntry> newVO = new ArrayList<>();
        for (org.oscm.vo.VOCatalogEntry tmp : oldVO) {
            newVO.add(convertToUp(tmp));
        }
        return newVO;
    }

    /**
     * Convert list of LdapProperties.
     * 
     * @param oldVO
     *            List of VO to convert.
     * @return Converted list of VO.
     */
    public static List<org.oscm.vo.VOCatalogEntry> convertToApiVOCatalogEntry(
            List<org.oscm.internal.vo.VOCatalogEntry> oldVO) {
        if (oldVO == null) {
            return null;
        }
        List<org.oscm.vo.VOCatalogEntry> newVO = new ArrayList<>();
        for (org.oscm.internal.vo.VOCatalogEntry tmp : oldVO) {
            newVO.add(convertToApi(tmp));
        }
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.internal.vo.LdapProperties convertToUp(
            org.oscm.vo.LdapProperties oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.internal.vo.LdapProperties newVO = new org.oscm.internal.vo.LdapProperties();
        newVO.setSettings(convertToUpSetting(oldVO.getSettings()));
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.vo.LdapProperties convertToApi(
            org.oscm.internal.vo.LdapProperties oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.vo.LdapProperties newVO = new org.oscm.vo.LdapProperties();
        newVO.setSettings(convertToApiSetting(oldVO.getSettings()));
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.vo.VOServiceFeedback convertToApi(
            org.oscm.internal.vo.VOServiceFeedback oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.vo.VOServiceFeedback newVO = new org.oscm.vo.VOServiceFeedback();
        newVO.setReviews(convertToApiVOServiceReview(oldVO.getReviews()));
        newVO.setAverageRating(oldVO.getAverageRating());
        newVO.setAllowedToWriteReview(oldVO.isAllowedToWriteReview());
        newVO.setServiceKey(oldVO.getServiceKey());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.internal.vo.VOServiceFeedback convertToUp(
            org.oscm.vo.VOServiceFeedback oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.internal.vo.VOServiceFeedback newVO = new org.oscm.internal.vo.VOServiceFeedback();
        newVO.setReviews(convertToUpVOServiceReview(oldVO.getReviews()));
        newVO.setAverageRating(oldVO.getAverageRating());
        newVO.setAllowedToWriteReview(oldVO.isAllowedToWriteReview());
        newVO.setServiceKey(oldVO.getServiceKey());
        return newVO;
    }

    /**
     * Convert list of VOServiceReview.
     * 
     * @param oldVO
     *            List of VO to convert.
     * @return Converted list of VO.
     */
    public static List<org.oscm.vo.VOServiceReview> convertToApiVOServiceReview(
            List<org.oscm.internal.vo.VOServiceReview> oldVO) {
        if (oldVO == null) {
            return null;
        }
        List<org.oscm.vo.VOServiceReview> newVO = new ArrayList<>();
        for (org.oscm.internal.vo.VOServiceReview tmp : oldVO) {
            newVO.add(convertToApi(tmp));
        }
        return newVO;
    }

    /**
     * Convert list of VOServiceReview.
     * 
     * @param oldVO
     *            List of VO to convert.
     * @return Converted list of VO.
     */
    public static List<org.oscm.internal.vo.VOServiceReview> convertToUpVOServiceReview(
            List<org.oscm.vo.VOServiceReview> oldVO) {
        if (oldVO == null) {
            return null;
        }
        List<org.oscm.internal.vo.VOServiceReview> newVO = new ArrayList<>();
        for (org.oscm.vo.VOServiceReview tmp : oldVO) {
            newVO.add(convertToUp(tmp));
        }
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.internal.vo.VOVatRate convertToUp(
            org.oscm.vo.VOVatRate oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.internal.vo.VOVatRate newVO = new org.oscm.internal.vo.VOVatRate();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setRate(oldVO.getRate());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.vo.VOVatRate convertToApi(
            org.oscm.internal.vo.VOVatRate oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.vo.VOVatRate newVO = new org.oscm.vo.VOVatRate();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setRate(oldVO.getRate());
        return newVO;
    }

    /**
     * Convert list of Setting.
     * 
     * @param oldVO
     *            List of VO to convert.
     * @return Converted list of VO.
     */
    public static Set<org.oscm.internal.vo.Setting> convertToUpSetting(
            Set<org.oscm.vo.Setting> oldVO) {
        if (oldVO == null) {
            return null;
        }
        Set<org.oscm.internal.vo.Setting> newVO = new HashSet<>();
        for (org.oscm.vo.Setting tmp : oldVO) {
            newVO.add(convertToUp(tmp));
        }
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.internal.vo.Setting convertToUp(
            org.oscm.vo.Setting oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.internal.vo.Setting newVO = new org.oscm.internal.vo.Setting();
        newVO.setKey(oldVO.getKey());
        newVO.setValue(oldVO.getValue());
        return newVO;
    }

    /**
     * Convert list of Setting.
     * 
     * @param oldVO
     *            List of VO to convert.
     * @return Converted list of VO.
     */
    public static Set<org.oscm.vo.Setting> convertToApiSetting(
            Set<org.oscm.internal.vo.Setting> oldVO) {
        if (oldVO == null) {
            return null;
        }
        Set<org.oscm.vo.Setting> newVO = new HashSet<>();
        for (org.oscm.internal.vo.Setting tmp : oldVO) {
            newVO.add(convertToApi(tmp));
        }
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.vo.Setting convertToApi(
            org.oscm.internal.vo.Setting oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.vo.Setting newVO = new org.oscm.vo.Setting();
        newVO.setKey(oldVO.getKey());
        newVO.setValue(oldVO.getValue());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.internal.vo.VOPriceModelLocalization convertToUp(
            org.oscm.vo.VOPriceModelLocalization oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.internal.vo.VOPriceModelLocalization newVO = new org.oscm.internal.vo.VOPriceModelLocalization();
        newVO.setDescriptions(
                convertToUpVOLocalizedText(oldVO.getDescriptions()));
        newVO.setLicenses(convertToUpVOLocalizedText(oldVO.getLicenses()));
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.vo.VOPriceModelLocalization convertToApi(
            org.oscm.internal.vo.VOPriceModelLocalization oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.vo.VOPriceModelLocalization newVO = new org.oscm.vo.VOPriceModelLocalization();
        newVO.setDescriptions(
                convertToVOLocalizedText(oldVO.getDescriptions()));
        newVO.setLicenses(convertToVOLocalizedText(oldVO.getLicenses()));
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.internal.vo.VOUda convertToUp(
            org.oscm.vo.VOUda oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.internal.vo.VOUda newVO = new org.oscm.internal.vo.VOUda();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setUdaDefinition(convertToUp(oldVO.getUdaDefinition()));
        newVO.setTargetObjectKey(oldVO.getTargetObjectKey());
        newVO.setUdaValue(oldVO.getUdaValue());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.vo.VOUda convertToApi(
            org.oscm.internal.vo.VOUda oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.vo.VOUda newVO = new org.oscm.vo.VOUda();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setUdaDefinition(convertToApi(oldVO.getUdaDefinition()));
        newVO.setUdaValue(oldVO.getUdaValue());
        newVO.setTargetObjectKey(oldVO.getTargetObjectKey());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.internal.vo.VOSubscription convertToUp(
            org.oscm.vo.VOSubscription oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.internal.vo.VOSubscription newVO = new org.oscm.internal.vo.VOSubscription();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setServiceId(oldVO.getServiceId());
        newVO.setServiceKey(oldVO.getServiceKey());
        newVO.setActivationDate(oldVO.getActivationDate());
        newVO.setCreationDate(oldVO.getCreationDate());
        newVO.setDeactivationDate(oldVO.getDeactivationDate());
        newVO.setServiceAccessInfo(oldVO.getServiceAccessInfo());
        newVO.setServiceAccessType(EnumConverter.convert(
                oldVO.getServiceAccessType(),
                org.oscm.internal.types.enumtypes.ServiceAccessType.class));
        newVO.setServiceBaseURL(oldVO.getServiceBaseURL());
        newVO.setServiceLoginPath(oldVO.getServiceLoginPath());
        newVO.setStatus(EnumConverter.convert(oldVO.getStatus(),
                org.oscm.internal.types.enumtypes.SubscriptionStatus.class));
        newVO.setServiceInstanceId(oldVO.getServiceInstanceId());
        newVO.setTimeoutMailSent(oldVO.isTimeoutMailSent());
        newVO.setPurchaseOrderNumber(oldVO.getPurchaseOrderNumber());
        newVO.setSubscriptionId(oldVO.getSubscriptionId());
        newVO.setProvisioningProgress(oldVO.getProvisioningProgress());
        newVO.setNumberOfAssignedUsers(oldVO.getNumberOfAssignedUsers());
        newVO.setSellerName(oldVO.getSellerName());
        newVO.setTechnicalServiceOperations(
                convertToUpVOTechnicalServiceOperation(
                        oldVO.getTechnicalServiceOperations()));
        newVO.setOwnerId(oldVO.getOwnerId());
        newVO.setUnitKey(oldVO.getUnitKey());
        newVO.setUnitName(oldVO.getUnitName());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.vo.VOSubscription convertToApi(
            org.oscm.internal.vo.VOSubscription oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.vo.VOSubscription newVO = new org.oscm.vo.VOSubscription();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setServiceId(oldVO.getServiceId());
        newVO.setServiceKey(oldVO.getServiceKey());
        newVO.setActivationDate(oldVO.getActivationDate());
        newVO.setCreationDate(oldVO.getCreationDate());
        newVO.setDeactivationDate(oldVO.getDeactivationDate());
        newVO.setServiceAccessInfo(oldVO.getServiceAccessInfo());
        newVO.setServiceAccessType(
                EnumConverter.convert(oldVO.getServiceAccessType(),
                        org.oscm.types.enumtypes.ServiceAccessType.class));
        newVO.setServiceBaseURL(oldVO.getServiceBaseURL());
        newVO.setServiceLoginPath(oldVO.getServiceLoginPath());
        newVO.setStatus(EnumConverter.convert(oldVO.getStatus(),
                org.oscm.types.enumtypes.SubscriptionStatus.class));
        newVO.setServiceInstanceId(oldVO.getServiceInstanceId());
        newVO.setTimeoutMailSent(oldVO.isTimeoutMailSent());
        newVO.setPurchaseOrderNumber(oldVO.getPurchaseOrderNumber());
        newVO.setSubscriptionId(oldVO.getSubscriptionId());
        newVO.setProvisioningProgress(oldVO.getProvisioningProgress());
        newVO.setNumberOfAssignedUsers(oldVO.getNumberOfAssignedUsers());
        newVO.setSellerName(oldVO.getSellerName());
        newVO.setTechnicalServiceOperations(
                convertToApiVOTechnicalServiceOperation(
                        oldVO.getTechnicalServiceOperations()));
        newVO.setOwnerId(oldVO.getOwnerId());
        newVO.setUnitKey(oldVO.getUnitKey());
        newVO.setUnitName(oldVO.getUnitName());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.internal.vo.VOPricedRole convertToUp(
            org.oscm.vo.VOPricedRole oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.internal.vo.VOPricedRole newVO = new org.oscm.internal.vo.VOPricedRole();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setPricePerUser(oldVO.getPricePerUser());
        newVO.setRole(convertToUp(oldVO.getRole()));
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.vo.VOPricedRole convertToApi(
            org.oscm.internal.vo.VOPricedRole oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.vo.VOPricedRole newVO = new org.oscm.vo.VOPricedRole();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setPricePerUser(oldVO.getPricePerUser());
        newVO.setRole(convertToApi(oldVO.getRole()));
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.internal.vo.VOTechnicalServiceOperation convertToUp(
            org.oscm.vo.VOTechnicalServiceOperation oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.internal.vo.VOTechnicalServiceOperation newVO = new org.oscm.internal.vo.VOTechnicalServiceOperation();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setOperationId(oldVO.getOperationId());
        newVO.setOperationName(oldVO.getOperationName());
        newVO.setOperationDescription(oldVO.getOperationDescription());
        if (oldVO.getOperationParameters() != null) {
            for (org.oscm.vo.VOServiceOperationParameter tmp : oldVO
                    .getOperationParameters()) {
                newVO.getOperationParameters().add(convertToUp(tmp));
            }
        }
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.internal.vo.VOServiceOperationParameter convertToUp(
            org.oscm.vo.VOServiceOperationParameter oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.internal.vo.VOServiceOperationParameter newVO = new org.oscm.internal.vo.VOServiceOperationParameter();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setParameterId(oldVO.getParameterId());
        newVO.setParameterName(oldVO.getParameterName());
        newVO.setParameterValue(oldVO.getParameterValue());
        newVO.setMandatory(oldVO.isMandatory());
        newVO.setType(EnumConverter.convert(oldVO.getType(),
                org.oscm.internal.types.enumtypes.OperationParameterType.class));
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.internal.vo.VOServiceOperationParameterValues convertToUp(
            org.oscm.vo.VOServiceOperationParameterValues oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.internal.vo.VOServiceOperationParameterValues newVO = new org.oscm.internal.vo.VOServiceOperationParameterValues();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setParameterId(oldVO.getParameterId());
        newVO.setParameterName(oldVO.getParameterName());
        newVO.setParameterValue(oldVO.getParameterValue());
        newVO.setMandatory(oldVO.isMandatory());
        newVO.setType(EnumConverter.convert(oldVO.getType(),
                org.oscm.internal.types.enumtypes.OperationParameterType.class));
        newVO.setValues(oldVO.getValues());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.vo.VOTechnicalServiceOperation convertToApi(
            org.oscm.internal.vo.VOTechnicalServiceOperation oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.vo.VOTechnicalServiceOperation newVO = new org.oscm.vo.VOTechnicalServiceOperation();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setOperationId(oldVO.getOperationId());
        newVO.setOperationName(oldVO.getOperationName());
        newVO.setOperationDescription(oldVO.getOperationDescription());
        if (oldVO.getOperationParameters() != null) {
            for (org.oscm.internal.vo.VOServiceOperationParameter tmp : oldVO
                    .getOperationParameters()) {
                newVO.getOperationParameters().add(convertToApi(tmp));
            }
        }
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.vo.VOServiceOperationParameter convertToApi(
            org.oscm.internal.vo.VOServiceOperationParameter oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.vo.VOServiceOperationParameter newVO = new org.oscm.vo.VOServiceOperationParameter();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setParameterId(oldVO.getParameterId());
        newVO.setParameterName(oldVO.getParameterName());
        newVO.setParameterValue(oldVO.getParameterValue());
        newVO.setMandatory(oldVO.isMandatory());
        newVO.setType(EnumConverter.convert(oldVO.getType(),
                OperationParameterType.class));
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.vo.VOServiceOperationParameterValues convertToApi(
            org.oscm.internal.vo.VOServiceOperationParameterValues oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.vo.VOServiceOperationParameterValues newVO = new org.oscm.vo.VOServiceOperationParameterValues();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setParameterId(oldVO.getParameterId());
        newVO.setParameterName(oldVO.getParameterName());
        newVO.setParameterValue(oldVO.getParameterValue());
        newVO.setMandatory(oldVO.isMandatory());
        newVO.setType(EnumConverter.convert(oldVO.getType(),
                OperationParameterType.class));
        newVO.setValues(oldVO.getValues());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.internal.vo.VOGatheredEvent convertToUp(
            org.oscm.vo.VOGatheredEvent oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.internal.vo.VOGatheredEvent newVO = new org.oscm.internal.vo.VOGatheredEvent();
        newVO.setOccurrenceTime(oldVO.getOccurrenceTime());
        newVO.setActor(oldVO.getActor());
        newVO.setEventId(oldVO.getEventId());
        newVO.setMultiplier(oldVO.getMultiplier());
        newVO.setUniqueId(oldVO.getUniqueId());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.vo.VOGatheredEvent convertToApi(
            org.oscm.internal.vo.VOGatheredEvent oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.vo.VOGatheredEvent newVO = new org.oscm.vo.VOGatheredEvent();
        newVO.setOccurrenceTime(oldVO.getOccurrenceTime());
        newVO.setActor(oldVO.getActor());
        newVO.setEventId(oldVO.getEventId());
        newVO.setMultiplier(oldVO.getMultiplier());
        newVO.setUniqueId(oldVO.getUniqueId());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.internal.vo.VOReport convertToUp(
            org.oscm.vo.VOReport oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.internal.vo.VOReport newVO = new org.oscm.internal.vo.VOReport();
        newVO.setReportName(oldVO.getReportName());
        newVO.setLocalizedReportName(oldVO.getLocalizedReportName());
        newVO.setReportURLTemplate(oldVO.getReportUrlTemplate());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.vo.VOReport convertToApi(
            org.oscm.internal.vo.VOReport oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.vo.VOReport newVO = new org.oscm.vo.VOReport();
        newVO.setReportName(oldVO.getReportName());
        newVO.setLocalizedReportName(oldVO.getLocalizedReportName());
        newVO.setReportURLTemplate(oldVO.getReportUrlTemplate());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.internal.vo.VOParameterDefinition convertToUp(
            org.oscm.vo.VOParameterDefinition oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.internal.vo.VOParameterDefinition newVO = new org.oscm.internal.vo.VOParameterDefinition();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setParameterOptions(
                convertToUpVOParameterOption(oldVO.getParameterOptions()));
        newVO.setDefaultValue(oldVO.getDefaultValue());
        newVO.setMinValue(oldVO.getMinValue());
        newVO.setMaxValue(oldVO.getMaxValue());
        newVO.setMandatory(oldVO.isMandatory());
        newVO.setConfigurable(oldVO.isConfigurable());
        newVO.setParameterType(EnumConverter.convert(oldVO.getParameterType(),
                org.oscm.internal.types.enumtypes.ParameterType.class));
        newVO.setParameterId(oldVO.getParameterId());
        newVO.setValueType(EnumConverter.convert(oldVO.getValueType(),
                org.oscm.internal.types.enumtypes.ParameterValueType.class));
        newVO.setModificationType(EnumConverter.convert(
                oldVO.getModificationType(),
                org.oscm.internal.types.enumtypes.ParameterModificationType.class));
        newVO.setDescription(oldVO.getDescription());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.vo.VOParameterDefinition convertToApi(
            org.oscm.internal.vo.VOParameterDefinition oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.vo.VOParameterDefinition newVO = new org.oscm.vo.VOParameterDefinition();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setParameterOptions(
                convertToApiVOParameterOption(oldVO.getParameterOptions()));
        newVO.setDefaultValue(oldVO.getDefaultValue());
        newVO.setMinValue(oldVO.getMinValue());
        newVO.setMaxValue(oldVO.getMaxValue());
        newVO.setMandatory(oldVO.isMandatory());
        newVO.setConfigurable(oldVO.isConfigurable());
        newVO.setParameterType(EnumConverter.convert(oldVO.getParameterType(),
                org.oscm.types.enumtypes.ParameterType.class));
        newVO.setParameterId(oldVO.getParameterId());
        newVO.setValueType(EnumConverter.convert(oldVO.getValueType(),
                org.oscm.types.enumtypes.ParameterValueType.class));
        newVO.setModificationType(EnumConverter.convert(
                oldVO.getModificationType(),
                org.oscm.types.enumtypes.ParameterModificationType.class));
        newVO.setDescription(oldVO.getDescription());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.internal.vo.VOTriggerProcess convertToUp(
            org.oscm.vo.VOTriggerProcess oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.internal.vo.VOTriggerProcess newVO = new org.oscm.internal.vo.VOTriggerProcess();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setActivationDate(oldVO.getActivationDate());
        newVO.setReason(oldVO.getReason());
        newVO.setStatus(EnumConverter.convert(oldVO.getStatus(),
                org.oscm.internal.types.enumtypes.TriggerProcessStatus.class));
        newVO.setTriggerDefinition(convertToUp(oldVO.getTriggerDefinition()));
        newVO.setUser(convertToUp(oldVO.getUser()));
        newVO.setTargetNames(oldVO.getTargetNames());
        newVO.setParameter(oldVO.getParameter());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.vo.VOTriggerProcess convertToApi(
            org.oscm.internal.vo.VOTriggerProcess oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.vo.VOTriggerProcess newVO = new org.oscm.vo.VOTriggerProcess();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setActivationDate(oldVO.getActivationDate());
        newVO.setReason(oldVO.getReason());
        newVO.setStatus(EnumConverter.convert(oldVO.getStatus(),
                org.oscm.types.enumtypes.TriggerProcessStatus.class));
        newVO.setTriggerDefinition(convertToApi(oldVO.getTriggerDefinition()));
        newVO.setUser(convertToApi(oldVO.getUser()));
        newVO.setTargetNames(oldVO.getTargetNames());
        newVO.setParameter(oldVO.getParameter());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.internal.vo.VOParameterOption convertToUp(
            org.oscm.vo.VOParameterOption oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.internal.vo.VOParameterOption newVO = new org.oscm.internal.vo.VOParameterOption();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setOptionId(oldVO.getOptionId());
        newVO.setOptionDescription(oldVO.getOptionDescription());
        newVO.setParamDefId(oldVO.getParamDefId());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.vo.VOParameterOption convertToApi(
            org.oscm.internal.vo.VOParameterOption oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.vo.VOParameterOption newVO = new org.oscm.vo.VOParameterOption();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setOptionId(oldVO.getOptionId());
        newVO.setOptionDescription(oldVO.getOptionDescription());
        newVO.setParamDefId(oldVO.getParamDefId());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.internal.vo.VOOrganization convertToUp(
            org.oscm.vo.VOOrganization oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.internal.vo.VOOrganization newVO = new org.oscm.internal.vo.VOOrganization();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setOrganizationId(oldVO.getOrganizationId());
        newVO.setAddress(oldVO.getAddress());
        newVO.setEmail(oldVO.getEmail());
        newVO.setLocale(oldVO.getLocale());
        newVO.setName(oldVO.getName());
        newVO.setPhone(oldVO.getPhone());
        newVO.setUrl(oldVO.getUrl());
        newVO.setDescription(oldVO.getDescription());
        newVO.setDiscount(convertToUp(oldVO.getDiscount()));
        newVO.setDistinguishedName(oldVO.getDistinguishedName());
        newVO.setDomicileCountry(oldVO.getDomicileCountry());
        newVO.setNameSpace(oldVO.getNameSpace());
        newVO.setImageDefined(oldVO.isImageDefined());
        newVO.setSupportEmail(oldVO.getSupportEmail());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.vo.VOOrganization convertToApi(
            org.oscm.internal.vo.VOOrganization oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.vo.VOOrganization newVO = new org.oscm.vo.VOOrganization();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setOrganizationId(oldVO.getOrganizationId());
        newVO.setAddress(oldVO.getAddress());
        newVO.setEmail(oldVO.getEmail());
        newVO.setLocale(oldVO.getLocale());
        newVO.setName(oldVO.getName());
        newVO.setPhone(oldVO.getPhone());
        newVO.setUrl(oldVO.getUrl());
        newVO.setDescription(oldVO.getDescription());
        newVO.setDiscount(convertToApi(oldVO.getDiscount()));
        newVO.setDistinguishedName(oldVO.getDistinguishedName());
        newVO.setDomicileCountry(oldVO.getDomicileCountry());
        newVO.setNameSpace(oldVO.getNameSpace());
        newVO.setImageDefined(oldVO.isImageDefined());
        newVO.setSupportEmail(oldVO.getSupportEmail());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.internal.vo.VOUserDetails convertToUp(
            org.oscm.vo.VOUserDetails oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.internal.vo.VOUserDetails newVO = new org.oscm.internal.vo.VOUserDetails();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setEMail(oldVO.getEMail());
        newVO.setFirstName(oldVO.getFirstName());
        newVO.setAdditionalName(oldVO.getAdditionalName());
        newVO.setLastName(oldVO.getLastName());
        newVO.setAddress(oldVO.getAddress());
        newVO.setPhone(oldVO.getPhone());
        newVO.setLocale(oldVO.getLocale());
        newVO.setSalutation(EnumConverter.convert(oldVO.getSalutation(),
                org.oscm.internal.types.enumtypes.Salutation.class));
        newVO.setRealmUserId(oldVO.getRealmUserId());
        newVO.setRemoteLdapActive(oldVO.isRemoteLdapActive());
        newVO.setRemoteLdapAttributes(
                EnumConverter.convertList(oldVO.getRemoteLdapAttributes(),
                        org.oscm.internal.types.enumtypes.SettingType.class));
        newVO.setOrganizationId(oldVO.getOrganizationId());
        newVO.setUserId(oldVO.getUserId());
        newVO.setStatus(EnumConverter.convert(oldVO.getStatus(),
                org.oscm.internal.types.enumtypes.UserAccountStatus.class));
        newVO.setOrganizationRoles(EnumConverter.convertSet(
                oldVO.getOrganizationRoles(),
                org.oscm.internal.types.enumtypes.OrganizationRoleType.class));
        newVO.setUserRoles(EnumConverter.convertSet(oldVO.getUserRoles(),
                org.oscm.internal.types.enumtypes.UserRoleType.class));

        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.vo.VOUserDetails convertToApi(
            org.oscm.internal.vo.VOUserDetails oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.vo.VOUserDetails newVO = new org.oscm.vo.VOUserDetails();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setEMail(oldVO.getEMail());
        newVO.setFirstName(oldVO.getFirstName());
        newVO.setAdditionalName(oldVO.getAdditionalName());
        newVO.setLastName(oldVO.getLastName());
        newVO.setAddress(oldVO.getAddress());
        newVO.setPhone(oldVO.getPhone());
        newVO.setLocale(oldVO.getLocale());
        newVO.setSalutation(EnumConverter.convert(oldVO.getSalutation(),
                org.oscm.types.enumtypes.Salutation.class));
        newVO.setRealmUserId(oldVO.getRealmUserId());
        newVO.setRemoteLdapActive(oldVO.isRemoteLdapActive());
        newVO.setRemoteLdapAttributes(
                EnumConverter.convertList(oldVO.getRemoteLdapAttributes(),
                        org.oscm.types.enumtypes.SettingType.class));
        newVO.setOrganizationId(oldVO.getOrganizationId());
        newVO.setUserId(oldVO.getUserId());
        newVO.setStatus(EnumConverter.convert(oldVO.getStatus(),
                org.oscm.types.enumtypes.UserAccountStatus.class));
        newVO.setOrganizationRoles(
                EnumConverter.convertSet(oldVO.getOrganizationRoles(),
                        org.oscm.types.enumtypes.OrganizationRoleType.class));
        newVO.setUserRoles(EnumConverter.convertSet(oldVO.getUserRoles(),
                org.oscm.types.enumtypes.UserRoleType.class));
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.internal.vo.VOTechnicalService convertToUp(
            org.oscm.vo.VOTechnicalService oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.internal.vo.VOTechnicalService newVO = new org.oscm.internal.vo.VOTechnicalService();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setEventDefinitions(
                convertToUpVOEventDefinition(oldVO.getEventDefinitions()));
        newVO.setTechnicalServiceId(oldVO.getTechnicalServiceId());
        newVO.setTechnicalServiceBuildId(oldVO.getTechnicalServiceBuildId());
        newVO.setAccessType(EnumConverter.convert(oldVO.getAccessType(),
                org.oscm.internal.types.enumtypes.ServiceAccessType.class));
        newVO.setTechnicalServiceDescription(
                oldVO.getTechnicalServiceDescription());
        newVO.setBaseUrl(oldVO.getBaseUrl());
        newVO.setProvisioningUrl(oldVO.getProvisioningUrl());
        newVO.setLoginPath(oldVO.getLoginPath());
        newVO.setProvisioningVersion(oldVO.getProvisioningVersion());
        newVO.setParameterDefinitions(convertToUpVOParameterDefinition(
                oldVO.getParameterDefinitions()));
        newVO.setRoleDefinitions(
                convertToUpVORoleDefinition(oldVO.getRoleDefinitions()));
        newVO.setTags(oldVO.getTags());
        newVO.setLicense(oldVO.getLicense());
        newVO.setAccessInfo(oldVO.getAccessInfo());
        newVO.setTechnicalServiceOperations(
                convertToUpVOTechnicalServiceOperation(
                        oldVO.getTechnicalServiceOperations()));
        newVO.setExternalBilling(oldVO.isExternalBilling());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.vo.VOTechnicalService convertToApi(
            org.oscm.internal.vo.VOTechnicalService oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.vo.VOTechnicalService newVO = new org.oscm.vo.VOTechnicalService();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setEventDefinitions(
                convertToApiVOEventDefinition(oldVO.getEventDefinitions()));
        newVO.setTechnicalServiceId(oldVO.getTechnicalServiceId());
        newVO.setTechnicalServiceBuildId(oldVO.getTechnicalServiceBuildId());
        newVO.setAccessType(EnumConverter.convert(oldVO.getAccessType(),
                org.oscm.types.enumtypes.ServiceAccessType.class));
        newVO.setTechnicalServiceDescription(
                oldVO.getTechnicalServiceDescription());
        newVO.setBaseUrl(oldVO.getBaseUrl());
        newVO.setProvisioningUrl(oldVO.getProvisioningUrl());
        newVO.setLoginPath(oldVO.getLoginPath());
        newVO.setProvisioningVersion(oldVO.getProvisioningVersion());
        newVO.setParameterDefinitions(convertToApiVOParameterDefinition(
                oldVO.getParameterDefinitions()));
        newVO.setRoleDefinitions(
                convertToApiVORoleDefinition(oldVO.getRoleDefinitions()));
        newVO.setTags(oldVO.getTags());
        newVO.setLicense(oldVO.getLicense());
        newVO.setAccessInfo(oldVO.getAccessInfo());
        newVO.setTechnicalServiceOperations(
                convertToApiVOTechnicalServiceOperation(
                        oldVO.getTechnicalServiceOperations()));
        newVO.setExternalBilling(oldVO.isExternalBilling());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.internal.vo.VOUserSubscription convertToUp(
            org.oscm.vo.VOUserSubscription oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.internal.vo.VOUserSubscription newVO = new org.oscm.internal.vo.VOUserSubscription();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setServiceId(oldVO.getServiceId());
        newVO.setServiceKey(oldVO.getServiceKey());
        newVO.setActivationDate(oldVO.getActivationDate());
        newVO.setCreationDate(oldVO.getCreationDate());
        newVO.setDeactivationDate(oldVO.getDeactivationDate());
        newVO.setServiceAccessInfo(oldVO.getServiceAccessInfo());
        newVO.setServiceAccessType(EnumConverter.convert(
                oldVO.getServiceAccessType(),
                org.oscm.internal.types.enumtypes.ServiceAccessType.class));
        newVO.setServiceBaseURL(oldVO.getServiceBaseURL());
        newVO.setServiceLoginPath(oldVO.getServiceLoginPath());
        newVO.setStatus(EnumConverter.convert(oldVO.getStatus(),
                org.oscm.internal.types.enumtypes.SubscriptionStatus.class));
        newVO.setServiceInstanceId(oldVO.getServiceInstanceId());
        newVO.setTimeoutMailSent(oldVO.isTimeoutMailSent());
        newVO.setPurchaseOrderNumber(oldVO.getPurchaseOrderNumber());
        newVO.setSubscriptionId(oldVO.getSubscriptionId());
        newVO.setProvisioningProgress(oldVO.getProvisioningProgress());
        newVO.setNumberOfAssignedUsers(oldVO.getNumberOfAssignedUsers());
        newVO.setSellerName(oldVO.getSellerName());
        newVO.setTechnicalServiceOperations(
                convertToUpVOTechnicalServiceOperation(
                        oldVO.getTechnicalServiceOperations()));
        newVO.setLicense(convertToUp(oldVO.getLicense()));
        newVO.setOwnerId(oldVO.getOwnerId());
        newVO.setUnitKey(oldVO.getUnitKey());
        newVO.setUnitName(oldVO.getUnitName());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.vo.VOUserSubscription convertToApi(
            org.oscm.internal.vo.VOUserSubscription oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.vo.VOUserSubscription newVO = new org.oscm.vo.VOUserSubscription();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setServiceId(oldVO.getServiceId());
        newVO.setServiceKey(oldVO.getServiceKey());
        newVO.setActivationDate(oldVO.getActivationDate());
        newVO.setCreationDate(oldVO.getCreationDate());
        newVO.setDeactivationDate(oldVO.getDeactivationDate());
        newVO.setServiceAccessInfo(oldVO.getServiceAccessInfo());
        newVO.setServiceAccessType(
                EnumConverter.convert(oldVO.getServiceAccessType(),
                        org.oscm.types.enumtypes.ServiceAccessType.class));
        newVO.setServiceBaseURL(oldVO.getServiceBaseURL());
        newVO.setServiceLoginPath(oldVO.getServiceLoginPath());
        newVO.setStatus(EnumConverter.convert(oldVO.getStatus(),
                org.oscm.types.enumtypes.SubscriptionStatus.class));
        newVO.setServiceInstanceId(oldVO.getServiceInstanceId());
        newVO.setTimeoutMailSent(oldVO.isTimeoutMailSent());
        newVO.setPurchaseOrderNumber(oldVO.getPurchaseOrderNumber());
        newVO.setSubscriptionId(oldVO.getSubscriptionId());
        newVO.setProvisioningProgress(oldVO.getProvisioningProgress());
        newVO.setNumberOfAssignedUsers(oldVO.getNumberOfAssignedUsers());
        newVO.setSellerName(oldVO.getSellerName());
        newVO.setTechnicalServiceOperations(
                convertToApiVOTechnicalServiceOperation(
                        oldVO.getTechnicalServiceOperations()));
        newVO.setLicense(convertToApi(oldVO.getLicense()));
        newVO.setOwnerId(oldVO.getOwnerId());
        newVO.setUnitKey(oldVO.getUnitKey());
        newVO.setUnitName(oldVO.getUnitName());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.internal.vo.VOServiceDetails convertToUp(
            org.oscm.vo.VOServiceDetails oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.internal.vo.VOServiceDetails newVO = new org.oscm.internal.vo.VOServiceDetails();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setTechnicalService(convertToUp(oldVO.getTechnicalService()));
        newVO.setImageDefined(oldVO.isImageDefined());
        newVO.setParameters(convertToUpVOParameter(oldVO.getParameters()));
        newVO.setDescription(oldVO.getDescription());
        newVO.setName(oldVO.getName());
        newVO.setServiceId(oldVO.getServiceId());
        newVO.setTechnicalId(oldVO.getTechnicalId());
        newVO.setFeatureURL(oldVO.getFeatureURL());
        newVO.setBaseURL(oldVO.getBaseURL());
        newVO.setPriceModel(convertToUp(oldVO.getPriceModel()));
        newVO.setStatus(EnumConverter.convert(oldVO.getStatus(),
                org.oscm.internal.types.enumtypes.ServiceStatus.class));
        newVO.setAccessType(EnumConverter.convert(oldVO.getAccessType(),
                org.oscm.internal.types.enumtypes.ServiceAccessType.class));
        newVO.setSellerId(oldVO.getSellerId());
        newVO.setSellerName(oldVO.getSellerName());
        newVO.setSellerKey(oldVO.getSellerKey());
        newVO.setTags(oldVO.getTags());
        newVO.setShortDescription(oldVO.getShortDescription());
        newVO.setAverageRating(oldVO.getAverageRating());
        newVO.setNumberOfReviews(oldVO.getNumberOfReviews());
        newVO.setOfferingType(EnumConverter.convert(oldVO.getOfferingType(),
                org.oscm.internal.types.enumtypes.OfferingType.class));
        newVO.setConfiguratorUrl(oldVO.getConfiguratorUrl());
        newVO.setCustomTabUrl(oldVO.getCustomTabUrl());
        newVO.setCustomTabName(oldVO.getCustomTabName());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.vo.VOServiceDetails convertToApi(
            org.oscm.internal.vo.VOServiceDetails oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.vo.VOServiceDetails newVO = new org.oscm.vo.VOServiceDetails();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setTechnicalService(convertToApi(oldVO.getTechnicalService()));
        newVO.setImageDefined(oldVO.isImageDefined());
        newVO.setParameters(convertToApiVOParameter(oldVO.getParameters()));
        newVO.setDescription(oldVO.getDescription());
        newVO.setName(oldVO.getName());
        newVO.setServiceId(oldVO.getServiceId());
        newVO.setTechnicalId(oldVO.getTechnicalId());
        newVO.setFeatureURL(oldVO.getFeatureURL());
        newVO.setBaseURL(oldVO.getBaseURL());
        newVO.setPriceModel(convertToApi(oldVO.getPriceModel()));
        newVO.setStatus(EnumConverter.convert(oldVO.getStatus(),
                org.oscm.types.enumtypes.ServiceStatus.class));
        newVO.setAccessType(EnumConverter.convert(oldVO.getAccessType(),
                org.oscm.types.enumtypes.ServiceAccessType.class));
        newVO.setSellerId(oldVO.getSellerId());
        newVO.setSellerName(oldVO.getSellerName());
        newVO.setSellerKey(oldVO.getSellerKey());
        newVO.setTags(oldVO.getTags());
        newVO.setShortDescription(oldVO.getShortDescription());
        newVO.setAverageRating(oldVO.getAverageRating());
        newVO.setNumberOfReviews(oldVO.getNumberOfReviews());
        newVO.setOfferingType(EnumConverter.convert(oldVO.getOfferingType(),
                org.oscm.types.enumtypes.OfferingType.class));
        newVO.setConfiguratorUrl(oldVO.getConfiguratorUrl());
        newVO.setCustomTabUrl(oldVO.getCustomTabUrl());
        newVO.setCustomTabName(oldVO.getCustomTabName());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.vo.VOServiceEntry convertToApi(
            org.oscm.internal.vo.VOServiceEntry oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.vo.VOServiceEntry newVO = new org.oscm.vo.VOServiceEntry();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setParameters(convertToApiVOParameter(oldVO.getParameters()));
        newVO.setDescription(oldVO.getDescription());
        newVO.setName(oldVO.getName());
        newVO.setServiceId(oldVO.getServiceId());
        newVO.setTechnicalId(oldVO.getTechnicalId());
        newVO.setFeatureURL(oldVO.getFeatureURL());
        newVO.setBaseURL(oldVO.getBaseURL());
        newVO.setPriceModel(convertToApi(oldVO.getPriceModel()));
        newVO.setStatus(EnumConverter.convert(oldVO.getStatus(),
                org.oscm.types.enumtypes.ServiceStatus.class));
        newVO.setAccessType(EnumConverter.convert(oldVO.getAccessType(),
                org.oscm.types.enumtypes.ServiceAccessType.class));
        newVO.setSellerId(oldVO.getSellerId());
        newVO.setSellerName(oldVO.getSellerName());
        newVO.setSellerKey(oldVO.getSellerKey());
        newVO.setTags(oldVO.getTags());
        newVO.setShortDescription(oldVO.getShortDescription());
        newVO.setAverageRating(oldVO.getAverageRating());
        newVO.setNumberOfReviews(oldVO.getNumberOfReviews());
        newVO.setOfferingType(EnumConverter.convert(oldVO.getOfferingType(),
                org.oscm.types.enumtypes.OfferingType.class));
        newVO.setSubscriptionLimitReached(oldVO.isSubscriptionLimitReached());
        newVO.setConfiguratorUrl(oldVO.getConfiguratorUrl());
        newVO.setCustomTabUrl(oldVO.getCustomTabUrl());
        newVO.setCustomTabName(oldVO.getCustomTabName());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.internal.vo.VOServiceEntry convertToUp(
            org.oscm.vo.VOServiceEntry oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.internal.vo.VOServiceEntry newVO = new org.oscm.internal.vo.VOServiceEntry();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setParameters(convertToUpVOParameter(oldVO.getParameters()));
        newVO.setDescription(oldVO.getDescription());
        newVO.setName(oldVO.getName());
        newVO.setServiceId(oldVO.getServiceId());
        newVO.setTechnicalId(oldVO.getTechnicalId());
        newVO.setFeatureURL(oldVO.getFeatureURL());
        newVO.setBaseURL(oldVO.getBaseURL());
        newVO.setPriceModel(convertToUp(oldVO.getPriceModel()));
        newVO.setStatus(EnumConverter.convert(oldVO.getStatus(),
                org.oscm.internal.types.enumtypes.ServiceStatus.class));
        newVO.setAccessType(EnumConverter.convert(oldVO.getAccessType(),
                org.oscm.internal.types.enumtypes.ServiceAccessType.class));
        newVO.setSellerId(oldVO.getSellerId());
        newVO.setSellerName(oldVO.getSellerName());
        newVO.setSellerKey(oldVO.getSellerKey());
        newVO.setTags(oldVO.getTags());
        newVO.setShortDescription(oldVO.getShortDescription());
        newVO.setAverageRating(oldVO.getAverageRating());
        newVO.setNumberOfReviews(oldVO.getNumberOfReviews());
        newVO.setOfferingType(EnumConverter.convert(oldVO.getOfferingType(),
                org.oscm.internal.types.enumtypes.OfferingType.class));
        newVO.setSubscriptionLimitReached(oldVO.isSubscriptionLimitReached());
        newVO.setConfiguratorUrl(oldVO.getConfiguratorUrl());
        newVO.setCustomTabUrl(oldVO.getCustomTabUrl());
        newVO.setCustomTabName(oldVO.getCustomTabName());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.internal.vo.VOPricedParameter convertToUp(
            org.oscm.vo.VOPricedParameter oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.internal.vo.VOPricedParameter newVO = new org.oscm.internal.vo.VOPricedParameter();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setVoParameterDef(convertToUp(oldVO.getVoParameterDef()));
        newVO.setPricePerUser(oldVO.getPricePerUser());
        newVO.setPricePerSubscription(oldVO.getPricePerSubscription());
        newVO.setParameterKey(oldVO.getParameterKey());
        newVO.setPricedOptions(
                convertToUpVOPricedOption(oldVO.getPricedOptions()));
        newVO.setRoleSpecificUserPrices(
                convertToUpVOPricedRole(oldVO.getRoleSpecificUserPrices()));
        newVO.setSteppedPrices(
                convertToUpVOSteppedPrice(oldVO.getSteppedPrices()));
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.vo.VOPricedParameter convertToApi(
            org.oscm.internal.vo.VOPricedParameter oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.vo.VOPricedParameter newVO = new org.oscm.vo.VOPricedParameter();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setVoParameterDef(convertToApi(oldVO.getVoParameterDef()));
        newVO.setPricePerUser(oldVO.getPricePerUser());
        newVO.setPricePerSubscription(oldVO.getPricePerSubscription());
        newVO.setParameterKey(oldVO.getParameterKey());
        newVO.setPricedOptions(
                convertToApiVOPricedOption(oldVO.getPricedOptions()));
        newVO.setRoleSpecificUserPrices(
                convertToApiVOPricedRole(oldVO.getRoleSpecificUserPrices()));
        newVO.setSteppedPrices(
                convertToApiVOSteppedPrice(oldVO.getSteppedPrices()));
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.internal.vo.VOUdaDefinition convertToUp(
            org.oscm.vo.VOUdaDefinition oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.internal.vo.VOUdaDefinition newVO = new org.oscm.internal.vo.VOUdaDefinition();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setUdaId(oldVO.getUdaId());
        newVO.setTargetType(oldVO.getTargetType());
        newVO.setDefaultValue(oldVO.getDefaultValue());
        newVO.setConfigurationType(EnumConverter.convert(
                oldVO.getConfigurationType(),
                org.oscm.internal.types.enumtypes.UdaConfigurationType.class));
        newVO.setEncrypted(false);
        newVO.setName("");

        UdaDefinition existing = getUdaFromDB(oldVO);
        if (existing != null) {
            newVO.setEncrypted(existing.getDataContainer().isEncrypted());
            newVO.setDefaultValue(oldVO.getDefaultValue());
            String value = retrieveNameForUdaDefinition(existing);
            newVO.setName(value);
        }
        return newVO;
    }

    protected static String retrieveNameForUdaDefinition(
            UdaDefinition existing) {
        String value = "";
        Query query = ds
                .createNamedQuery("LocalizedResource.getAllTextsWithLocale");
        query.setParameter("objectKey", existing.getKey());
        query.setParameter("objectType",
                LocalizedObjectTypes.CUSTOM_ATTRIBUTE_NAME);
        for (LocalizedResource resource : ParameterizedTypes
                .iterable(query.getResultList(), LocalizedResource.class)) {
            if (resource.getLocale().equals(ds.getCurrentUser().getLocale())) {
                value = resource.getValue();
                break;
            }
        }
        return value;
    }

    private static UdaDefinition getUdaFromDB(VOUdaDefinition oldVO) {
        return ds.find(UdaDefinition.class, oldVO.getKey());
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.vo.VOUdaDefinition convertToApi(
            org.oscm.internal.vo.VOUdaDefinition oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.vo.VOUdaDefinition newVO = new org.oscm.vo.VOUdaDefinition();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setUdaId(oldVO.getUdaId());
        newVO.setTargetType(oldVO.getTargetType());
        newVO.setDefaultValue(oldVO.getDefaultValue());
        newVO.setConfigurationType(
                EnumConverter.convert(oldVO.getConfigurationType(),
                        org.oscm.types.enumtypes.UdaConfigurationType.class));
        return newVO;
    }

    private static Uda getUdaFromDB(org.oscm.vo.VOUda oldVO) {
        return ds.find(Uda.class, oldVO.getKey());
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.internal.vo.VOSubscriptionDetails convertToUp(
            org.oscm.vo.VOSubscriptionDetails oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.internal.vo.VOSubscriptionDetails newVO = new org.oscm.internal.vo.VOSubscriptionDetails();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setServiceId(oldVO.getServiceId());
        newVO.setServiceKey(oldVO.getServiceKey());
        newVO.setActivationDate(oldVO.getActivationDate());
        newVO.setCreationDate(oldVO.getCreationDate());
        newVO.setDeactivationDate(oldVO.getDeactivationDate());
        newVO.setServiceAccessInfo(oldVO.getServiceAccessInfo());
        newVO.setServiceAccessType(EnumConverter.convert(
                oldVO.getServiceAccessType(),
                org.oscm.internal.types.enumtypes.ServiceAccessType.class));
        newVO.setServiceBaseURL(oldVO.getServiceBaseURL());
        newVO.setServiceLoginPath(oldVO.getServiceLoginPath());
        newVO.setStatus(EnumConverter.convert(oldVO.getStatus(),
                org.oscm.internal.types.enumtypes.SubscriptionStatus.class));
        newVO.setServiceInstanceId(oldVO.getServiceInstanceId());
        newVO.setTimeoutMailSent(oldVO.isTimeoutMailSent());
        newVO.setPurchaseOrderNumber(oldVO.getPurchaseOrderNumber());
        newVO.setSubscriptionId(oldVO.getSubscriptionId());
        newVO.setProvisioningProgress(oldVO.getProvisioningProgress());
        newVO.setNumberOfAssignedUsers(oldVO.getNumberOfAssignedUsers());
        newVO.setSellerName(oldVO.getSellerName());
        newVO.setTechnicalServiceOperations(
                convertToUpVOTechnicalServiceOperation(
                        oldVO.getTechnicalServiceOperations()));
        newVO.setUsageLicenses(
                convertToUpVOUsageLicense(oldVO.getUsageLicenses()));
        newVO.setPriceModel(convertToUp(oldVO.getPriceModel()));
        newVO.setSubscribedService(convertToUp(oldVO.getSubscribedService()));
        newVO.setBillingContact(convertToUp(oldVO.getBillingContact()));
        newVO.setPaymentInfo(convertToUp(oldVO.getPaymentInfo()));
        newVO.setOwnerId(oldVO.getOwnerId());
        newVO.setUnitKey(oldVO.getUnitKey());
        newVO.setUnitName(oldVO.getUnitName());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.vo.VOSubscriptionDetails convertToApi(
            org.oscm.internal.vo.VOSubscriptionDetails oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.vo.VOSubscriptionDetails newVO = new org.oscm.vo.VOSubscriptionDetails();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setServiceId(oldVO.getServiceId());
        newVO.setServiceKey(oldVO.getServiceKey());
        newVO.setActivationDate(oldVO.getActivationDate());
        newVO.setCreationDate(oldVO.getCreationDate());
        newVO.setDeactivationDate(oldVO.getDeactivationDate());
        newVO.setServiceAccessInfo(oldVO.getServiceAccessInfo());
        newVO.setServiceAccessType(
                EnumConverter.convert(oldVO.getServiceAccessType(),
                        org.oscm.types.enumtypes.ServiceAccessType.class));
        newVO.setServiceBaseURL(oldVO.getServiceBaseURL());
        newVO.setServiceLoginPath(oldVO.getServiceLoginPath());
        newVO.setStatus(EnumConverter.convert(oldVO.getStatus(),
                org.oscm.types.enumtypes.SubscriptionStatus.class));
        newVO.setServiceInstanceId(oldVO.getServiceInstanceId());
        newVO.setTimeoutMailSent(oldVO.isTimeoutMailSent());
        newVO.setPurchaseOrderNumber(oldVO.getPurchaseOrderNumber());
        newVO.setSubscriptionId(oldVO.getSubscriptionId());
        newVO.setProvisioningProgress(oldVO.getProvisioningProgress());
        newVO.setNumberOfAssignedUsers(oldVO.getNumberOfAssignedUsers());
        newVO.setSellerName(oldVO.getSellerName());
        newVO.setTechnicalServiceOperations(
                convertToApiVOTechnicalServiceOperation(
                        oldVO.getTechnicalServiceOperations()));
        newVO.setUsageLicenses(
                convertToVOUsageLicense(oldVO.getUsageLicenses()));
        newVO.setPriceModel(convertToApi(oldVO.getPriceModel()));
        newVO.setSubscribedService(convertToApi(oldVO.getSubscribedService()));
        newVO.setBillingContact(convertToApi(oldVO.getBillingContact()));
        newVO.setPaymentInfo(convertToApi(oldVO.getPaymentInfo()));
        newVO.setOwnerId(oldVO.getOwnerId());
        newVO.setUnitKey(oldVO.getUnitKey());
        newVO.setUnitName(oldVO.getUnitName());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.internal.vo.VOPricedOption convertToUp(
            org.oscm.vo.VOPricedOption oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.internal.vo.VOPricedOption newVO = new org.oscm.internal.vo.VOPricedOption();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setPricePerUser(oldVO.getPricePerUser());
        newVO.setPricePerSubscription(oldVO.getPricePerSubscription());
        newVO.setParameterOptionKey(oldVO.getParameterOptionKey());
        newVO.setOptionId(oldVO.getOptionId());
        newVO.setRoleSpecificUserPrices(
                convertToUpVOPricedRole(oldVO.getRoleSpecificUserPrices()));
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.vo.VOPricedOption convertToApi(
            org.oscm.internal.vo.VOPricedOption oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.vo.VOPricedOption newVO = new org.oscm.vo.VOPricedOption();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setPricePerUser(oldVO.getPricePerUser());
        newVO.setPricePerSubscription(oldVO.getPricePerSubscription());
        newVO.setParameterOptionKey(oldVO.getParameterOptionKey());
        newVO.setOptionId(oldVO.getOptionId());
        newVO.setRoleSpecificUserPrices(
                convertToApiVOPricedRole(oldVO.getRoleSpecificUserPrices()));
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.internal.vo.VOBillingContact convertToUp(
            org.oscm.vo.VOBillingContact oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.internal.vo.VOBillingContact newVO = new org.oscm.internal.vo.VOBillingContact();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setEmail(oldVO.getEmail());
        newVO.setCompanyName(oldVO.getCompanyName());
        newVO.setAddress(oldVO.getAddress());
        newVO.setOrgAddressUsed(oldVO.isOrgAddressUsed());
        newVO.setId(oldVO.getId());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.internal.vo.VODiscount convertToUp(
            org.oscm.vo.VODiscount oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.internal.vo.VODiscount newVO = new org.oscm.internal.vo.VODiscount();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setEndTime(oldVO.getEndTime());
        newVO.setStartTime(oldVO.getStartTime());
        newVO.setValue(oldVO.getValue());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.vo.VOBillingContact convertToApi(
            org.oscm.internal.vo.VOBillingContact oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.vo.VOBillingContact newVO = new org.oscm.vo.VOBillingContact();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setEmail(oldVO.getEmail());
        newVO.setCompanyName(oldVO.getCompanyName());
        newVO.setAddress(oldVO.getAddress());
        newVO.setOrgAddressUsed(oldVO.isOrgAddressUsed());
        newVO.setId(oldVO.getId());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.vo.VODiscount convertToApi(
            org.oscm.internal.vo.VODiscount oldVO) {

        if (oldVO == null) {
            return null;
        }
        org.oscm.vo.VODiscount newVO = new org.oscm.vo.VODiscount();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setEndTime(oldVO.getEndTime());
        newVO.setStartTime(oldVO.getStartTime());
        newVO.setValue(oldVO.getValue());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.internal.vo.VOPaymentType convertToUp(
            org.oscm.vo.VOPaymentType oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.internal.vo.VOPaymentType newVO = new org.oscm.internal.vo.VOPaymentType();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setName(oldVO.getName());
        newVO.setPaymentTypeId(oldVO.getPaymentTypeId());
        newVO.setCollectionType(EnumConverter.convert(oldVO.getCollectionType(),
                org.oscm.internal.types.enumtypes.PaymentCollectionType.class));
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.vo.VOPaymentType convertToApi(
            org.oscm.internal.vo.VOPaymentType oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.vo.VOPaymentType newVO = new org.oscm.vo.VOPaymentType();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setName(oldVO.getName());
        newVO.setPaymentTypeId(oldVO.getPaymentTypeId());
        newVO.setCollectionType(EnumConverter.convert(oldVO.getCollectionType(),
                org.oscm.types.enumtypes.PaymentCollectionType.class));
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.vo.VOPaymentInfo convertToApi(
            org.oscm.internal.vo.VOPaymentInfo oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.vo.VOPaymentInfo newVO = new org.oscm.vo.VOPaymentInfo();
        newVO.setAccountNumber(oldVO.getAccountNumber());
        newVO.setId(oldVO.getId());
        newVO.setKey(oldVO.getKey());
        newVO.setPaymentType(convertToApi(oldVO.getPaymentType()));
        newVO.setProviderName(oldVO.getProviderName());
        newVO.setVersion(oldVO.getVersion());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.internal.vo.VOPaymentInfo convertToUp(
            org.oscm.vo.VOPaymentInfo oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.internal.vo.VOPaymentInfo newVO = new org.oscm.internal.vo.VOPaymentInfo();
        newVO.setAccountNumber(oldVO.getAccountNumber());
        newVO.setId(oldVO.getId());
        newVO.setKey(oldVO.getKey());
        newVO.setPaymentType(convertToUp(oldVO.getPaymentType()));
        newVO.setProviderName(oldVO.getProviderName());
        newVO.setVersion(oldVO.getVersion());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.internal.vo.VOSteppedPrice convertToUp(
            org.oscm.vo.VOSteppedPrice oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.internal.vo.VOSteppedPrice newVO = new org.oscm.internal.vo.VOSteppedPrice();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setLimit(oldVO.getLimit());
        newVO.setPrice(oldVO.getPrice());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.vo.VOSteppedPrice convertToApi(
            org.oscm.internal.vo.VOSteppedPrice oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.vo.VOSteppedPrice newVO = new org.oscm.vo.VOSteppedPrice();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setLimit(oldVO.getLimit());
        newVO.setPrice(oldVO.getPrice());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.internal.vo.VOInstanceInfo convertToUp(
            org.oscm.vo.VOInstanceInfo oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.internal.vo.VOInstanceInfo newVO = new org.oscm.internal.vo.VOInstanceInfo();
        newVO.setInstanceId(oldVO.getInstanceId());
        newVO.setAccessInfo(oldVO.getAccessInfo());
        newVO.setBaseUrl(oldVO.getBaseUrl());
        newVO.setLoginPath(oldVO.getLoginPath());
        newVO.setVmsNumber(oldVO.getVmsNumber());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.vo.VOInstanceInfo convertToApi(
            org.oscm.internal.vo.VOInstanceInfo oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.vo.VOInstanceInfo newVO = new org.oscm.vo.VOInstanceInfo();
        newVO.setInstanceId(oldVO.getInstanceId());
        newVO.setAccessInfo(oldVO.getAccessInfo());
        newVO.setBaseUrl(oldVO.getBaseUrl());
        newVO.setLoginPath(oldVO.getLoginPath());
        newVO.setVmsNumber(oldVO.getVmsNumber());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.internal.vo.VOPricedEvent convertToUp(
            org.oscm.vo.VOPricedEvent oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.internal.vo.VOPricedEvent newVO = new org.oscm.internal.vo.VOPricedEvent();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setSteppedPrices(
                convertToUpVOSteppedPrice(oldVO.getSteppedPrices()));
        newVO.setEventDefinition(convertToUp(oldVO.getEventDefinition()));
        newVO.setEventPrice(oldVO.getEventPrice());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.vo.VOPricedEvent convertToApi(
            org.oscm.internal.vo.VOPricedEvent oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.vo.VOPricedEvent newVO = new org.oscm.vo.VOPricedEvent();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setSteppedPrices(
                convertToApiVOSteppedPrice(oldVO.getSteppedPrices()));
        newVO.setEventDefinition(convertToApi(oldVO.getEventDefinition()));
        newVO.setEventPrice(oldVO.getEventPrice());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.internal.vo.VOTriggerDefinition convertToUp(
            org.oscm.vo.VOTriggerDefinition oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.internal.vo.VOTriggerDefinition newVO = new org.oscm.internal.vo.VOTriggerDefinition();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setType(EnumConverter.convert(oldVO.getType(),
                org.oscm.internal.types.enumtypes.TriggerType.class));
        newVO.setTargetType(EnumConverter.convert(oldVO.getTargetType(),
                org.oscm.internal.types.enumtypes.TriggerTargetType.class));
        newVO.setTarget(oldVO.getTarget());
        newVO.setSuspendProcess(oldVO.isSuspendProcess());
        newVO.setName(oldVO.getName());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.vo.VOTriggerDefinition convertToApi(
            org.oscm.internal.vo.VOTriggerDefinition oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.vo.VOTriggerDefinition newVO = new org.oscm.vo.VOTriggerDefinition();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setType(EnumConverter.convert(oldVO.getType(),
                org.oscm.types.enumtypes.TriggerType.class));
        newVO.setTargetType(EnumConverter.convert(oldVO.getTargetType(),
                org.oscm.types.enumtypes.TriggerTargetType.class));
        newVO.setTarget(oldVO.getTarget());
        newVO.setSuspendProcess(oldVO.isSuspendProcess());
        newVO.setName(oldVO.getName());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.internal.vo.VOImageResource convertToUp(
            org.oscm.vo.VOImageResource oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.internal.vo.VOImageResource newVO = new org.oscm.internal.vo.VOImageResource();
        newVO.setBuffer(oldVO.getBuffer());
        newVO.setContentType(oldVO.getContentType());
        newVO.setImageType(EnumConverter.convert(oldVO.getImageType(),
                org.oscm.internal.types.enumtypes.ImageType.class));
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.vo.VOImageResource convertToApi(
            org.oscm.internal.vo.VOImageResource oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.vo.VOImageResource newVO = new org.oscm.vo.VOImageResource();
        newVO.setBuffer(oldVO.getBuffer());
        newVO.setContentType(oldVO.getContentType());
        newVO.setImageType(EnumConverter.convert(oldVO.getImageType(),
                org.oscm.types.enumtypes.ImageType.class));
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.internal.vo.VOServiceLocalization convertToUp(
            org.oscm.vo.VOServiceLocalization oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.internal.vo.VOServiceLocalization newVO = new org.oscm.internal.vo.VOServiceLocalization();
        newVO.setNames(convertToUpVOLocalizedText(oldVO.getNames()));
        newVO.setDescriptions(
                convertToUpVOLocalizedText(oldVO.getDescriptions()));
        newVO.setShortDescriptions(
                convertToUpVOLocalizedText(oldVO.getShortDescriptions()));
        newVO.setCustomTabNames(
                convertToUpVOLocalizedText(oldVO.getCustomTabNames()));
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.vo.VOServiceLocalization convertToApi(
            org.oscm.internal.vo.VOServiceLocalization oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.vo.VOServiceLocalization newVO = new org.oscm.vo.VOServiceLocalization();
        newVO.setNames(convertToVOLocalizedText(oldVO.getNames()));
        newVO.setDescriptions(
                convertToVOLocalizedText(oldVO.getDescriptions()));
        newVO.setShortDescriptions(
                convertToVOLocalizedText(oldVO.getShortDescriptions()));
        newVO.setCustomTabNames(
                convertToVOLocalizedText(oldVO.getCustomTabNames()));
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.vo.VOServiceListResult convertToApi(
            org.oscm.internal.vo.VOServiceListResult oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.vo.VOServiceListResult newVO = new org.oscm.vo.VOServiceListResult();
        newVO.setResultSize(oldVO.getResultSize());
        for (org.oscm.internal.vo.VOService s : oldVO.getServices()) {
            newVO.getServices().add(convertToApi(s));
        }
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.internal.vo.VOServiceListResult convertToUp(
            org.oscm.vo.VOServiceListResult oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.internal.vo.VOServiceListResult newVO = new org.oscm.internal.vo.VOServiceListResult();
        newVO.setResultSize(oldVO.getResultSize());
        for (org.oscm.vo.VOService s : oldVO.getServices()) {
            newVO.getServices().add(convertToUp(s));
        }
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.internal.vo.ListCriteria convertToUp(
            org.oscm.vo.ListCriteria oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.internal.vo.ListCriteria newVO = new org.oscm.internal.vo.ListCriteria();
        newVO.setOffset(oldVO.getOffset());
        newVO.setLimit(oldVO.getLimit());
        newVO.setFilter(oldVO.getFilter());
        newVO.setSorting(EnumConverter.convert(oldVO.getSorting(),
                org.oscm.internal.types.enumtypes.Sorting.class));
        newVO.setCategoryId(oldVO.getCategoryId());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.vo.ListCriteria convertToApi(
            org.oscm.internal.vo.ListCriteria oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.vo.ListCriteria newVO = new org.oscm.vo.ListCriteria();
        newVO.setOffset(oldVO.getOffset());
        newVO.setLimit(oldVO.getLimit());
        newVO.setFilter(oldVO.getFilter());
        newVO.setSorting(EnumConverter.convert(oldVO.getSorting(),
                org.oscm.types.enumtypes.Sorting.class));
        newVO.setCategoryId(oldVO.getCategoryId());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.internal.vo.VOOrganizationPaymentConfiguration convertToUp(
            org.oscm.vo.VOOrganizationPaymentConfiguration oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.internal.vo.VOOrganizationPaymentConfiguration newVO = new org.oscm.internal.vo.VOOrganizationPaymentConfiguration();
        newVO.setOrganization(convertToUp(oldVO.getOrganization()));
        newVO.setEnabledPaymentTypes(
                convertToUpVOPaymentType(oldVO.getEnabledPaymentTypes()));
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.vo.VOOrganizationPaymentConfiguration convertToApi(
            org.oscm.internal.vo.VOOrganizationPaymentConfiguration oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.vo.VOOrganizationPaymentConfiguration newVO = new org.oscm.vo.VOOrganizationPaymentConfiguration();
        newVO.setOrganization(convertToApi(oldVO.getOrganization()));
        newVO.setEnabledPaymentTypes(
                convertToApiVOPaymentType(oldVO.getEnabledPaymentTypes()));
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.internal.vo.VORoleDefinition convertToUp(
            org.oscm.vo.VORoleDefinition oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.internal.vo.VORoleDefinition newVO = new org.oscm.internal.vo.VORoleDefinition();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setRoleId(oldVO.getRoleId());
        newVO.setName(oldVO.getName());
        newVO.setDescription(oldVO.getDescription());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.vo.VORoleDefinition convertToApi(
            org.oscm.internal.vo.VORoleDefinition oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.vo.VORoleDefinition newVO = new org.oscm.vo.VORoleDefinition();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setRoleId(oldVO.getRoleId());
        newVO.setName(oldVO.getName());
        newVO.setDescription(oldVO.getDescription());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.internal.vo.VOService convertToUp(
            org.oscm.vo.VOService oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.internal.vo.VOService newVO = new org.oscm.internal.vo.VOService();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setParameters(convertToUpVOParameter(oldVO.getParameters()));
        newVO.setDescription(oldVO.getDescription());
        newVO.setName(oldVO.getName());
        newVO.setServiceId(oldVO.getServiceId());
        newVO.setTechnicalId(oldVO.getTechnicalId());
        newVO.setFeatureURL(oldVO.getFeatureURL());
        newVO.setBaseURL(oldVO.getBaseURL());
        newVO.setPriceModel(convertToUp(oldVO.getPriceModel()));
        newVO.setStatus(EnumConverter.convert(oldVO.getStatus(),
                org.oscm.internal.types.enumtypes.ServiceStatus.class));
        newVO.setAccessType(EnumConverter.convert(oldVO.getAccessType(),
                org.oscm.internal.types.enumtypes.ServiceAccessType.class));
        newVO.setSellerId(oldVO.getSellerId());
        newVO.setSellerName(oldVO.getSellerName());
        newVO.setSellerKey(oldVO.getSellerKey());
        newVO.setTags(oldVO.getTags());
        newVO.setShortDescription(oldVO.getShortDescription());
        newVO.setAverageRating(oldVO.getAverageRating());
        newVO.setNumberOfReviews(oldVO.getNumberOfReviews());
        newVO.setOfferingType(EnumConverter.convert(oldVO.getOfferingType(),
                org.oscm.internal.types.enumtypes.OfferingType.class));
        newVO.setConfiguratorUrl(oldVO.getConfiguratorUrl());
        newVO.setCustomTabUrl(oldVO.getCustomTabUrl());
        newVO.setCustomTabName(oldVO.getCustomTabName());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.internal.vo.VOServiceReview convertToUp(
            org.oscm.vo.VOServiceReview oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.internal.vo.VOServiceReview newVO = new org.oscm.internal.vo.VOServiceReview();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setTitle(oldVO.getTitle());
        newVO.setComment(oldVO.getComment());
        newVO.setRating(oldVO.getRating());
        newVO.setModificationDate(oldVO.getModificationDate());
        newVO.setProductKey(oldVO.getProductKey());
        newVO.setUserId(oldVO.getUserId());
        newVO.setUserName(oldVO.getUserName());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.vo.VOServiceReview convertToApi(
            org.oscm.internal.vo.VOServiceReview oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.vo.VOServiceReview newVO = new org.oscm.vo.VOServiceReview();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setTitle(oldVO.getTitle());
        newVO.setComment(oldVO.getComment());
        newVO.setRating(oldVO.getRating());
        newVO.setModificationDate(oldVO.getModificationDate());
        newVO.setProductKey(oldVO.getProductKey());
        newVO.setUserId(oldVO.getUserId());
        newVO.setUserName(oldVO.getUserName());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.vo.VOService convertToApi(
            org.oscm.internal.vo.VOService oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.vo.VOService newVO = new org.oscm.vo.VOService();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setParameters(convertToApiVOParameter(oldVO.getParameters()));
        newVO.setDescription(oldVO.getDescription());
        newVO.setName(oldVO.getName());
        newVO.setServiceId(oldVO.getServiceId());
        newVO.setTechnicalId(oldVO.getTechnicalId());
        newVO.setFeatureURL(oldVO.getFeatureURL());
        newVO.setBaseURL(oldVO.getBaseURL());
        newVO.setPriceModel(convertToApi(oldVO.getPriceModel()));
        newVO.setStatus(EnumConverter.convert(oldVO.getStatus(),
                org.oscm.types.enumtypes.ServiceStatus.class));
        newVO.setAccessType(EnumConverter.convert(oldVO.getAccessType(),
                org.oscm.types.enumtypes.ServiceAccessType.class));
        newVO.setSellerId(oldVO.getSellerId());
        newVO.setSellerName(oldVO.getSellerName());
        newVO.setSellerKey(oldVO.getSellerKey());
        newVO.setTags(oldVO.getTags());
        newVO.setShortDescription(oldVO.getShortDescription());
        newVO.setAverageRating(oldVO.getAverageRating());
        newVO.setNumberOfReviews(oldVO.getNumberOfReviews());
        newVO.setOfferingType(EnumConverter.convert(oldVO.getOfferingType(),
                org.oscm.types.enumtypes.OfferingType.class));
        newVO.setConfiguratorUrl(oldVO.getConfiguratorUrl());
        newVO.setCustomTabUrl(oldVO.getCustomTabUrl());
        newVO.setCustomTabName(oldVO.getCustomTabName());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.internal.vo.VOEventDefinition convertToUp(
            org.oscm.vo.VOEventDefinition oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.internal.vo.VOEventDefinition newVO = new org.oscm.internal.vo.VOEventDefinition();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setEventType(EnumConverter.convert(oldVO.getEventType(),
                org.oscm.internal.types.enumtypes.EventType.class));
        newVO.setEventId(oldVO.getEventId());
        newVO.setEventDescription(oldVO.getEventDescription());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.vo.VOEventDefinition convertToApi(
            org.oscm.internal.vo.VOEventDefinition oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.vo.VOEventDefinition newVO = new org.oscm.vo.VOEventDefinition();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setEventType(EnumConverter.convert(oldVO.getEventType(),
                org.oscm.types.enumtypes.EventType.class));
        newVO.setEventId(oldVO.getEventId());
        newVO.setEventDescription(oldVO.getEventDescription());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.internal.vo.VOParameter convertToUp(
            org.oscm.vo.VOParameter oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.internal.vo.VOParameter newVO = new org.oscm.internal.vo.VOParameter();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setParameterDefinition(
                convertToUp(oldVO.getParameterDefinition()));
        newVO.setValue(oldVO.getValue());
        newVO.setConfigurable(oldVO.isConfigurable());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.vo.VOParameter convertToApi(
            org.oscm.internal.vo.VOParameter oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.vo.VOParameter newVO = new org.oscm.vo.VOParameter();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setParameterDefinition(
                convertToApi(oldVO.getParameterDefinition()));
        newVO.setValue(oldVO.getValue());
        newVO.setConfigurable(oldVO.isConfigurable());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.internal.vo.VOPriceModel convertToUp(
            org.oscm.vo.VOPriceModel oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.internal.vo.VOPriceModel newVO = new org.oscm.internal.vo.VOPriceModel();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setDescription(oldVO.getDescription());
        newVO.setConsideredEvents(
                convertToUpVOPricedEvent(oldVO.getConsideredEvents()));
        newVO.setSelectedParameters(
                convertToUpVOPricedParameter(oldVO.getSelectedParameters()));
        newVO.setPeriod(EnumConverter.convert(oldVO.getPeriod(),
                org.oscm.internal.types.enumtypes.PricingPeriod.class));
        newVO.setPricePerPeriod(oldVO.getPricePerPeriod());
        newVO.setPricePerUserAssignment(oldVO.getPricePerUserAssignment());
        newVO.setCurrencyISOCode(oldVO.getCurrencyISOCode());
        newVO.setOneTimeFee(oldVO.getOneTimeFee());
        newVO.setRoleSpecificUserPrices(
                convertToUpVOPricedRole(oldVO.getRoleSpecificUserPrices()));
        newVO.setSteppedPrices(
                convertToUpVOSteppedPrice(oldVO.getSteppedPrices()));
        newVO.setLicense(oldVO.getLicense());
        newVO.setFreePeriod(oldVO.getFreePeriod());
        newVO.setType(EnumConverter.convert(oldVO.getType(),
                org.oscm.internal.types.enumtypes.PriceModelType.class));
        newVO.setExternal(oldVO.isExternal());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.vo.VOPriceModel convertToApi(
            org.oscm.internal.vo.VOPriceModel oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.vo.VOPriceModel newVO = new org.oscm.vo.VOPriceModel();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setDescription(oldVO.getDescription());
        newVO.setConsideredEvents(
                convertToApiVOPricedEvent(oldVO.getConsideredEvents()));
        newVO.setSelectedParameters(
                convertToApiVOPricedParameter(oldVO.getSelectedParameters()));
        newVO.setPeriod(EnumConverter.convert(oldVO.getPeriod(),
                org.oscm.types.enumtypes.PricingPeriod.class));
        newVO.setPricePerPeriod(oldVO.getPricePerPeriod());
        newVO.setPricePerUserAssignment(oldVO.getPricePerUserAssignment());
        newVO.setCurrencyISOCode(oldVO.getCurrencyISOCode());
        newVO.setOneTimeFee(oldVO.getOneTimeFee());
        newVO.setRoleSpecificUserPrices(
                convertToApiVOPricedRole(oldVO.getRoleSpecificUserPrices()));
        newVO.setSteppedPrices(
                convertToApiVOSteppedPrice(oldVO.getSteppedPrices()));
        newVO.setLicense(oldVO.getLicense());
        newVO.setFreePeriod(oldVO.getFreePeriod());
        newVO.setType(EnumConverter.convert(oldVO.getType(),
                org.oscm.types.enumtypes.PriceModelType.class));
        newVO.setExternal(oldVO.isExternal());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.internal.vo.VOUsageLicense convertToUp(
            org.oscm.vo.VOUsageLicense oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.internal.vo.VOUsageLicense newVO = new org.oscm.internal.vo.VOUsageLicense();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setUser(convertToUp(oldVO.getUser()));
        newVO.setApplicationUserId(oldVO.getApplicationUserId());
        newVO.setRoleDefinition(convertToUp(oldVO.getRoleDefinition()));
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.vo.VOUsageLicense convertToApi(
            org.oscm.internal.vo.VOUsageLicense oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.vo.VOUsageLicense newVO = new org.oscm.vo.VOUsageLicense();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setUser(convertToApi(oldVO.getUser()));
        newVO.setApplicationUserId(oldVO.getApplicationUserId());
        newVO.setRoleDefinition(convertToApi(oldVO.getRoleDefinition()));
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.internal.vo.VOLocalizedText convertToUp(
            org.oscm.vo.VOLocalizedText oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.internal.vo.VOLocalizedText newVO = new org.oscm.internal.vo.VOLocalizedText();
        newVO.setLocale(oldVO.getLocale());
        newVO.setText(oldVO.getText());
        newVO.setVersion(oldVO.getVersion());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.vo.VOLocalizedText convertToApi(
            org.oscm.internal.vo.VOLocalizedText oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.vo.VOLocalizedText newVO = new org.oscm.vo.VOLocalizedText();
        newVO.setLocale(oldVO.getLocale());
        newVO.setText(oldVO.getText());
        newVO.setVersion(oldVO.getVersion());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.internal.vo.VOUser convertToUp(
            org.oscm.vo.VOUser oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.internal.vo.VOUser newVO = new org.oscm.internal.vo.VOUser();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setOrganizationId(oldVO.getOrganizationId());
        newVO.setUserId(oldVO.getUserId());
        newVO.setStatus(EnumConverter.convert(oldVO.getStatus(),
                org.oscm.internal.types.enumtypes.UserAccountStatus.class));
        newVO.setOrganizationRoles(EnumConverter.convertSet(
                oldVO.getOrganizationRoles(),
                org.oscm.internal.types.enumtypes.OrganizationRoleType.class));
        newVO.setUserRoles(EnumConverter.convertSet(oldVO.getUserRoles(),
                org.oscm.internal.types.enumtypes.UserRoleType.class));
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.vo.VOUser convertToApi(
            org.oscm.internal.vo.VOUser oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.vo.VOUser newVO = new org.oscm.vo.VOUser();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setOrganizationId(oldVO.getOrganizationId());
        newVO.setUserId(oldVO.getUserId());
        newVO.setStatus(EnumConverter.convert(oldVO.getStatus(),
                org.oscm.types.enumtypes.UserAccountStatus.class));
        newVO.setOrganizationRoles(
                EnumConverter.convertSet(oldVO.getOrganizationRoles(),
                        org.oscm.types.enumtypes.OrganizationRoleType.class));
        newVO.setUserRoles(EnumConverter.convertSet(oldVO.getUserRoles(),
                org.oscm.types.enumtypes.UserRoleType.class));
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.internal.vo.VOCustomerService convertToUp(
            org.oscm.vo.VOCustomerService oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.internal.vo.VOCustomerService newVO = new org.oscm.internal.vo.VOCustomerService();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setParameters(convertToUpVOParameter(oldVO.getParameters()));
        newVO.setDescription(oldVO.getDescription());
        newVO.setName(oldVO.getName());
        newVO.setServiceId(oldVO.getServiceId());
        newVO.setTechnicalId(oldVO.getTechnicalId());
        newVO.setFeatureURL(oldVO.getFeatureURL());
        newVO.setBaseURL(oldVO.getBaseURL());
        newVO.setPriceModel(convertToUp(oldVO.getPriceModel()));
        newVO.setStatus(EnumConverter.convert(oldVO.getStatus(),
                org.oscm.internal.types.enumtypes.ServiceStatus.class));
        newVO.setAccessType(EnumConverter.convert(oldVO.getAccessType(),
                org.oscm.internal.types.enumtypes.ServiceAccessType.class));
        newVO.setSellerId(oldVO.getSellerId());
        newVO.setSellerName(oldVO.getSellerName());
        newVO.setSellerKey(oldVO.getSellerKey());
        newVO.setTags(oldVO.getTags());
        newVO.setShortDescription(oldVO.getShortDescription());
        newVO.setAverageRating(oldVO.getAverageRating());
        newVO.setNumberOfReviews(oldVO.getNumberOfReviews());
        newVO.setOfferingType(EnumConverter.convert(oldVO.getOfferingType(),
                org.oscm.internal.types.enumtypes.OfferingType.class));
        newVO.setOrganizationId(oldVO.getOrganizationId());
        newVO.setOrganizationKey(oldVO.getOrganizationKey());
        newVO.setOrganizationName(oldVO.getOrganizationName());
        newVO.setName(oldVO.getName());
        newVO.setConfiguratorUrl(oldVO.getConfiguratorUrl());
        newVO.setCustomTabUrl(oldVO.getCustomTabUrl());
        newVO.setCustomTabName(oldVO.getCustomTabName());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.vo.VOCustomerService convertToApi(
            org.oscm.internal.vo.VOCustomerService oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.vo.VOCustomerService newVO = new org.oscm.vo.VOCustomerService();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setParameters(convertToApiVOParameter(oldVO.getParameters()));
        newVO.setDescription(oldVO.getDescription());
        newVO.setName(oldVO.getName());
        newVO.setServiceId(oldVO.getServiceId());
        newVO.setTechnicalId(oldVO.getTechnicalId());
        newVO.setFeatureURL(oldVO.getFeatureURL());
        newVO.setBaseURL(oldVO.getBaseURL());
        newVO.setPriceModel(convertToApi(oldVO.getPriceModel()));
        newVO.setStatus(EnumConverter.convert(oldVO.getStatus(),
                org.oscm.types.enumtypes.ServiceStatus.class));
        newVO.setAccessType(EnumConverter.convert(oldVO.getAccessType(),
                org.oscm.types.enumtypes.ServiceAccessType.class));
        newVO.setSellerId(oldVO.getSellerId());
        newVO.setSellerName(oldVO.getSellerName());
        newVO.setSellerKey(oldVO.getSellerKey());
        newVO.setTags(oldVO.getTags());
        newVO.setShortDescription(oldVO.getShortDescription());
        newVO.setAverageRating(oldVO.getAverageRating());
        newVO.setNumberOfReviews(oldVO.getNumberOfReviews());
        newVO.setOfferingType(EnumConverter.convert(oldVO.getOfferingType(),
                org.oscm.types.enumtypes.OfferingType.class));
        newVO.setOrganizationId(oldVO.getOrganizationId());
        newVO.setOrganizationKey(oldVO.getOrganizationKey());
        newVO.setOrganizationName(oldVO.getOrganizationName());
        newVO.setConfiguratorUrl(oldVO.getConfiguratorUrl());
        newVO.setCustomTabUrl(oldVO.getCustomTabUrl());
        newVO.setCustomTabName(oldVO.getCustomTabName());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.internal.vo.VOSubscriptionIdAndOrganizations convertToUp(
            org.oscm.vo.VOSubscriptionIdAndOrganizations oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.internal.vo.VOSubscriptionIdAndOrganizations newVO = new org.oscm.internal.vo.VOSubscriptionIdAndOrganizations();
        newVO.setSubscriptionId(oldVO.getSubscriptionId());
        newVO.setOrganizations(
                convertToUpVOOrganization(oldVO.getOrganizations()));
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.vo.VOSubscriptionIdAndOrganizations convertToApi(
            org.oscm.internal.vo.VOSubscriptionIdAndOrganizations oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.vo.VOSubscriptionIdAndOrganizations newVO = new org.oscm.vo.VOSubscriptionIdAndOrganizations();
        newVO.setSubscriptionId(oldVO.getSubscriptionId());
        newVO.setOrganizations(
                convertToApiVOOrganization(oldVO.getOrganizations()));
        return newVO;
    }

    /**
     * Convert list of VOPricedRole.
     * 
     * @param oldVO
     *            List of VO to convert.
     * @return Converted list of VO.
     */
    public static List<org.oscm.internal.vo.VOPricedRole> convertToUpVOPricedRole(
            List<org.oscm.vo.VOPricedRole> oldVO) {
        if (oldVO == null) {
            return null;
        }
        List<org.oscm.internal.vo.VOPricedRole> newVO = new ArrayList<>();
        for (org.oscm.vo.VOPricedRole tmp : oldVO) {
            newVO.add(convertToUp(tmp));
        }
        return newVO;
    }

    /**
     * Convert list of VOPricedRole.
     * 
     * @param oldVO
     *            List of VO to convert.
     * @return Converted list of VO.
     */
    public static List<org.oscm.vo.VOPricedRole> convertToApiVOPricedRole(
            List<org.oscm.internal.vo.VOPricedRole> oldVO) {
        if (oldVO == null) {
            return null;
        }
        List<org.oscm.vo.VOPricedRole> newVO = new ArrayList<>();
        for (org.oscm.internal.vo.VOPricedRole tmp : oldVO) {
            newVO.add(convertToApi(tmp));
        }
        return newVO;
    }

    /**
     * Convert list of VOTechnicalServiceOperation.
     * 
     * @param oldVO
     *            List of VO to convert.
     * @return Converted list of VO.
     */
    public static List<org.oscm.internal.vo.VOTechnicalServiceOperation> convertToUpVOTechnicalServiceOperation(
            List<org.oscm.vo.VOTechnicalServiceOperation> oldVO) {
        if (oldVO == null) {
            return null;
        }
        List<org.oscm.internal.vo.VOTechnicalServiceOperation> newVO = new ArrayList<>();
        for (org.oscm.vo.VOTechnicalServiceOperation tmp : oldVO) {
            newVO.add(convertToUp(tmp));
        }
        return newVO;
    }

    /**
     * Convert list of VOTechnicalServiceOperation.
     * 
     * @param oldVO
     *            List of VO to convert.
     * @return Converted list of VO.
     */
    public static List<org.oscm.vo.VOTechnicalServiceOperation> convertToApiVOTechnicalServiceOperation(
            List<org.oscm.internal.vo.VOTechnicalServiceOperation> oldVO) {
        if (oldVO == null) {
            return null;
        }
        List<org.oscm.vo.VOTechnicalServiceOperation> newVO = new ArrayList<>();
        for (org.oscm.internal.vo.VOTechnicalServiceOperation tmp : oldVO) {
            newVO.add(convertToApi(tmp));
        }
        return newVO;
    }

    /**
     * Convert list of VOParameterDefinition.
     * 
     * @param oldVO
     *            List of VO to convert.
     * @return Converted list of VO.
     */
    public static List<org.oscm.internal.vo.VOParameterDefinition> convertToUpVOParameterDefinition(
            List<org.oscm.vo.VOParameterDefinition> oldVO) {
        if (oldVO == null) {
            return null;
        }
        List<org.oscm.internal.vo.VOParameterDefinition> newVO = new ArrayList<>();
        for (org.oscm.vo.VOParameterDefinition tmp : oldVO) {
            newVO.add(convertToUp(tmp));
        }
        return newVO;
    }

    /**
     * Convert list of VOParameterDefinition.
     * 
     * @param oldVO
     *            List of VO to convert.
     * @return Converted list of VO.
     */
    public static List<org.oscm.vo.VOParameterDefinition> convertToApiVOParameterDefinition(
            List<org.oscm.internal.vo.VOParameterDefinition> oldVO) {
        if (oldVO == null) {
            return null;
        }
        List<org.oscm.vo.VOParameterDefinition> newVO = new ArrayList<>();
        for (org.oscm.internal.vo.VOParameterDefinition tmp : oldVO) {
            newVO.add(convertToApi(tmp));
        }
        return newVO;
    }

    /**
     * Convert list of VOOrganization.
     * 
     * @param oldVO
     *            List of VO to convert.
     * @return Converted list of VO.
     */
    public static List<org.oscm.internal.vo.VOOrganization> convertToUpVOOrganization(
            List<org.oscm.vo.VOOrganization> oldVO) {
        if (oldVO == null) {
            return null;
        }
        List<org.oscm.internal.vo.VOOrganization> newVO = new ArrayList<>();
        for (org.oscm.vo.VOOrganization tmp : oldVO) {
            newVO.add(convertToUp(tmp));
        }
        return newVO;
    }

    /**
     * Convert list of VOOrganization.
     * 
     * @param oldVO
     *            List of VO to convert.
     * @return Converted list of VO.
     */
    public static List<org.oscm.vo.VOOrganization> convertToApiVOOrganization(
            List<org.oscm.internal.vo.VOOrganization> oldVO) {
        if (oldVO == null) {
            return null;
        }
        List<org.oscm.vo.VOOrganization> newVO = new ArrayList<>();
        for (org.oscm.internal.vo.VOOrganization tmp : oldVO) {
            newVO.add(convertToApi(tmp));
        }
        return newVO;
    }

    /**
     * Convert list of VOParameterOption.
     * 
     * @param oldVO
     *            List of VO to convert.
     * @return Converted list of VO.
     */
    public static List<org.oscm.internal.vo.VOParameterOption> convertToUpVOParameterOption(
            List<org.oscm.vo.VOParameterOption> oldVO) {
        if (oldVO == null) {
            return null;
        }
        List<org.oscm.internal.vo.VOParameterOption> newVO = new ArrayList<>();
        for (org.oscm.vo.VOParameterOption tmp : oldVO) {
            newVO.add(convertToUp(tmp));
        }
        return newVO;
    }

    /**
     * Convert list of VOParameterOption.
     * 
     * @param oldVO
     *            List of VO to convert.
     * @return Converted list of VO.
     */
    public static List<org.oscm.vo.VOParameterOption> convertToApiVOParameterOption(
            List<org.oscm.internal.vo.VOParameterOption> oldVO) {
        if (oldVO == null) {
            return null;
        }
        List<org.oscm.vo.VOParameterOption> newVO = new ArrayList<>();
        for (org.oscm.internal.vo.VOParameterOption tmp : oldVO) {
            newVO.add(convertToApi(tmp));
        }
        return newVO;
    }

    /**
     * Convert list of VOPricedParameter.
     * 
     * @param oldVO
     *            List of VO to convert.
     * @return Converted list of VO.
     */
    public static List<org.oscm.internal.vo.VOPricedParameter> convertToUpVOPricedParameter(
            List<org.oscm.vo.VOPricedParameter> oldVO) {
        if (oldVO == null) {
            return null;
        }
        List<org.oscm.internal.vo.VOPricedParameter> newVO = new ArrayList<>();
        for (org.oscm.vo.VOPricedParameter tmp : oldVO) {
            newVO.add(convertToUp(tmp));
        }
        return newVO;
    }

    /**
     * Convert list of VOPricedParameter.
     * 
     * @param oldVO
     *            List of VO to convert.
     * @return Converted list of VO.
     */
    public static List<org.oscm.vo.VOPricedParameter> convertToApiVOPricedParameter(
            List<org.oscm.internal.vo.VOPricedParameter> oldVO) {
        if (oldVO == null) {
            return null;
        }
        List<org.oscm.vo.VOPricedParameter> newVO = new ArrayList<>();
        for (org.oscm.internal.vo.VOPricedParameter tmp : oldVO) {
            newVO.add(convertToApi(tmp));
        }
        return newVO;
    }

    /**
     * Convert list of VOPricedOption.
     * 
     * @param oldVO
     *            List of VO to convert.
     * @return Converted list of VO.
     */
    public static List<org.oscm.internal.vo.VOPricedOption> convertToUpVOPricedOption(
            List<org.oscm.vo.VOPricedOption> oldVO) {
        if (oldVO == null) {
            return null;
        }
        List<org.oscm.internal.vo.VOPricedOption> newVO = new ArrayList<>();
        for (org.oscm.vo.VOPricedOption tmp : oldVO) {
            newVO.add(convertToUp(tmp));
        }
        return newVO;
    }

    /**
     * Convert list of VOPricedOption.
     * 
     * @param oldVO
     *            List of VO to convert.
     * @return Converted list of VO.
     */
    public static List<org.oscm.vo.VOPricedOption> convertToApiVOPricedOption(
            List<org.oscm.internal.vo.VOPricedOption> oldVO) {
        if (oldVO == null) {
            return null;
        }
        List<org.oscm.vo.VOPricedOption> newVO = new ArrayList<>();
        for (org.oscm.internal.vo.VOPricedOption tmp : oldVO) {
            newVO.add(convertToApi(tmp));
        }
        return newVO;
    }

    /**
     * Convert list of VOPaymentType.
     * 
     * @param oldVO
     *            List of VO to convert.
     * @return Converted list of VO.
     */
    public static Set<org.oscm.internal.vo.VOPaymentType> convertToUpVOPaymentType(
            Set<org.oscm.vo.VOPaymentType> oldVO) {
        if (oldVO == null) {
            return null;
        }
        Set<org.oscm.internal.vo.VOPaymentType> newVO = new HashSet<>();
        for (org.oscm.vo.VOPaymentType tmp : oldVO) {
            newVO.add(convertToUp(tmp));
        }
        return newVO;
    }

    /**
     * Convert list of VOPaymentType.
     * 
     * @param oldVO
     *            List of VO to convert.
     * @return Converted list of VO.
     */
    public static Set<org.oscm.vo.VOPaymentType> convertToApiVOPaymentType(
            Set<org.oscm.internal.vo.VOPaymentType> oldVO) {
        if (oldVO == null) {
            return null;
        }
        Set<org.oscm.vo.VOPaymentType> newVO = new HashSet<>();
        for (org.oscm.internal.vo.VOPaymentType tmp : oldVO) {
            newVO.add(convertToApi(tmp));
        }
        return newVO;
    }

    /**
     * Convert list of VOSteppedPrice.
     * 
     * @param oldVO
     *            List of VO to convert.
     * @return Converted list of VO.
     */
    public static List<org.oscm.internal.vo.VOSteppedPrice> convertToUpVOSteppedPrice(
            List<org.oscm.vo.VOSteppedPrice> oldVO) {
        if (oldVO == null) {
            return null;
        }
        List<org.oscm.internal.vo.VOSteppedPrice> newVO = new ArrayList<>();
        for (org.oscm.vo.VOSteppedPrice tmp : oldVO) {
            newVO.add(convertToUp(tmp));
        }
        return newVO;
    }

    /**
     * Convert list of VOSteppedPrice.
     * 
     * @param oldVO
     *            List of VO to convert.
     * @return Converted list of VO.
     */
    public static List<org.oscm.vo.VOSteppedPrice> convertToApiVOSteppedPrice(
            List<org.oscm.internal.vo.VOSteppedPrice> oldVO) {
        if (oldVO == null) {
            return null;
        }
        List<org.oscm.vo.VOSteppedPrice> newVO = new ArrayList<>();
        for (org.oscm.internal.vo.VOSteppedPrice tmp : oldVO) {
            newVO.add(convertToApi(tmp));
        }
        return newVO;
    }

    /**
     * Convert list of VOPricedEvent.
     * 
     * @param oldVO
     *            List of VO to convert.
     * @return Converted list of VO.
     */
    public static List<org.oscm.internal.vo.VOPricedEvent> convertToUpVOPricedEvent(
            List<org.oscm.vo.VOPricedEvent> oldVO) {
        if (oldVO == null) {
            return null;
        }
        List<org.oscm.internal.vo.VOPricedEvent> newVO = new ArrayList<>();
        for (org.oscm.vo.VOPricedEvent tmp : oldVO) {
            newVO.add(convertToUp(tmp));
        }
        return newVO;
    }

    /**
     * Convert list of VOPricedEvent.
     * 
     * @param oldVO
     *            List of VO to convert.
     * @return Converted list of VO.
     */
    public static List<org.oscm.vo.VOPricedEvent> convertToApiVOPricedEvent(
            List<org.oscm.internal.vo.VOPricedEvent> oldVO) {
        if (oldVO == null) {
            return null;
        }
        List<org.oscm.vo.VOPricedEvent> newVO = new ArrayList<>();
        for (org.oscm.internal.vo.VOPricedEvent tmp : oldVO) {
            newVO.add(convertToApi(tmp));
        }
        return newVO;
    }

    /**
     * Convert list of VORoleDefinition.
     * 
     * @param oldVO
     *            List of VO to convert.
     * @return Converted list of VO.
     */
    public static List<org.oscm.internal.vo.VORoleDefinition> convertToUpVORoleDefinition(
            List<org.oscm.vo.VORoleDefinition> oldVO) {
        if (oldVO == null) {
            return null;
        }
        List<org.oscm.internal.vo.VORoleDefinition> newVO = new ArrayList<>();
        for (org.oscm.vo.VORoleDefinition tmp : oldVO) {
            newVO.add(convertToUp(tmp));
        }
        return newVO;
    }

    /**
     * Convert list of VORoleDefinition.
     * 
     * @param oldVO
     *            List of VO to convert.
     * @return Converted list of VO.
     */
    public static List<org.oscm.vo.VORoleDefinition> convertToApiVORoleDefinition(
            List<org.oscm.internal.vo.VORoleDefinition> oldVO) {
        if (oldVO == null) {
            return null;
        }
        List<org.oscm.vo.VORoleDefinition> newVO = new ArrayList<>();
        for (org.oscm.internal.vo.VORoleDefinition tmp : oldVO) {
            newVO.add(convertToApi(tmp));
        }
        return newVO;
    }

    /**
     * Convert list of VOEventDefinition.
     * 
     * @param oldVO
     *            List of VO to convert.
     * @return Converted list of VO.
     */
    public static List<org.oscm.internal.vo.VOEventDefinition> convertToUpVOEventDefinition(
            List<org.oscm.vo.VOEventDefinition> oldVO) {
        if (oldVO == null) {
            return null;
        }
        List<org.oscm.internal.vo.VOEventDefinition> newVO = new ArrayList<>();
        for (org.oscm.vo.VOEventDefinition tmp : oldVO) {
            newVO.add(convertToUp(tmp));
        }
        return newVO;
    }

    /**
     * Convert list of VOEventDefinition.
     * 
     * @param oldVO
     *            List of VO to convert.
     * @return Converted list of VO.
     */
    public static List<org.oscm.vo.VOEventDefinition> convertToApiVOEventDefinition(
            List<org.oscm.internal.vo.VOEventDefinition> oldVO) {
        if (oldVO == null) {
            return null;
        }
        List<org.oscm.vo.VOEventDefinition> newVO = new ArrayList<>();
        for (org.oscm.internal.vo.VOEventDefinition tmp : oldVO) {
            newVO.add(convertToApi(tmp));
        }
        return newVO;
    }

    /**
     * Convert list of VOParameter.
     * 
     * @param oldVO
     *            List of VO to convert.
     * @return Converted list of VO.
     */
    public static List<org.oscm.internal.vo.VOParameter> convertToUpVOParameter(
            List<org.oscm.vo.VOParameter> oldVO) {
        if (oldVO == null) {
            return null;
        }
        List<org.oscm.internal.vo.VOParameter> newVO = new ArrayList<>();
        for (org.oscm.vo.VOParameter tmp : oldVO) {
            newVO.add(convertToUp(tmp));
        }
        return newVO;
    }

    /**
     * Convert list of VOParameter.
     * 
     * @param oldVO
     *            List of VO to convert.
     * @return Converted list of VO.
     */
    public static List<org.oscm.vo.VOParameter> convertToApiVOParameter(
            List<org.oscm.internal.vo.VOParameter> oldVO) {
        if (oldVO == null) {
            return null;
        }
        List<org.oscm.vo.VOParameter> newVO = new ArrayList<>();
        for (org.oscm.internal.vo.VOParameter tmp : oldVO) {
            newVO.add(convertToApi(tmp));
        }
        return newVO;
    }

    /**
     * Convert list of VOUsageLicense.
     * 
     * @param oldVO
     *            List of VO to convert.
     * @return Converted list of VO.
     */
    public static List<org.oscm.internal.vo.VOUsageLicense> convertToUpVOUsageLicense(
            List<org.oscm.vo.VOUsageLicense> oldVO) {
        if (oldVO == null) {
            return null;
        }
        List<org.oscm.internal.vo.VOUsageLicense> newVO = new ArrayList<>();
        for (org.oscm.vo.VOUsageLicense tmp : oldVO) {
            newVO.add(convertToUp(tmp));
        }
        return newVO;
    }

    /**
     * Convert list of VOUsageLicense.
     * 
     * @param oldVO
     *            List of VO to convert.
     * @return Converted list of VO.
     */
    public static List<org.oscm.vo.VOUsageLicense> convertToVOUsageLicense(
            List<org.oscm.internal.vo.VOUsageLicense> oldVO) {
        if (oldVO == null) {
            return null;
        }
        List<org.oscm.vo.VOUsageLicense> newVO = new ArrayList<>();
        for (org.oscm.internal.vo.VOUsageLicense tmp : oldVO) {
            newVO.add(convertToApi(tmp));
        }
        return newVO;
    }

    /**
     * Convert list of VOLocalizedText.
     * 
     * @param oldVO
     *            List of VO to convert.
     * @return Converted list of VO.
     */
    public static List<org.oscm.internal.vo.VOLocalizedText> convertToUpVOLocalizedText(
            List<org.oscm.vo.VOLocalizedText> oldVO) {
        if (oldVO == null) {
            return null;
        }
        List<org.oscm.internal.vo.VOLocalizedText> newVO = new ArrayList<>();
        for (org.oscm.vo.VOLocalizedText tmp : oldVO) {
            newVO.add(convertToUp(tmp));
        }
        return newVO;
    }

    /**
     * Convert list of VOLocalizedText.
     * 
     * @param oldVO
     *            List of VO to convert.
     * @return Converted list of VO.
     */
    public static List<org.oscm.vo.VOLocalizedText> convertToVOLocalizedText(
            List<org.oscm.internal.vo.VOLocalizedText> oldVO) {
        if (oldVO == null) {
            return null;
        }
        List<org.oscm.vo.VOLocalizedText> newVO = new ArrayList<>();
        for (org.oscm.internal.vo.VOLocalizedText tmp : oldVO) {
            newVO.add(convertToApi(tmp));
        }
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.internal.vo.VOCatalogEntry convertToUp(
            org.oscm.vo.VOCatalogEntry oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.internal.vo.VOCatalogEntry newVO = new org.oscm.internal.vo.VOCatalogEntry();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setMarketplace(convertToUp(oldVO.getMarketplace()));
        newVO.setAnonymousVisible(oldVO.isAnonymousVisible());
        newVO.setCategories(convertToUpVOCategory(oldVO.getCategories()));
        newVO.setVisibleInCatalog(oldVO.isVisibleInCatalog());
        newVO.setService(convertToUp(oldVO.getService()));
        return newVO;
    }

    /**
     * Convert list of VOCategory.
     * 
     * @param oldVO
     *            List of VO to convert.
     * @return Converted list of VO.
     */
    public static List<org.oscm.internal.vo.VOCategory> convertToUpVOCategory(
            List<org.oscm.vo.VOCategory> oldVO) {
        if (oldVO == null) {
            return null;
        }
        List<org.oscm.internal.vo.VOCategory> newVO = new ArrayList<>();
        for (org.oscm.vo.VOCategory tmp : oldVO) {
            newVO.add(convertToUp(tmp));
        }
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.vo.VOCatalogEntry convertToApi(
            org.oscm.internal.vo.VOCatalogEntry oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.vo.VOCatalogEntry newVO = new org.oscm.vo.VOCatalogEntry();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setMarketplace(convertToApi(oldVO.getMarketplace()));
        newVO.setAnonymousVisible(oldVO.isAnonymousVisible());
        newVO.setCategories(convertToApiVOCategory(oldVO.getCategories()));
        newVO.setVisibleInCatalog(oldVO.isVisibleInCatalog());
        newVO.setService(convertToApi(oldVO.getService()));
        return newVO;
    }

    /**
     * Convert list of VOCategory.
     * 
     * @param oldVO
     *            List of VO to convert.
     * @return Converted list of VO.
     */
    public static List<org.oscm.vo.VOCategory> convertToApiVOCategory(
            List<org.oscm.internal.vo.VOCategory> oldVO) {
        if (oldVO == null) {
            return null;
        }
        List<org.oscm.vo.VOCategory> newVO = new ArrayList<>();
        for (org.oscm.internal.vo.VOCategory tmp : oldVO) {
            newVO.add(convertToApi(tmp));
        }
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.internal.vo.VOCategory convertToUp(
            org.oscm.vo.VOCategory oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.internal.vo.VOCategory newVO = new org.oscm.internal.vo.VOCategory();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setCategoryId(oldVO.getCategoryId());
        newVO.setMarketplaceId(oldVO.getMarketplaceId());
        newVO.setName(oldVO.getName());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.vo.VOCategory convertToApi(
            org.oscm.internal.vo.VOCategory oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.vo.VOCategory newVO = new org.oscm.vo.VOCategory();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setCategoryId(oldVO.getCategoryId());
        newVO.setMarketplaceId(oldVO.getMarketplaceId());
        newVO.setName(oldVO.getName());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.internal.vo.VOTag convertToUp(
            org.oscm.vo.VOTag oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.internal.vo.VOTag newVO = new org.oscm.internal.vo.VOTag();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setLocale(oldVO.getLocale());
        newVO.setValue(oldVO.getValue());
        newVO.setNumberReferences(oldVO.getNumberReferences());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.vo.VOTag convertToApi(
            org.oscm.internal.vo.VOTag oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.vo.VOTag newVO = new org.oscm.vo.VOTag();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setLocale(oldVO.getLocale());
        newVO.setValue(oldVO.getValue());
        newVO.setNumberReferences(oldVO.getNumberReferences());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.internal.vo.VOCompatibleService convertToUp(
            org.oscm.vo.VOCompatibleService oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.internal.vo.VOCompatibleService newVO = new org.oscm.internal.vo.VOCompatibleService();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setParameters(convertToUpVOParameter(oldVO.getParameters()));
        newVO.setDescription(oldVO.getDescription());
        newVO.setName(oldVO.getName());
        newVO.setServiceId(oldVO.getServiceId());
        newVO.setTechnicalId(oldVO.getTechnicalId());
        newVO.setFeatureURL(oldVO.getFeatureURL());
        newVO.setBaseURL(oldVO.getBaseURL());
        newVO.setPriceModel(convertToUp(oldVO.getPriceModel()));
        newVO.setStatus(EnumConverter.convert(oldVO.getStatus(),
                org.oscm.internal.types.enumtypes.ServiceStatus.class));
        newVO.setAccessType(EnumConverter.convert(oldVO.getAccessType(),
                org.oscm.internal.types.enumtypes.ServiceAccessType.class));
        newVO.setSellerId(oldVO.getSellerId());
        newVO.setSellerName(oldVO.getSellerName());
        newVO.setSellerKey(oldVO.getSellerKey());
        newVO.setTags(oldVO.getTags());
        newVO.setShortDescription(oldVO.getShortDescription());
        newVO.setAverageRating(oldVO.getAverageRating());
        newVO.setNumberOfReviews(oldVO.getNumberOfReviews());
        newVO.setOfferingType(EnumConverter.convert(oldVO.getOfferingType(),
                org.oscm.internal.types.enumtypes.OfferingType.class));
        newVO.setCompatible(oldVO.isCompatible());
        newVO.setConfiguratorUrl(oldVO.getConfiguratorUrl());
        newVO.setCustomTabUrl(oldVO.getCustomTabUrl());
        newVO.setCustomTabName(oldVO.getCustomTabName());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.vo.VOCompatibleService convertToApi(
            org.oscm.internal.vo.VOCompatibleService oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.vo.VOCompatibleService newVO = new org.oscm.vo.VOCompatibleService();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setParameters(convertToApiVOParameter(oldVO.getParameters()));
        newVO.setDescription(oldVO.getDescription());
        newVO.setName(oldVO.getName());
        newVO.setServiceId(oldVO.getServiceId());
        newVO.setTechnicalId(oldVO.getTechnicalId());
        newVO.setFeatureURL(oldVO.getFeatureURL());
        newVO.setBaseURL(oldVO.getBaseURL());
        newVO.setPriceModel(convertToApi(oldVO.getPriceModel()));
        newVO.setStatus(EnumConverter.convert(oldVO.getStatus(),
                org.oscm.types.enumtypes.ServiceStatus.class));
        newVO.setAccessType(EnumConverter.convert(oldVO.getAccessType(),
                org.oscm.types.enumtypes.ServiceAccessType.class));
        newVO.setSellerId(oldVO.getSellerId());
        newVO.setSellerName(oldVO.getSellerName());
        newVO.setSellerKey(oldVO.getSellerKey());
        newVO.setTags(oldVO.getTags());
        newVO.setShortDescription(oldVO.getShortDescription());
        newVO.setAverageRating(oldVO.getAverageRating());
        newVO.setNumberOfReviews(oldVO.getNumberOfReviews());
        newVO.setOfferingType(EnumConverter.convert(oldVO.getOfferingType(),
                org.oscm.types.enumtypes.OfferingType.class));
        newVO.setCompatible(oldVO.isCompatible());
        newVO.setConfiguratorUrl(oldVO.getConfiguratorUrl());
        newVO.setCustomTabUrl(oldVO.getCustomTabUrl());
        newVO.setCustomTabName(oldVO.getCustomTabName());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.internal.vo.VOCountryVatRate convertToUp(
            org.oscm.vo.VOCountryVatRate oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.internal.vo.VOCountryVatRate newVO = new org.oscm.internal.vo.VOCountryVatRate();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setRate(oldVO.getRate());
        newVO.setCountry(oldVO.getCountry());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.vo.VOCountryVatRate convertToApi(
            org.oscm.internal.vo.VOCountryVatRate oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.vo.VOCountryVatRate newVO = new org.oscm.vo.VOCountryVatRate();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setRate(oldVO.getRate());
        newVO.setCountry(oldVO.getCountry());
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.internal.vo.VOOrganizationVatRate convertToUp(
            org.oscm.vo.VOOrganizationVatRate oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.internal.vo.VOOrganizationVatRate newVO = new org.oscm.internal.vo.VOOrganizationVatRate();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setRate(oldVO.getRate());
        newVO.setOrganization(convertToUp(oldVO.getOrganization()));
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.vo.VOOrganizationVatRate convertToApi(
            org.oscm.internal.vo.VOOrganizationVatRate oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.vo.VOOrganizationVatRate newVO = new org.oscm.vo.VOOrganizationVatRate();
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());
        newVO.setRate(oldVO.getRate());
        newVO.setOrganization(convertToApi(oldVO.getOrganization()));
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.internal.vo.VOServiceActivation convertToUp(
            org.oscm.vo.VOServiceActivation oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.internal.vo.VOServiceActivation newVO = new org.oscm.internal.vo.VOServiceActivation();
        newVO.setActive(oldVO.isActive());
        newVO.setCatalogEntries(
                convertToUpVOCatalogEntry(oldVO.getCatalogEntries()));
        newVO.setService(convertToUp(oldVO.getService()));
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.vo.VOServiceActivation convertToApi(
            org.oscm.internal.vo.VOServiceActivation oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.vo.VOServiceActivation newVO = new org.oscm.vo.VOServiceActivation();
        newVO.setActive(oldVO.isActive());
        newVO.setCatalogEntries(
                convertToApiVOCatalogEntry(oldVO.getCatalogEntries()));
        newVO.setService(convertToApi(oldVO.getService()));
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.internal.vo.VOServicePaymentConfiguration convertToUp(
            org.oscm.vo.VOServicePaymentConfiguration oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.internal.vo.VOServicePaymentConfiguration newVO = new org.oscm.internal.vo.VOServicePaymentConfiguration();
        newVO.setEnabledPaymentTypes(
                convertToUpVOPaymentType(oldVO.getEnabledPaymentTypes()));
        newVO.setService(convertToUp(oldVO.getService()));
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     */
    public static org.oscm.vo.VOServicePaymentConfiguration convertToApi(
            org.oscm.internal.vo.VOServicePaymentConfiguration oldVO) {
        if (oldVO == null) {
            return null;
        }
        org.oscm.vo.VOServicePaymentConfiguration newVO = new org.oscm.vo.VOServicePaymentConfiguration();
        newVO.setEnabledPaymentTypes(
                convertToApiVOPaymentType(oldVO.getEnabledPaymentTypes()));
        newVO.setService(convertToApi(oldVO.getService()));
        return newVO;
    }

    /**
     * Convert source version VO to target version VO.
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     * @throws OperationNotPermittedException
     */
    @SuppressWarnings("unchecked")
    public static org.oscm.internal.vo.VOTriggerProcessParameter convertToUp(
            org.oscm.vo.VOTriggerProcessParameter oldVO)
            throws OperationNotPermittedException {
        if (oldVO == null) {
            return null;
        }

        ConversionStrategy<String, Object> strategy = TPPConversionStrategyFactory
                .getStrategy(oldVO.getType());

        org.oscm.internal.vo.VOTriggerProcessParameter newVO = new org.oscm.internal.vo.VOTriggerProcessParameter();
        newVO.setType(EnumConverter.convert(oldVO.getType(),
                org.oscm.internal.types.enumtypes.TriggerProcessParameterType.class));
        newVO.setValue(strategy.convert(oldVO.getValue()));
        newVO.setTriggerProcessKey(oldVO.getTriggerProcessKey());
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());

        return newVO;
    }

    /**
     * Convert source version VO to target version VO. S
     * 
     * @param oldVO
     *            VO to convert.
     * @return VO of target version.
     * @throws OperationNotPermittedException
     */
    @SuppressWarnings("unchecked")
    public static org.oscm.vo.VOTriggerProcessParameter convertToApi(
            org.oscm.internal.vo.VOTriggerProcessParameter oldVO)
            throws OperationNotPermittedException {
        if (oldVO == null) {
            return null;
        }

        ConversionStrategy<Object, String> strategy = TPPConversionStrategyFactory
                .getStrategy(oldVO.getType());

        org.oscm.vo.VOTriggerProcessParameter newVO = new org.oscm.vo.VOTriggerProcessParameter();
        newVO.setType(EnumConverter.convert(oldVO.getType(),
                TriggerProcessParameterType.class));
        newVO.setValue(strategy.convert(oldVO.getValue()));
        newVO.setTriggerProcessKey(oldVO.getTriggerProcessKey());
        newVO.setKey(oldVO.getKey());
        newVO.setVersion(oldVO.getVersion());

        return newVO;
    }

}
