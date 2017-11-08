/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 05.02.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.payproc.stubs;

import java.util.List;
import java.util.Properties;

import org.oscm.accountservice.local.AccountServiceLocal;
import org.oscm.domobjects.Discount;
import org.oscm.domobjects.ImageResource;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationReference;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.TriggerProcess;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.DeletionConstraintException;
import org.oscm.internal.types.exception.DistinguishedNameException;
import org.oscm.internal.types.exception.ImageException;
import org.oscm.internal.types.exception.MailOperationException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.types.exception.OrganizationAuthorityException;
import org.oscm.internal.types.exception.TechnicalServiceNotAliveException;
import org.oscm.internal.types.exception.TechnicalServiceOperationException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOImageResource;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOUserDetails;

/**
 * Test stub for the account management.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class AccountServiceStub implements AccountServiceLocal {

    boolean isOrgAuthorized = true;

    IdentityServiceStub im;

    public AccountServiceStub(IdentityServiceStub im) {
        this.im = im;
    }

    @Override
    public Organization addOrganizationToRole(String organizationId,
            OrganizationRoleType role) throws ObjectNotFoundException {

        return null;
    }

    @Override
    public Organization registerOrganization(Organization organization,
            ImageResource imageResource, VOUserDetails user,
            Properties organizationProperties, String domicileCountry,
            String marketplaceId, String description,
            OrganizationRoleType... roles)
            throws NonUniqueBusinessKeyException, ValidationException,
            MailOperationException, OrganizationAuthorityException {

        return null;
    }

    @Override
    public void removeOverdueOrganization(Organization organization)
            throws DeletionConstraintException, ObjectNotFoundException {

    }

    @Override
    public boolean removeOverdueOrganizations(long currentTime) {

        return false;
    }

    @Override
    public List<OrganizationReference> getOrganizationForDiscountEndNotificiation(
            long currentTimeMillis) {

        return null;
    }

    @Override
    public boolean sendDiscountEndNotificationMail(long currentTimeMillis)
            throws MailOperationException {
        return true;

    }

    @Override
    public void checkDistinguishedName(Organization organization)
            throws DistinguishedNameException {

    }

    @Override
    public VOOrganization registerKnownCustomerInt(TriggerProcess tp)
            throws OrganizationAuthoritiesException, ValidationException,
            NonUniqueBusinessKeyException, MailOperationException {

        return null;
    }

    @Override
    public void savePaymentConfigurationInt(TriggerProcess tp)
            throws ObjectNotFoundException {

    }

    @Override
    public void processImage(ImageResource imageResource, long organizationKey)
            throws ValidationException {

    }

    @Override
    public void updateAccountInformation(Organization organization,
            VOUserDetails user, String marketplaceId)
            throws ValidationException, NonUniqueBusinessKeyException,
            OperationNotPermittedException, TechnicalServiceNotAliveException,
            TechnicalServiceOperationException, DistinguishedNameException,
            ConcurrentModificationException {

    }

    @Override
    public Discount updateCustomerDiscount(Organization organization,
            Discount discount, Integer discountVersion)
            throws ObjectNotFoundException, ValidationException,
            OperationNotPermittedException, ConcurrentModificationException {

        return null;
    }

    @Override
    public List<PlatformUser> getOrganizationAdmins(long organizationKey) {
        return null;
    }

    @Override
    public void updateAccountInformation(VOOrganization organization,
            VOUserDetails user, String marketplaceId,
            VOImageResource imageResource) throws ValidationException,
            NonUniqueBusinessKeyException, OperationNotPermittedException,
            TechnicalServiceNotAliveException,
            TechnicalServiceOperationException, ObjectNotFoundException,
            DistinguishedNameException, ConcurrentModificationException,
            ImageException {
    }

    @Override
    public VOOrganization getOrganizationDataFallback() {
        return null;
    }

    @Override
    public boolean isPaymentTypeEnabled(long serviceKey, long paymentTypeKey)
            throws ObjectNotFoundException {
        return false;
    }

    @Override
    public boolean checkUserNum() throws MailOperationException {
        return false;
    }

}
