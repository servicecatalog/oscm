/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 08.08.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.accountservice.bean;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Collections;

import javax.ejb.SessionContext;

import org.junit.Before;
import org.junit.Test;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationRole;
import org.oscm.domobjects.OrganizationToRole;
import org.oscm.domobjects.PlatformUser;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOOrganization;

/**
 * Tests the validation of methods in AccountServiceBean
 * 
 * @author barzu
 */
public class AccountServiceBeanValidationTest {

    private AccountServiceBean asb;
    private PlatformUser user;

    @Before
    public void setup() throws Exception {
        asb = new AccountServiceBean();
        asb.dm = mock(DataService.class);
        asb.sessionCtx = mock(SessionContext.class);

        Organization org = new Organization();
        user = new PlatformUser();
        user.setOrganization(org);

        doReturn(user).when(asb.dm).getCurrentUser();
        doReturn(org).when(asb.dm).getReferenceByBusinessKey(
                any(Organization.class));
    }

    private void addRole(OrganizationRoleType role) {
        OrganizationRole orgRole = new OrganizationRole();
        orgRole.setRoleName(role);
        OrganizationToRole orgToRole = new OrganizationToRole();
        orgToRole.setOrganization(user.getOrganization());
        orgToRole.setOrganizationRole(orgRole);
        user.getOrganization()
                .setGrantedRoles(Collections.singleton(orgToRole));
    }

    private VOOrganization createVOOrganization() {
        VOOrganization org = new VOOrganization();
        org.setLocale("en");
        return org;
    }

    @Test(expected = ValidationException.class)
    public void updateAccountInformation_MandatoryFieldNotSet_Reseller()
            throws Exception {
        addRole(OrganizationRoleType.RESELLER);

        asb.updateAccountInformation(createVOOrganization(), null, null, null);
    }

    @Test(expected = ValidationException.class)
    public void updateAccountInformation_MandatoryFieldNotSet_Broker()
            throws Exception {
        addRole(OrganizationRoleType.BROKER);

        asb.updateAccountInformation(createVOOrganization(), null, null, null);
    }

    @Test(expected = ValidationException.class)
    public void updateAccountInformation_MandatoryFieldNotSet_Supplier()
            throws Exception {
        addRole(OrganizationRoleType.SUPPLIER);

        asb.updateAccountInformation(createVOOrganization(), null, null, null);
    }

    @Test(expected = ValidationException.class)
    public void updateAccountInformation_MandatoryFieldNotSet_TechnicalProvider()
            throws Exception {
        addRole(OrganizationRoleType.TECHNOLOGY_PROVIDER);

        asb.updateAccountInformation(createVOOrganization(), null, null, null);
    }

    @Test(expected = ValidationException.class)
    public void updateCustomerDiscount_MandatoryFieldNotSet_Reseller()
            throws Exception {
        addRole(OrganizationRoleType.RESELLER);

        asb.updateCustomerDiscount(createVOOrganization());
    }

    @Test(expected = ValidationException.class)
    public void updateCustomerDiscount_MandatoryFieldNotSet_Broker()
            throws Exception {
        addRole(OrganizationRoleType.BROKER);

        asb.updateCustomerDiscount(createVOOrganization());
    }

    @Test(expected = ValidationException.class)
    public void updateCustomerDiscount_MandatoryFieldNotSet_Supplier()
            throws Exception {
        addRole(OrganizationRoleType.SUPPLIER);

        asb.updateCustomerDiscount(createVOOrganization());
    }

    @Test(expected = ValidationException.class)
    public void updateCustomerDiscount_MandatoryFieldNotSet_TechnologyProvider()
            throws Exception {
        addRole(OrganizationRoleType.TECHNOLOGY_PROVIDER);

        asb.updateCustomerDiscount(createVOOrganization());
    }

}
