/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: pock                                                      
 *                                                                              
 *  Creation Date: 17.11.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.vatservice.assembler;

import org.oscm.accountservice.assembler.OrganizationAssembler;
import org.oscm.domobjects.VatRate;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.validator.BLValidator;
import org.oscm.vo.BaseAssembler;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOCountryVatRate;
import org.oscm.internal.vo.VOOrganizationVatRate;
import org.oscm.internal.vo.VOVatRate;

/**
 * Assembler to convert a VAT rate domain objects into a the corresponding value
 * object and vice versa.
 * 
 * @author pock
 * 
 */
public class VatRateAssembler extends BaseAssembler {

    public static final String FIELD_NAME_ORGANIZATION = "organization";

    public static final String FIELD_NAME_COUNTRY = "country";

    public static final String FIELD_NAME_RATE = "rate";

    /**
     * Creates a new VOVatRate object and fills the fields with the
     * corresponding fields from the given domain object.
     * 
     * @param domObj
     *            The domain object containing the values to be set.
     * @return The created value object or null if the domain object was null.
     */
    public static VOVatRate toVOVatRate(VatRate domObj) {
        if (domObj == null) {
            return null;
        }
        VOVatRate vo = new VOVatRate();
        vo.setRate(domObj.getRate());

        updateValueObject(vo, domObj);
        return vo;
    }

    /**
     * Creates a new VOCountryVatRate object and fills the fields with the
     * corresponding fields from the given domain object.
     * 
     * @param domObj
     *            The domain object containing the values to be set.
     * @return The created value object or null if the domain object was null.
     */
    public static VOCountryVatRate toVOCountryVatRate(VatRate domObj) {
        if (domObj == null) {
            return null;
        }
        VOCountryVatRate vo = new VOCountryVatRate();
        vo.setRate(domObj.getRate());
        vo.setCountry(domObj.getTargetCountry().getCountryISOCode());

        updateValueObject(vo, domObj);
        return vo;
    }

    /**
     * Creates a new VOOrganizationVatRate object and fills the fields with the
     * corresponding fields from the given domain object.
     * 
     * @param domObj
     *            The domain object containing the values to be set.
     * @return The created value object or null if the domain object was null.
     */
    public static VOOrganizationVatRate toVOOrganizationVatRate(VatRate domObj,
            LocalizerFacade localizerFacade) {
        if (domObj == null) {
            return null;
        }
        VOOrganizationVatRate vo = new VOOrganizationVatRate();
        vo.setRate(domObj.getRate());
        vo.setOrganization(OrganizationAssembler.toVOOrganization(
                domObj.getTargetOrganization(), false, localizerFacade));

        updateValueObject(vo, domObj);
        return vo;
    }

    /**
     * Updates the fields in the VAT rate object to reflect the changes
     * performed in the value object.
     * 
     * @param domObj
     *            The domain object to be updated.
     * @param vo
     *            The value object.
     * @return The updated domain object.
     * @throws ValidationException
     *             Thrown if the validation of the value objects failed.
     * @throws ConcurrentModificationException
     *             Thrown if the object versions does not match.
     */
    public static VatRate updateVatRate(VatRate domObj, VOVatRate vo)
            throws ValidationException, ConcurrentModificationException {
        validate(vo);
        verifyVersionAndKey(domObj, vo);
        domObj.setRate(vo.getRate());
        return domObj;
    }

    /**
     * Validate a VAT rate value object.
     * 
     * @param vo
     *            the value object to validate
     * @throws ValidationException
     *             Thrown if the validation of the value objects failed.
     */
    static void validate(VOVatRate vo) throws ValidationException {
        BLValidator.isVat(FIELD_NAME_RATE, vo.getRate());
    }

}
