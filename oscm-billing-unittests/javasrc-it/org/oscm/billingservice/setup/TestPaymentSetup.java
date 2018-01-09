/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 15.04.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.setup;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.oscm.test.ejb.TestContainer;
import org.oscm.internal.intf.AccountService;
import org.oscm.internal.intf.OperatorService;
import org.oscm.internal.types.enumtypes.PaymentInfoType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OperationPendingException;
import org.oscm.internal.types.exception.OrganizationAuthorityException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOOrganizationPaymentConfiguration;
import org.oscm.internal.vo.VOPSP;
import org.oscm.internal.vo.VOPSPAccount;
import org.oscm.internal.vo.VOPaymentType;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOServicePaymentConfiguration;

/**
 * Setup of Payment data
 * 
 * @author baumann
 */
public class TestPaymentSetup {

    private final TestContainer container;
    private final OperatorService operatorService;
    private final AccountService accountService;

    public TestPaymentSetup(TestContainer container) {
        this.container = container;
        operatorService = container.get(OperatorService.class);
        accountService = container.get(AccountService.class);
    }

    /**
     * Get the default payment types for the current organization
     * 
     * @return a set with the default payment types
     */
    public Set<VOPaymentType> getDefaultPaymentConfig() {
        return accountService.getDefaultPaymentConfiguration();
    }

    /**
     * Get the default service payment types for the current organization
     * 
     * @return a set with the default service payment types
     */
    public Set<VOPaymentType> getDefaultServicePaymentConfig() {
        return accountService.getDefaultServicePaymentConfiguration();
    }

    /**
     * Get an available payment type for the current organization
     * 
     * @param paymentInfoType
     *            a payment type
     * @return the VOPaymentType if it was found, otherwise <code>null</code>
     */
    public VOPaymentType getAvailablePaymentTypeForOrganization(
            PaymentInfoType paymentInfoType) {
        return getPaymentTypeVO(
                accountService.getAvailablePaymentTypesForOrganization(),
                PaymentInfoType.INVOICE);
    }

    /**
     * Find a payment type in a set
     * 
     * @param paymentTypeSet
     *            a set of payment types
     * @param paymentInfoType
     *            a payment type
     * @return the VOPaymentType if it was found, otherwise <code>null</code>
     */
    private VOPaymentType getPaymentTypeVO(Set<VOPaymentType> paymentTypeSet,
            PaymentInfoType paymentInfoType) {
        for (VOPaymentType voPaymentType : paymentTypeSet) {
            if (voPaymentType.getPaymentTypeId().equals(paymentInfoType.name())) {
                return voPaymentType;
            }
        }
        return null;
    }

    private Set<VOPaymentType> newVOPaymentTypeSet(String... ptIds) {
        Set<VOPaymentType> ptSet = new HashSet<VOPaymentType>();
        for (String ptID : ptIds) {
            VOPaymentType pt = new VOPaymentType();
            pt.setPaymentTypeId(ptID);
            ptSet.add(pt);
        }
        return ptSet;
    }

    public VOOrganizationPaymentConfiguration newVOOrganizationPaymentConfiguration(
            VOOrganization organization, Set<VOPaymentType> enabledPaymentTypes) {
        VOOrganizationPaymentConfiguration orgPaymentConfig = new VOOrganizationPaymentConfiguration();
        orgPaymentConfig.setOrganization(organization);
        if (enabledPaymentTypes != null) {
            orgPaymentConfig.setEnabledPaymentTypes(enabledPaymentTypes);
        }
        return orgPaymentConfig;
    }

    public boolean savePaymentConfiguration(
            Set<VOPaymentType> defaultConfiguration,
            List<VOOrganizationPaymentConfiguration> customerConfigurations,
            Set<VOPaymentType> defaultServiceConfiguration,
            List<VOServicePaymentConfiguration> serviceConfigurations)
            throws Exception {
        return accountService.savePaymentConfiguration(defaultConfiguration,
                customerConfigurations, defaultServiceConfiguration,
                serviceConfigurations);
    }

