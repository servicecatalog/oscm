/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: cheld                                                      
 *                                                                              
 *  Creation Date: 15.02.2012                                                      
 *                                                                              
 *  Completion Time: 15.02.2012                                        
 *                                                                              
 *******************************************************************************/

package org.oscm.marketplace.assembler;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.domobjects.Category;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.validator.BLValidator;
import org.oscm.vo.BaseAssembler;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOCategory;

/**
 * Assembler to handle VOCategory <=> Category conversions.
 * 
 * @author cheld
 * 
 */
public class CategoryAssembler extends BaseAssembler {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(MarketplaceAssembler.class);

    public static VOCategory toVOCategory(Category domObj,
            LocalizerFacade facade) {
        if (domObj == null) {
            return null;
        }
        VOCategory voResult = new VOCategory();
        updateValueObject(voResult, domObj);
        voResult.setCategoryId(domObj.getCategoryId());
        voResult.setMarketplaceId(domObj.getMarketplace().getMarketplaceId());
        voResult.setName(facade.getText(domObj.getKey(),
                LocalizedObjectTypes.CATEGORY_NAME));
        return voResult;
    }

    public static Category updateCategory(Category domObj, VOCategory voObj)
            throws ValidationException, ConcurrentModificationException {
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
        BLValidator.isId("categoryId", voObj.getCategoryId(), true);
        domObj.setCategoryId(voObj.getCategoryId());
        return domObj;
    }

    public static void verifyCategoryUpdated(Category domObj, VOCategory voObj,
            LocalizerFacade facade) throws ConcurrentModificationException {
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
        String categoryNameInDB = facade.getText(domObj.getKey(),
                LocalizedObjectTypes.CATEGORY_NAME);
        if (!categoryNameInDB.equals(voObj.getName())) {
            ConcurrentModificationException cme = new ConcurrentModificationException(
                    voObj);
            logger.logWarn(Log4jLogger.SYSTEM_LOG, cme,
                    LogMessageIdentifier.WARN_CONCURRENT_MODIFICATION, voObj
                            .getClass().getSimpleName());
            throw cme;
        }
    }
}
