/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.internal.profile;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.oscm.accountservice.local.AccountServiceLocal;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.internal.intf.IdentityService;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.DistinguishedNameException;
import org.oscm.internal.types.exception.ImageException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.TechnicalServiceNotAliveException;
import org.oscm.internal.types.exception.TechnicalServiceOperationException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOImageResource;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOUserDetails;

@Stateless
@Remote(ProfileService.class)
@Interceptors({ InvocationDateContainer.class })
public class ProfileServiceBean implements ProfileService {

    @EJB(beanInterface = AccountServiceLocal.class)
    private AccountServiceLocal as;

    @EJB(beanInterface = IdentityService.class)
    private IdentityService is;

    public POProfile getProfile() {
        VOUserDetails currentUserDetails = is.getCurrentUserDetails();
        POOrganization org = null;
        if (is.isCallerOrganizationAdmin()) {
            org = new POOrganization(as.getOrganizationDataFallback(),
                    currentUserDetails.getOrganizationRoles());
        }
        return new POProfile(new POUser(currentUserDetails), org);
    }

    public void saveProfile(POProfile profile, String marketplaceId)
            throws NonUniqueBusinessKeyException, ObjectNotFoundException,
            ValidationException, OperationNotPermittedException,
            TechnicalServiceNotAliveException,
            TechnicalServiceOperationException, DistinguishedNameException,
            ConcurrentModificationException, ImageException {
        VOOrganization organization = getOrganization(profile.getOrganization());
        VOUserDetails user = getUserDetails(profile.getUser());
        VOImageResource imageResource = getImageResource(profile.getImage());
        as.updateAccountInformation(organization, user, marketplaceId,
                imageResource);
    }

    private VOImageResource getImageResource(POImageResource image) {
        VOImageResource voImageResource = null;
        if (image != null) {
            voImageResource = new VOImageResource();
            voImageResource.setBuffer(image.getBuffer());
            voImageResource.setContentType(image.getContentType());
            voImageResource.setImageType(image.getImageType());
        }
        return voImageResource;
    }

    private VOOrganization getOrganization(POOrganization organization) {
        VOOrganization result = null;
        // leave the other settings not covered in the PO unchanged,
        // that is take them from the current VO
        if (organization != null) {
            result = as.getOrganizationDataFallback();
            result.setKey(organization.getKey());
            result.setVersion(organization.getVersion());
            result.setOrganizationId(organization.getIdentifier());
            result.setEmail(organization.getMail());
            result.setSupportEmail(organization.getSupportEmail());
            result.setPhone(organization.getPhone());
            result.setUrl(organization.getWebsiteUrl());
            result.setAddress(organization.getAddress());
            result.setDomicileCountry(organization.getCountryISOCode());
            result.setDescription(organization.getDescription());
            result.setName(organization.getName());
        }

        return result;
    }

    private VOUserDetails getUserDetails(POUser user) {
        VOUserDetails result = is.getCurrentUserDetails();
        // leave the other settings not covered in the PO unchanged,
        // that is take them from the current VO

        result.setKey(user.getKey());
        result.setVersion(user.getVersion());
        result.setFirstName(user.getFirstName());
        result.setLastName(user.getLastName());
        result.setLocale(user.getLocale());
        result.setSalutation(user.getTitle());
        result.setEMail(user.getMail());

        return result;
    }
}
