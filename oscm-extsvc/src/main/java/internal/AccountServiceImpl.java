/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Aleh Khomich                                                     
 *                                                                              
 *  Creation Date: 22.06.2010                                                      
 *                                                                              
 *  Completion Time: 22.06.2010                                              
 *                                                                              
 *******************************************************************************/
package internal;

import java.util.List;
import java.util.Set;

import javax.jws.WebService;

import org.oscm.intf.AccountService;
import org.oscm.types.exceptions.ConcurrentModificationException;
import org.oscm.types.exceptions.MarketingPermissionNotFoundException;
import org.oscm.types.exceptions.NonUniqueBusinessKeyException;
import org.oscm.types.exceptions.ObjectNotFoundException;
import org.oscm.types.exceptions.OperationNotPermittedException;
import org.oscm.types.exceptions.OrganizationAuthoritiesException;
import org.oscm.types.exceptions.PaymentDeregistrationException;
import org.oscm.types.exceptions.ValidationException;
import org.oscm.vo.LdapProperties;
import org.oscm.vo.VOBillingContact;
import org.oscm.vo.VOImageResource;
import org.oscm.vo.VOOrganization;
import org.oscm.vo.VOOrganizationPaymentConfiguration;
import org.oscm.vo.VOPaymentInfo;
import org.oscm.vo.VOPaymentType;
import org.oscm.vo.VOServicePaymentConfiguration;
import org.oscm.vo.VOTechnicalService;
import org.oscm.vo.VOUda;
import org.oscm.vo.VOUdaDefinition;
import org.oscm.vo.VOUserDetails;

/**
 * This is a stub implementation of the {@link AccountService} as the Metro
 * jax-ws tools do not allow to generate WSDL files from the service interfaces.
 * 
 * <p>
 * <b>WARNING:</b> Do not use this class, all methods simply throw an
 * {@link UnsupportedOperationException}!
 * </p>
 * 
 * @author Aleh Khomich
 */
@WebService(serviceName = "AccountService", targetNamespace = "http://oscm.org/xsd", portName = "AccountServicePort", endpointInterface = "org.oscm.intf.AccountService")
public class AccountServiceImpl implements AccountService {

    @Override
    public void deregisterOrganization() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<VOPaymentType> getAvailablePaymentTypesForOrganization() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<VOPaymentType> getAvailablePaymentTypesFromOrganization(
            Long serviceKey) throws OrganizationAuthoritiesException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOBillingContact> getBillingContacts() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOOrganizationPaymentConfiguration> getCustomerPaymentConfiguration() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOOrganization> getMyCustomers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<VOPaymentType> getDefaultPaymentConfiguration() {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOOrganization getOrganizationData() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getOrganizationId(long subscriptionKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOOrganization registerKnownCustomer(VOOrganization organization,
            VOUserDetails user, LdapProperties organizationProperties,
            String marketplaceId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOOrganization registerCustomer(VOOrganization voOrganization,
            VOUserDetails admin, String password, Long serviceKey,
            String marketplaceId, String sellerId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOBillingContact saveBillingContact(VOBillingContact billingContact)
            throws NonUniqueBusinessKeyException,
            OperationNotPermittedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteBillingContact(VOBillingContact billingContact)
            throws ObjectNotFoundException, ConcurrentModificationException,
            OperationNotPermittedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean savePaymentConfiguration(
            Set<VOPaymentType> defaultConfiguration,
            List<VOOrganizationPaymentConfiguration> customerConfigurations,
            Set<VOPaymentType> defaultServiceConfiguration,
            List<VOServicePaymentConfiguration> serviceConfigurations)
            throws OperationNotPermittedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOPaymentInfo savePaymentInfo(VOPaymentInfo paymentInfo) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateAccountInformation(VOOrganization voOrganization,
            VOUserDetails voUser, String marketplaceId,
            VOImageResource imageResource) {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOOrganization updateCustomerDiscount(VOOrganization voOrganization) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> getUdaTargetTypes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOUdaDefinition> getUdaDefinitions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOUda> getUdas(String targetType, long targetObjectKey)
            throws ValidationException, OrganizationAuthoritiesException,
            ObjectNotFoundException, OperationNotPermittedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void saveUdaDefinitions(List<VOUdaDefinition> udaDefinitionsToSave,
            List<VOUdaDefinition> udaDefinitionsToDelete)
            throws ValidationException, OrganizationAuthoritiesException,
            NonUniqueBusinessKeyException, ObjectNotFoundException,
            ConcurrentModificationException, OperationNotPermittedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void saveUdas(List<VOUda> udas) throws ValidationException,
            ObjectNotFoundException, OperationNotPermittedException,
            ConcurrentModificationException, NonUniqueBusinessKeyException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getSupportedCountryCodes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOOrganization getSeller(String sellerId, String locale)
            throws ObjectNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOImageResource loadImageOfOrganization(long organizationKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deletePaymentInfo(VOPaymentInfo paymentInfo)
            throws ObjectNotFoundException, OperationNotPermittedException,
            ConcurrentModificationException, PaymentDeregistrationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOPaymentInfo> getPaymentInfos() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<VOPaymentType> getAvailablePaymentTypes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<VOPaymentType> getDefaultServicePaymentConfiguration() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOServicePaymentConfiguration> getServicePaymentConfiguration() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addSuppliersForTechnicalService(
            VOTechnicalService technicalService, List<String> organizationIds)
            throws ObjectNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeSuppliersFromTechnicalService(
            VOTechnicalService technicalService, List<String> organizationIds)
            throws OrganizationAuthoritiesException,
            MarketingPermissionNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOOrganization> getSuppliersForTechnicalService(
            VOTechnicalService technicalService) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOUdaDefinition> getUdaDefinitionsForCustomer(String supplierId)
            throws ObjectNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOUda> getUdasForCustomer(String targetType,
            long targetObjectKey, String supplierId)
            throws ValidationException, OrganizationAuthoritiesException,
            ObjectNotFoundException, OperationNotPermittedException {
        throw new UnsupportedOperationException();
    }

}
