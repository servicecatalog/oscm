/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: pock                                                      
 *                                                                              
 *  Creation Date: 06.11.2009                                                      
 *                                                                              
 *  Completion Time: 06.11.2009                                              
 *                                                                              
 *******************************************************************************/
package org.oscm.brandservice.bean;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.ImageResource;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.local.ImageResourceServiceLocal;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.permission.PermissionCheck;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.validation.ArgumentValidator;
import org.oscm.validation.ImageValidator;
import org.oscm.internal.intf.BrandService;
import org.oscm.internal.types.enumtypes.ImageType;
import org.oscm.internal.types.enumtypes.ImageType.ImageOwnerType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOImageResource;
import org.oscm.internal.vo.VOLocalizedText;

/**
 * Session Bean implementation to manage the properties, translations and images
 * of a supplier.
 * 
 * 
 * @author pock
 * 
 */
@Stateless
@Remote(BrandService.class)
@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class })
public class BrandServiceBean implements BrandService {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(BrandServiceBean.class);

    @EJB(beanInterface = DataService.class)
    DataService dm;

    @EJB(beanInterface = ImageResourceServiceLocal.class)
    private ImageResourceServiceLocal irm;

    @EJB(beanInterface = LocalizerServiceLocal.class)
    LocalizerServiceLocal localizer;

    /**
     * Gets the marketplace object for the given marketplace id.
     * 
     * @param marketplaceId
     *            the identifier of the organization for which the marketplace
     *            object is requested.
     * @return the marketplace object or null.
     */
    private Marketplace getMarketplace(String marketplaceId) {

        if (marketplaceId == null || marketplaceId.trim().length() == 0) {
            return null;
        }
        Marketplace mp = new Marketplace();
        mp.setMarketplaceId(marketplaceId);
        try {
            mp = (Marketplace) dm.getReferenceByBusinessKey(mp);
        } catch (ObjectNotFoundException e) {
            logger.logWarn(Log4jLogger.SYSTEM_LOG | Log4jLogger.AUDIT_LOG, e,
                    LogMessageIdentifier.WARN_UNKNOWN_MARKETPLACE,
                    marketplaceId);
            mp = null;
        }

        return mp;
    }

    /**
     * Writes the properties into a string.
     * 
     * @param props
     *            the properties to be written.
     * @return the string representing the properties.
     */
    private String writeProperties(Properties props) {
        if (props == null) {
            return null;
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            props.store(outputStream, "");
            // Property files are always encoded in ISO-8859-1:
            return outputStream.toString("ISO-8859-1");
        } catch (IOException e) {
            SaaSSystemException se = new SaaSSystemException(
                    "Storing properties failed!", e);
            logger.logError(Log4jLogger.SYSTEM_LOG, se,
                    LogMessageIdentifier.ERROR_STORE_PROPERTIES_FAILED);
            throw se;
        }
    }

    @Override
    @RolesAllowed({ "MARKETPLACE_OWNER" })
    public void deleteImages(List<ImageType> imageTypes) {

        if (imageTypes == null) {

            return;
        }

        PlatformUser user = dm.getCurrentUser();
        Organization organization = user.getOrganization();
        Marketplace marketplace = getMarketplaceToWorkOn(organization, user);

        if (marketplace == null) {
            logger.logWarn(Log4jLogger.SYSTEM_LOG,
                    LogMessageIdentifier.WARN_ORGANIZATION_HAS_NO_MARKETPLACE,
                    organization.getOrganizationId());

            return;
        }

        for (ImageType imageType : imageTypes) {
            if (imageType.getOwnerType() == ImageOwnerType.SHOP) {
                irm.delete(marketplace.getKey(), imageType);
            } else {
                logger.logWarn(
                        Log4jLogger.SYSTEM_LOG,
                        LogMessageIdentifier.WARN_IMAGE_DELETION_FAILED_NOT_BELONG_MARKETPLACE);
            }
        }

    }

