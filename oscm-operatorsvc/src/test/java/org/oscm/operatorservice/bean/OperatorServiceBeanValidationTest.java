/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 08.08.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.operatorservice.bean;

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
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOOperatorOrganization;

/**
 * @author barzu
 * 
 */
public class OperatorServiceBeanValidationTest {

    private OperatorServiceBean asb;

    @Before
    public void setup() throws Exception {
        asb = new OperatorServiceBean();
        asb.dm = mock(DataService.class);
        asb.sessionCtx = mock(SessionContext.class);

        doReturn(new Organization()).when(asb.dm).getReferenceByBusinessKey(
                any(Organization.class));
    }

    private VOOperatorOrganization createVOOrganization(
            OrganizationRoleType role) {
        VOOperatorOrganization org = new VOOperatorOrganization();
        org.setLocale("en");

        OrganizationRole orgRole = new OrganizationRole();
        orgRole.setRoleName(role);
        OrganizationToRole orgToRole = new OrganizationToRole();
        orgToRole.setOrganizationRole(orgRole);
        org.setOrganizationRoles(Collections.singletonList(role));
        return org;
    }

    @Test(expected = ValidationException.class)
    public void updateOrganizationIntern_MandatoryFieldNotSet_Reseller()
            throws Exception {
        asb.updateOrganizationIntern(
                createVOOrganization(OrganizationRoleType.RESELLER), null);
    }

    @Test(expected = ValidationException.class)
    public void updateOrganizationIntern_MandatoryFieldNotSet_Broker()
            throws Exception {
        asb.updateOrganizationIntern(
                createVOOrganization(OrganizationRoleType.BROKER), null);
    }

    @Test(expected = ValidationException.class)
    public void updateOrganizationIntern_MandatoryFieldNotSet_Supplier()
            throws Exception {
        asb.updateOrganizationIntern(
                createVOOrganization(OrganizationRoleType.SUPPLIER), null);
    }

    @Test(expected = ValidationException.class)
    public void updateOrganizationIntern_MandatoryFieldNotSet_TechnologyProvider()
            throws Exception {
        asb.updateOrganizationIntern(
                createVOOrganization(OrganizationRoleType.TECHNOLOGY_PROVIDER),
                null);
    }

}