    public void addAvailablePaymentTypes(VOOrganization seller,
            String... paymentTypes) throws Exception {
        operatorService.addAvailablePaymentTypes(seller, new HashSet<String>(
                Arrays.asList(paymentTypes)));
    }

    public void savePaymentConfigForSeller(VOOrganization seller)
            throws Exception {
        VOPaymentType invoicePT = getAvailablePaymentTypeForOrganization(PaymentInfoType.INVOICE);

        Set<VOPaymentType> enabledPaymentTypes = getDefaultPaymentConfig();
        enabledPaymentTypes.add(invoicePT);

        savePaymentConfiguration(enabledPaymentTypes,
                Collections
                        .singletonList(newVOOrganizationPaymentConfiguration(
                                seller, enabledPaymentTypes)),
                enabledPaymentTypes, null);
    }

    /**
     * Create payment info for supplier organization
     */
    public void createPaymentForSupplier(long platformAdminKey,
            long supplierAdminKey, VOOrganization supplier) throws Exception {
        createPaymentForSeller(platformAdminKey, supplierAdminKey, supplier,
                UserRoleType.SERVICE_MANAGER);
    }

    /**
     * Create payment info for supplier organization
     */
    public void createPaymentForReseller(long platformAdminKey,
            long resellerAdminKey, VOOrganization reseller) throws Exception {
        createPaymentForSeller(platformAdminKey, resellerAdminKey, reseller,
                UserRoleType.RESELLER_MANAGER);
    }

    /**
     * Create payment info for supplier or reseller organization
     */
    public void createPaymentForSeller(long platformAdminKey,
            long sellerAdminKey, VOOrganization seller, UserRoleType userRole)
            throws Exception {
        container
                .login(platformAdminKey, UserRoleType.PLATFORM_OPERATOR.name());
        savePSPAccount(seller, "psp1", getPSP(0));
        addAvailablePaymentTypes(seller, PaymentInfoType.INVOICE.name(),
                PaymentInfoType.CREDIT_CARD.name(),
                PaymentInfoType.DIRECT_DEBIT.name());
        container.login(sellerAdminKey, userRole.name());
        savePaymentConfigForSeller(seller);
    }

    public VOPSP getPSP(int index) {
        return operatorService.getPSPs().get(index);
    }

    public void savePSPAccount(VOOrganization organization,
            String pspIdentifier, VOPSP psp) throws ObjectNotFoundException,
            OrganizationAuthorityException, ConcurrentModificationException,
            ValidationException {
        operatorService.savePSPAccount(organization,
                newVOPSPAccount(pspIdentifier, psp));
    }

    private VOPSPAccount newVOPSPAccount(String pspIdentifier, VOPSP psp) {
        VOPSPAccount pspAccount = new VOPSPAccount();
        pspAccount.setPspIdentifier(pspIdentifier);
        pspAccount.setPsp(psp);
        return pspAccount;
    }

    /**
     * Delete all payment types of the given customer organization. All
     * subscriptions of the customer are suspended.
     */
    public void deleteCustomerPaymentTypes(VOOrganization customerOrg)
            throws ObjectNotFoundException, OperationNotPermittedException,
            OperationPendingException {
        List<VOOrganizationPaymentConfiguration> emptyPaymentConfig = Arrays
                .asList(new VOOrganizationPaymentConfiguration[] { newVOOrganizationPaymentConfiguration(
                        customerOrg, null) });

        accountService.savePaymentConfiguration(getDefaultPaymentConfig(),
                emptyPaymentConfig, getDefaultServicePaymentConfig(), null);
    }

    public void deleteCustomerPaymentTypes(long supplierAdminKey,
            VOOrganization customerOrg) throws ObjectNotFoundException,
            OperationNotPermittedException, OperationPendingException {
        container.login(supplierAdminKey, UserRoleType.SERVICE_MANAGER.name());
        deleteCustomerPaymentTypes(customerOrg);
    }

    public void deleteCustomerPaymentTypes(VendorData vendorData,
            VOOrganization customerOrg) throws ObjectNotFoundException,
            OperationNotPermittedException, OperationPendingException {
        container.login(vendorData.getAdminKey(),
                vendorData.getAdminUserRoles());
        deleteCustomerPaymentTypes(customerOrg);
    }