    private Marketplace getMarketplaceToWorkOn(Organization organization,
            PlatformUser user) {
        if (user.hasRole(UserRoleType.PLATFORM_OPERATOR)) {
            List<Marketplace> list = organization.getMarketplaces();
            if (!list.isEmpty()) {
                return list.get(0);
            }
        }
        return null;
    }

    @Override
    @RolesAllowed({ "MARKETPLACE_OWNER" })
    public void deleteAllMessageProperties(String marketplaceId)
            throws ObjectNotFoundException, OperationNotPermittedException {

        ArgumentValidator.notEmptyString("marketplaceId", marketplaceId);

        PlatformUser user = dm.getCurrentUser();
        Organization organization = user.getOrganization();

        Marketplace marketplace = new Marketplace();
        marketplace.setMarketplaceId(marketplaceId);
        marketplace = (Marketplace) dm.getReferenceByBusinessKey(marketplace);

        PermissionCheck.owns(marketplace, organization, logger, null);

        localizer.removeLocalizedValues(marketplace.getKey(),
                LocalizedObjectTypes.SHOP_MESSAGE_PROPERTIES);

    }

    @Override
    public VOImageResource loadImage(String marketplaceId, ImageType imageType) {

        VOImageResource vo = null;

        if (imageType.getOwnerType() != ImageOwnerType.SHOP) {
            logger.logWarn(
                    Log4jLogger.SYSTEM_LOG,
                    LogMessageIdentifier.WARN_IMAGE_LOAD_FAILED_NOT_BELONG_MARKETPLACE);

            return vo;
        }

        Marketplace shop = getMarketplace(marketplaceId);
        if (shop != null) {
            ImageResource imageResource = irm.read(shop.getKey(), imageType);
            if (imageResource != null) {
                vo = new VOImageResource();
                vo.setBuffer(imageResource.getBuffer());
                vo.setContentType(imageResource.getContentType());
                vo.setImageType(imageType);
            }
        }

        return vo;
    }

    @Override
    public Properties loadMessageProperties(String marketplaceId,
            String localeString) {

        Properties props = new Properties();

        Properties mailProps = localizer.loadLocalizedPropertiesFromFile(
                LocalizedObjectTypes.MAIL_CONTENT.getSourceLocation(),
                localeString);
        if (mailProps != null) {
            props.putAll(mailProps);
        }

        props.putAll(loadMessagePropertiesFromDB(marketplaceId, localeString));

        return props;
    }

    @Override
    public Properties loadMessagePropertiesFromDB(String marketplaceId,
            String localeString) {

        Properties props = new Properties();
        Marketplace shop = getMarketplace(marketplaceId);
        if (shop != null) {
            props.putAll(localizer.loadLocalizedPropertiesFromDatabase(0L,
                    LocalizedObjectTypes.MESSAGE_PROPERTIES, localeString));
            props.putAll(localizer.loadLocalizedPropertiesFromDatabase(0L,
                    LocalizedObjectTypes.MAIL_PROPERTIES, localeString));
            props.putAll(localizer.loadLocalizedPropertiesFromDatabase(
                    shop.getKey(),
                    LocalizedObjectTypes.SHOP_MESSAGE_PROPERTIES, localeString));
        }

        return props;
    }

    @Override
    @RolesAllowed({ "MARKETPLACE_OWNER" })
    public void saveImages(List<VOImageResource> voImageResources,
            String marketplaceId) throws ObjectNotFoundException,
            OperationNotPermittedException, ValidationException {

        if (voImageResources == null) {

            return;
        }

        Organization organization = dm.getCurrentUser().getOrganization();

        Marketplace marketplace = new Marketplace();
        marketplace.setMarketplaceId(marketplaceId);
        marketplace = (Marketplace) dm.getReferenceByBusinessKey(marketplace);

        PermissionCheck.owns(marketplace, organization, logger, null);

        for (VOImageResource vo : voImageResources) {
            if (vo.getImageType() != null
                    && vo.getImageType().getOwnerType() == ImageOwnerType.SHOP) {
                ImageValidator.validateImageType(vo.getBuffer(),
                        vo.getContentType());
                ImageResource imageResource = new ImageResource();
                imageResource.setObjectKey(marketplace.getKey());
                imageResource.setContentType(vo.getContentType());
                imageResource.setBuffer(vo.getBuffer());
                imageResource.setImageType(vo.getImageType());
                irm.save(imageResource);
            } else {
                logger.logWarn(
                        Log4jLogger.SYSTEM_LOG,
                        LogMessageIdentifier.WARN_IMAGE_SAVE_FAILED_NOT_BELONG_MARKETPLACE);
            }
        }

    }

