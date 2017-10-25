/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Enes Sejfi                      
 *                                                                              
 *  Creation Date: 11.06.2012                                                
 *                                                                              
 *******************************************************************************/

package org.oscm.marketplace.assembler;

import org.oscm.domobjects.LandingpageProduct;
import org.oscm.landingpageService.local.VOLandingpageService;
import org.oscm.validator.BLValidator;
import org.oscm.vo.BaseAssembler;
import org.oscm.internal.types.exception.ValidationException;

/**
 * Assembler to handle VOLandingpageService <=> LandingpageService conversions.
 * 
 * @author Enes Sejfi
 */
public class LandingpageProductAssembler extends BaseAssembler {

    /**
     * Creates a value object representing the current settings for the
     * landingpageservice.
     * 
     * @param domObj
     *            The technical marketplace to be represented as value object.
     * @return A value object representation of the given marketplace.
     */
    public static VOLandingpageService toVOLandingpageService(
            LandingpageProduct domObj) {
        if (domObj == null) {
            return null;
        }

        VOLandingpageService voObj = new VOLandingpageService();
        updateValueObject(voObj, domObj);
        voObj.setPosition(domObj.getPosition());
        return voObj;
    }

    /**
     * Converts a VOLandingpage object to a landingpage domain object
     * 
     * @param voObj
     *            VOLandingpage
     * @return domain object
     * @throws ValidationException
     *             Thrown if validation failed
     */
    public static LandingpageProduct toLandingpageProduct(
            VOLandingpageService voObj) throws ValidationException {
        final LandingpageProduct domObj = new LandingpageProduct();
        validate(voObj);
        domObj.setPosition(voObj.getPosition());
        return domObj;
    }

    /**
     * Validate a Landingpage VO object.
     * 
     * @param voObj
     *            Landingpage
     * @throws ValidationException
     *             Thrown if validation fails
     */
    public static void validate(VOLandingpageService voObj)
            throws ValidationException {
        BLValidator.isNonNegativeNumber("position", voObj.getPosition());
    }
}