    public void deleteServicePaymentTypes(VendorData vendorData,
            VOService service) throws Exception {
        container.login(vendorData.getAdminKey(),
                vendorData.getAdminUserRoles());
        deleteServicePaymentTypes(service);
    }

    public void deleteServicePaymentTypes(long supplierAdminKey,
            VOService service) throws Exception {
        container.login(supplierAdminKey, UserRoleType.SERVICE_MANAGER.name());
        deleteServicePaymentTypes(service);
    }

    public void deleteServicePaymentTypes(VOService service) throws Exception {
        List<VOServicePaymentConfiguration> emptySPS = Arrays
                .asList(new VOServicePaymentConfiguration[] { newVOServicePaymentConfiguration(
                        service, null) });

        accountService.savePaymentConfiguration(getDefaultPaymentConfig(),
                null, getDefaultServicePaymentConfig(), emptySPS);
    }

    private VOServicePaymentConfiguration newVOServicePaymentConfiguration(
            VOService service, Set<VOPaymentType> enabledPaymentTypes) {
        VOServicePaymentConfiguration sps = new VOServicePaymentConfiguration();
        sps.setService(service);
        if (enabledPaymentTypes != null) {
            sps.setEnabledPaymentTypes(enabledPaymentTypes);
        }
        return sps;
    }

    /**
     * Assign the payment type "INVOICE" to the given customer organization. If
     * the customer had no payment types, but has suspended subscriptions, these
     * subscriptions will be resumed.
     * 
     * @param customerOrg
     * @param accountService
     * @throws ObjectNotFoundException
     * @throws OperationNotPermittedException
     * @throws OperationPendingException
     */
    public void reassignCustomerPaymentTypes(VOOrganization customerOrg)
            throws ObjectNotFoundException, OperationNotPermittedException,
            OperationPendingException {

        VOOrganizationPaymentConfiguration paymentConfig = newVOOrganizationPaymentConfiguration(
                customerOrg, newVOPaymentTypeSet("INVOICE"));
        List<VOOrganizationPaymentConfiguration> paymentConfigList = Arrays
                .asList(new VOOrganizationPaymentConfiguration[] { paymentConfig });

        accountService.savePaymentConfiguration(getDefaultPaymentConfig(),
                paymentConfigList, getDefaultServicePaymentConfig(), null);
    }

    public void reassignCustomerPaymentTypes(long supplierAdminKey,
            VOOrganization customerOrg) throws ObjectNotFoundException,
            OperationNotPermittedException, OperationPendingException {
        container.login(supplierAdminKey, UserRoleType.SERVICE_MANAGER.name());
        reassignCustomerPaymentTypes(customerOrg);
    }

    public void reassignCustomerPaymentTypes(VendorData vendorData,
            VOOrganization customerOrg) throws ObjectNotFoundException,
            OperationNotPermittedException, OperationPendingException {
        container.login(vendorData.getAdminKey(),
                vendorData.getAdminUserRoles());
        reassignCustomerPaymentTypes(customerOrg);
    }

    public void reassignServicePaymentTypes(long supplierAdminKey,
            VOService service) throws Exception {
        container.login(supplierAdminKey, UserRoleType.SERVICE_MANAGER.name());
        reassignServicePaymentTypes(service);
    }

    public void reassignServicePaymentTypes(VendorData vendorData,
            VOService service) throws Exception {
        container.login(vendorData.getAdminKey(),
                vendorData.getAdminUserRoles());
        reassignServicePaymentTypes(service);
    }

    public void reassignServicePaymentTypes(VOService service) throws Exception {
        VOServicePaymentConfiguration paymentConfig = newVOServicePaymentConfiguration(
                service, newVOPaymentTypeSet("INVOICE"));
        List<VOServicePaymentConfiguration> paymentConfigList = Arrays
                .asList(new VOServicePaymentConfiguration[] { paymentConfig });

        accountService.savePaymentConfiguration(getDefaultPaymentConfig(),
                null, getDefaultServicePaymentConfig(), paymentConfigList);
    }
}
