/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.accountservice.assembler;

import java.util.ArrayList;
import java.util.List;

import org.oscm.domobjects.BillingContact;
import org.oscm.validator.BLValidator;
import org.oscm.vo.BaseAssembler;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOBillingContact;

/**
 * @author weiser
 * 
 */
public class BillingContactAssembler extends BaseAssembler {

    /**
     * Converts the BillingContact domain object to a value object. if the
     * domain object is null, an empty value object having
     * <code>orgAddressUsed</code> set to <code>true</code> will be created.
     * Otherwise the value object is filled with the values from the provided
     * domain object.
     * 
     * @param billingContact
     *            the existing BillingContact domain object or <code>null</code>
     * @return the value object
     */
    public static VOBillingContact toVOBillingContact(
            BillingContact billingContact) {
        VOBillingContact voBillingContact = new VOBillingContact();
        if (billingContact == null) {
            voBillingContact.setOrgAddressUsed(true);
        } else {
            voBillingContact.setAddress(billingContact.getAddress());
            voBillingContact.setCompanyName(billingContact.getCompanyName());
            voBillingContact.setEmail(billingContact.getEmail());
            voBillingContact.setOrgAddressUsed(billingContact
                    .isOrgAddressUsed());
            voBillingContact.setId(billingContact.getBillingContactId());
            updateValueObject(voBillingContact, billingContact);
        }
        return voBillingContact;
    }

    /**
     * Converts the BillingContact domain object to a value object. if the
     * domain object is null, an empty value object having
     * <code>orgAddressUsed</code> set to <code>true</code> will be created.
     * Otherwise the value object is filled with the values from the provided
     * domain object.
     * 
     * @param billingContacts
     *            the existing BillingContact domain object or <code>null</code>
     * @return the value object
     */
    public static List<VOBillingContact> toVOBillingContacts(
            List<BillingContact> billingContacts) {
        List<VOBillingContact> voBillingContacts = new ArrayList<VOBillingContact>();
        if (billingContacts != null) {
            for (BillingContact billingContact : billingContacts) {
                voBillingContacts.add(toVOBillingContact(billingContact));
            }
        }
        return voBillingContacts;
    }

    /**
     * Updates the values of the provided {@link BillingContact} with the values
     * of the provided {@link VOBillingContact}.
     * 
     * @param bc
     *            the {@link BillingContact} to update
     * @param vobc
     *            the {@link VOBillingContact} to use the values from
     * @return the updated {@link BillingContact}
     * @throws ValidationException
     *             Thrown in case of invalid or missing data provided
     */
    public static BillingContact updateBillingContact(BillingContact bc,
            VOBillingContact vobc) throws ValidationException {
        validate(vobc);
        bc.setBillingContactId(vobc.getId());
        bc.setAddress(vobc.getAddress());
        bc.setCompanyName(vobc.getCompanyName());
        bc.setEmail(vobc.getEmail());
        bc.setOrgAddressUsed(vobc.isOrgAddressUsed());
        return bc;
    }

    /**
     * Checks if a valid email is set.
     * 
     * @param voBillingContact
     *            the value object to validate
     * @throws ValidationException
     *             in case an invalid email is set
     */
    private static void validate(VOBillingContact voBillingContact)
            throws ValidationException {
        BLValidator.isId("id", voBillingContact.getId(), true);
        BLValidator.isEmail("email", voBillingContact.getEmail(), true);
        BLValidator.isName("company name", voBillingContact.getCompanyName(),
                true);
        BLValidator.isDescription("address", voBillingContact.getAddress(),
                true);
    }

}