    @Override
    @RolesAllowed({ "MARKETPLACE_OWNER" })
    public void saveMessageProperties(Map<String, Properties> propertiesMap,
            String marketplaceId) throws OperationNotPermittedException,
            ObjectNotFoundException {

        ArgumentValidator.notEmptyString("marketplaceId", marketplaceId);

        Organization organization = dm.getCurrentUser().getOrganization();

        Marketplace marketplace = new Marketplace();
        marketplace.setMarketplaceId(marketplaceId);
        marketplace = (Marketplace) dm.getReferenceByBusinessKey(marketplace);

        PermissionCheck.owns(marketplace, organization, logger, null);

        if (propertiesMap != null) {
            for (String localeString : propertiesMap.keySet()) {
                localizer.storeLocalizedResource(localeString,
                        marketplace.getKey(),
                        LocalizedObjectTypes.SHOP_MESSAGE_PROPERTIES,
                        writeProperties(propertiesMap.get(localeString)));
            }
        }

    }

    @Override
    public String getMarketplaceStage(String marketplaceId, String localeString) {

        Marketplace mp = getMarketplace(marketplaceId);
        if (mp == null) {
            // Return a empty string like the localizer does if there is no
            // localized resource available.
            return "";
        }
        String result = localizer.getLocalizedTextFromDatabase(localeString,
                mp.getKey(), LocalizedObjectTypes.MARKETPLACE_STAGE);

        return result;
    }

    @Override
    @RolesAllowed("MARKETPLACE_OWNER")
    public List<VOLocalizedText> getMarketplaceStageLocalization(
            String marketplaceId) throws ObjectNotFoundException,
            OperationNotPermittedException {

        List<VOLocalizedText> result = new ArrayList<VOLocalizedText>();
        Marketplace mp = new Marketplace();
        mp.setMarketplaceId(marketplaceId);
        mp = (Marketplace) dm.getReferenceByBusinessKey(mp);
        PermissionCheck.owns(mp, dm.getCurrentUser().getOrganization(), logger,
                null);
        result = localizer.getLocalizedValues(mp.getKey(),
                LocalizedObjectTypes.MARKETPLACE_STAGE);

        return result;
    }

    @Override
    @RolesAllowed({ "MARKETPLACE_OWNER" })
    public void setMarketplaceStage(String stageContent, String marketplaceId,
            String localeString) throws ObjectNotFoundException,
            OperationNotPermittedException {

        ArgumentValidator.notNull("stageContent", stageContent);
        ArgumentValidator.notNull("marketplaceId", marketplaceId);
        ArgumentValidator.notNull("localeString", localeString);

        Marketplace mp = new Marketplace();
        mp.setMarketplaceId(marketplaceId);

        mp = (Marketplace) dm.getReferenceByBusinessKey(mp);
        PermissionCheck.owns(mp, dm.getCurrentUser().getOrganization(), logger,
                null);
        if (stageContent.trim().length() == 0) {
            localizer.removeLocalizedValue(mp.getKey(),
                    LocalizedObjectTypes.MARKETPLACE_STAGE, localeString);
        } else {
            localizer.storeLocalizedResource(localeString, mp.getKey(),
                    LocalizedObjectTypes.MARKETPLACE_STAGE, stageContent);
        }

    }

}
