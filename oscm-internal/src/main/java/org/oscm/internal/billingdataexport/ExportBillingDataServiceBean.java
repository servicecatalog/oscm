/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.internal.billingdataexport;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.oscm.converter.DateConverter;
import org.oscm.dataservice.local.DataService;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.validation.ArgumentValidator;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.intf.AccountService;
import org.oscm.internal.intf.BillingService;
import org.oscm.internal.intf.IdentityService;
import org.oscm.internal.types.enumtypes.BillingSharesResultType;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.types.exceptions.NoBilingSharesDataAvailableException;
import org.oscm.internal.vo.VOOrganization;

@Stateless
@Remote(ExportBillingDataService.class)
@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class })
public class ExportBillingDataServiceBean implements ExportBillingDataService {

    @EJB
    BillingService billingService;

    @EJB
    IdentityService idService;

    @EJB
    AccountService accountService;

    @EJB(beanInterface = DataService.class)
    DataService dm;

    @Override
    @RolesAllowed({ "MARKETPLACE_OWNER", "SERVICE_MANAGER", "RESELLER_MANAGER",
            "BROKER_MANAGER", "PLATFORM_OPERATOR" })
    public Response exportRevenueShares(PORevenueShareExport exportParam)
            throws OrganizationAuthoritiesException,
            OperationNotPermittedException,
            NoBilingSharesDataAvailableException, ValidationException {

        ArgumentValidator.notNull("exportParam", exportParam);
        ArgumentValidator.notNull("from", exportParam.getFrom());
        ArgumentValidator.notNull("to", exportParam.getTo());
        ArgumentValidator.notNull("revenueShareType",
                exportParam.getRevenueShareType());

        Long from = DateConverter
                .getBeginningOfDayInCurrentTimeZone(exportParam.getFrom());
        Long to = DateConverter
                .getBeginningOfNextDayInCurrentTimeZone(exportParam.getTo());

        byte[] xmlResult = null;
        xmlResult = billingService.getRevenueShareData(from, to,
                exportParam.getRevenueShareType());
        if (xmlResult == null || xmlResult.length < 1) {
            throw new NoBilingSharesDataAvailableException();
        }

        return new Response(xmlResult);
    }

    @Override
    @RolesAllowed({ "SERVICE_MANAGER", "RESELLER_MANAGER" })
    public Response exportBillingData(POBillingDataExport exportParam)
            throws NoBilingSharesDataAvailableException,
            OrganizationAuthoritiesException {
        ArgumentValidator.notNull("exportParam", exportParam);
        ArgumentValidator.notNull("from", exportParam.getFrom());
        ArgumentValidator.notNull("to", exportParam.getTo());

        Long from = DateConverter
                .getBeginningOfDayInCurrentTimeZone(exportParam.getFrom());
        Long to = DateConverter
                .getBeginningOfNextDayInCurrentTimeZone(exportParam.getTo());

        byte[] xmlResult = null;
        xmlResult = billingService.getCustomerBillingData(from, to,
                exportParam.getOrganizationIds());
        if (xmlResult == null || xmlResult.length < 1) {
            throw new NoBilingSharesDataAvailableException();
        }

        return new Response(xmlResult);
    }

    @Override
    public List<BillingSharesResultType> getBillingShareResultTypes() {

        List<BillingSharesResultType> resultTypes = new ArrayList<BillingSharesResultType>();
        resultTypes.addAll(sharesResultTypesForRole());

        return resultTypes;
    }

    @Override
    public List<POOrganization> getCustomers() {
        List<VOOrganization> customerOrgs = null;
        try {
            customerOrgs = accountService.getMyCustomersOptimization();
        } catch (OrganizationAuthoritiesException e) {
            return new ArrayList<POOrganization>();
        }
        List<POOrganization> customers = new ArrayList<POOrganization>();
        for (VOOrganization vo : customerOrgs) {
            POOrganization org = new POOrganization();
            org.setKey(vo.getKey());
            org.setOrganizationAddress(vo.getAddress());
            org.setOrganizationId(vo.getOrganizationId());
            org.setOrganizationName(vo.getName());
            customers.add(org);
        }
        return customers;
    }

    Set<BillingSharesResultType> sharesResultTypesForRole() {

        Set<OrganizationRoleType> orgRoles = idService.getCurrentUserDetails()
                .getOrganizationRoles();
        Set<UserRoleType> userRoles = idService.getCurrentUserDetails()
                .getUserRoles();
        Set<BillingSharesResultType> billingShareTypes = new HashSet<BillingSharesResultType>();

        if (orgRoles.contains(OrganizationRoleType.PLATFORM_OPERATOR)
                && userRoles.contains(UserRoleType.PLATFORM_OPERATOR)) {
            billingShareTypes.add(BillingSharesResultType.BROKER);
            billingShareTypes.add(BillingSharesResultType.SUPPLIER);
            billingShareTypes.add(BillingSharesResultType.RESELLER);
            billingShareTypes.add(BillingSharesResultType.MARKETPLACE_OWNER);
            return billingShareTypes;
        }
        if (orgRoles.contains(OrganizationRoleType.BROKER)
                && userRoles.contains(UserRoleType.BROKER_MANAGER)) {
            billingShareTypes.add(BillingSharesResultType.BROKER);
        }
        if (orgRoles.contains(OrganizationRoleType.SUPPLIER)
                && userRoles.contains(UserRoleType.SERVICE_MANAGER)) {
            billingShareTypes.add(BillingSharesResultType.SUPPLIER);
        }
        if (orgRoles.contains(OrganizationRoleType.RESELLER)
                && userRoles.contains(UserRoleType.RESELLER_MANAGER)) {
            billingShareTypes.add(BillingSharesResultType.RESELLER);
        }
        if (orgRoles.contains(OrganizationRoleType.MARKETPLACE_OWNER)
                && userRoles.contains(UserRoleType.MARKETPLACE_OWNER)) {
            billingShareTypes.add(BillingSharesResultType.MARKETPLACE_OWNER);
        }

        return billingShareTypes;
    }

    @Override
    public boolean isPlatformOperator() {
        Set<OrganizationRoleType> orgRoles = idService.getCurrentUserDetails()
                .getOrganizationRoles();
        if (orgRoles.contains(OrganizationRoleType.PLATFORM_OPERATOR)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isSupplierOrReseller() {
        Set<OrganizationRoleType> orgRoles = idService.getCurrentUserDetails()
                .getOrganizationRoles();
        if (orgRoles.contains(OrganizationRoleType.SUPPLIER)
                || orgRoles.contains(OrganizationRoleType.RESELLER)) {
            return true;
        }
        return false;
    }
}
