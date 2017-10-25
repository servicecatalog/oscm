/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2012-12-10                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.accountmgmt;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.oscm.accountservice.assembler.OrganizationAssembler;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.vo.BaseAssembler;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.vo.VOOrganization;

/**
 * @author yuyin
 * 
 */
@Stateless
@Remote(AccountServiceManagement.class)
@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class })
public class AccountServiceManagementBean implements AccountServiceManagement {

    @EJB(beanInterface = DataService.class)
    protected DataService dm;

    @RolesAllowed({ "SERVICE_MANAGER", "RESELLER_MANAGER" })
    public int getCutOffDayOfOrganization() {
        Organization supplier = dm.getCurrentUser().getOrganization();
        return supplier.getCutOffDay();
    }

    @RolesAllowed({ "SERVICE_MANAGER", "RESELLER_MANAGER" })
    public void setCutOffDayOfOrganization(int cutOffDay,
            VOOrganization organization) throws ConcurrentModificationException {
        Organization supplier = dm.getCurrentUser().getOrganization();
        // check for concurrent change
        BaseAssembler.verifyVersionAndKey(supplier, organization);
        supplier.setCutOffDay(cutOffDay);
        dm.flush();
    }

    @RolesAllowed({ "SERVICE_MANAGER", "RESELLER_MANAGER" })
    public VOOrganization getOrganizationData() {

        final Organization organization = dm.getCurrentUser().getOrganization();
        VOOrganization result = OrganizationAssembler.toVOOrganization(
                organization, false, null);
        return result;
    }
}
