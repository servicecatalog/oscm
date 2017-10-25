/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 08.09.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.marketplace.assembler;

import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.Tenant;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOMarketplace;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.validator.BLValidator;
import org.oscm.vo.BaseAssembler;

/**
 * Assembler to handle VOMarketplace <=> Marketplace conversions.
 * 
 * @author groch
 * 
 */
public class MarketplaceAssembler extends BaseAssembler {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(MarketplaceAssembler.class);

    /**
     * Creates a value object representing the current settings for the
     * marketplace.
     * 
     * @param marketplace
     *            The technical marketplace to be represented as value object.
     * @param facade
     *            The localizer facade object.
     * @return A value object representation of the given marketplace.
     */
    public static VOMarketplace toVOMarketplace(Marketplace marketplace,
            LocalizerFacade facade) {
        if (marketplace == null) {
            return null;
        }
        VOMarketplace voResult = new VOMarketplace();
        updateValueObject(voResult, marketplace);
        voResult.setMarketplaceId(marketplace.getMarketplaceId());
        voResult.setName(facade.getText(marketplace.getKey(),
                LocalizedObjectTypes.MARKETPLACE_NAME));
        voResult.setOpen(marketplace.isOpen());

        voResult.setTaggingEnabled(marketplace.isTaggingEnabled());
        voResult.setReviewEnabled(marketplace.isReviewEnabled());
        voResult.setSocialBookmarkEnabled(marketplace.isSocialBookmarkEnabled());
        voResult.setCategoriesEnabled(marketplace.isCategoriesEnabled());
        voResult.setRestricted(marketplace.isRestricted());
        voResult.setHasPublicLandingPage(marketplace.getPublicLandingpage() != null);

        Organization owner = marketplace.getOrganization();
        if (owner != null) {
            voResult.setOwningOrganizationId(owner.getOrganizationId());
            String name = owner.getName();
            // FIXME what is "<empty>" good for?
            voResult.setOwningOrganizationName(name != null ? name : "<empty>");
        }
        
        Tenant tenant = marketplace.getTenant();
        if (tenant != null) {
            voResult.setTenantId(tenant.getTenantId());
        }
        
        return voResult;
    }

    /**
     * Updates the fields in the Marketplace object to reflect the changes
     * performed in the value object.
     * 
     * @param domObj
     *            The domain object to be updated.
     * @param voObj
     *            The value object.
     * @return The updated domain object.
     * @throws ValidationException
     *             Thrown if the validation of the value objects failed.
     * @throws ConcurrentModificationException
     *             Thrown if the object versions do not match.
     * @throws ValidationException
     *             Thrown if the attributes to copy at the value object do not
     *             meet all constraints.
     */
    public static Marketplace updateMarketplace(Marketplace domObj,
            VOMarketplace voObj) throws ValidationException,
            ConcurrentModificationException {
        if (domObj == null || voObj == null) {
            IllegalArgumentException e = new IllegalArgumentException(
                    "Parameters must not be null");
            logger.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_PARAMETER_NULL);
            throw e;
        }
        if (domObj.getKey() != 0) {
            verifyVersionAndKey(domObj, voObj);
        }
        validate(voObj);
        copyAttributes(domObj, voObj);
        return domObj;
    }

    public static Marketplace toMarketplace(VOMarketplace voObj)
            throws ValidationException {
        final Marketplace domObj = new Marketplace();
        validate(voObj);
        copyAttributes(domObj, voObj);
        return domObj;
    }

    public static Marketplace toMarketplaceWithKey(VOMarketplace voObj)
        throws ValidationException {
        Marketplace mp = toMarketplace(voObj);
        mp.setKey(voObj.getKey());
        return mp;
    }

    public static void validate(VOMarketplace voObj) throws ValidationException {
        BLValidator.isId("marketplaceId", voObj.getMarketplaceId(), true);
    }

    private static void copyAttributes(Marketplace domObj, VOMarketplace voObj) {
        domObj.setMarketplaceId(voObj.getMarketplaceId());
        domObj.setOpen(voObj.isOpen());

        domObj.setTaggingEnabled(voObj.isTaggingEnabled());
        domObj.setReviewEnabled(voObj.isReviewEnabled());
        domObj.setSocialBookmarkEnabled(voObj.isSocialBookmarkEnabled());
        domObj.setCategoriesEnabled(voObj.isCategoriesEnabled());
    }
}
