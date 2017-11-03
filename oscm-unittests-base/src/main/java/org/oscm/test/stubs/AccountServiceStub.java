/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.test.stubs;

import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.oscm.accountservice.local.AccountServiceLocal;
import org.oscm.domobjects.Discount;
import org.oscm.domobjects.ImageResource;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationReference;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.TriggerProcess;
import org.oscm.internal.intf.AccountService;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.PerformanceHint;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.DistinguishedNameException;
import org.oscm.internal.types.exception.MailOperationException;
import org.oscm.internal.types.exception.MarketingPermissionNotFoundException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.types.exception.OrganizationAuthorityException;
import org.oscm.internal.types.exception.PaymentDataException;
import org.oscm.internal.types.exception.PaymentDeregistrationException;
import org.oscm.internal.types.exception.TechnicalServiceNotAliveException;
import org.oscm.internal.types.exception.TechnicalServiceOperationException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.LdapProperties;
import org.oscm.internal.vo.VOBillingContact;
import org.oscm.internal.vo.VOImageResource;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOOrganizationPaymentConfiguration;
import org.oscm.internal.vo.VOPaymentInfo;
import org.oscm.internal.vo.VOPaymentType;
import org.oscm.internal.vo.VOServicePaymentConfiguration;
import org.oscm.internal.vo.VOTechnicalService;
import org.oscm.internal.vo.VOUda;
import org.oscm.internal.vo.VOUdaDefinition;
import org.oscm.internal.vo.VOUserDetails;

public class AccountServiceStub implements AccountService, AccountServiceLocal {

    @Override
    public void deregisterOrganization() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<VOPaymentType> getAvailablePaymentTypesForOrganization() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOOrganization> getMyCustomers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOOrganization> getMyCustomersOptimization() {
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
    public String getLocalizedAttributeName(long key, String locale) {
        return null;
    }

    @Override
    public VOOrganization registerKnownCustomer(VOOrganization organization,
            VOUserDetails user, LdapProperties organizationProperties,
            String marketplaceId) throws ValidationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOOrganization registerCustomer(VOOrganization voOrganization,
            VOUserDetails admin, String password, Long serviceKey,
            String marketplaceId, String sellerId) throws ValidationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateAccountInformation(VOOrganization voOrganization,
            VOUserDetails voUser, String marketplaceId,
            VOImageResource imageResource) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Organization addOrganizationToRole(String organizationId,
            OrganizationRoleType role) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Organization registerOrganization(Organization organization,
            ImageResource imageResource, VOUserDetails user,
            Properties organizationProperties, String domicileCountry,
            String marketplaceId, String description,
            OrganizationRoleType... roles)
            throws OrganizationAuthorityException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeOverdueOrganization(Organization organization) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeOverdueOrganizations(long currentTime) {
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
            throws ObjectNotFoundException, OperationNotPermittedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<VOPaymentType> getDefaultPaymentConfiguration() {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOPaymentInfo savePaymentInfo(VOPaymentInfo paymentInfo)
            throws ObjectNotFoundException, PaymentDeregistrationException,
            NonUniqueBusinessKeyException, OperationNotPermittedException,
            PaymentDataException {
        return null;
    }

    @Override
    public VOOrganization updateCustomerDiscount(VOOrganization voOrganization)
            throws ObjectNotFoundException, ValidationException,
            OperationNotPermittedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<OrganizationReference> getOrganizationForDiscountEndNotificiation(
            long currentTimeMillis) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean sendDiscountEndNotificationMail(long currentTimeMillis)
            throws MailOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void checkDistinguishedName(Organization organization)
            throws DistinguishedNameException {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOOrganization registerKnownCustomerInt(TriggerProcess tp)
            throws OrganizationAuthoritiesException, ValidationException,
            NonUniqueBusinessKeyException, MailOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void savePaymentConfigurationInt(TriggerProcess tp)
            throws ObjectNotFoundException {
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
    public List<VOUda> getUdas(String targetType, long targetObjectKey,
            boolean checkSeller) throws ValidationException,
            OrganizationAuthoritiesException, ObjectNotFoundException,
            OperationNotPermittedException {
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
    public VOImageResource loadImageOfOrganization(long organizationKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOOrganization getSeller(String sellerId, String locale)
            throws ObjectNotFoundException {
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
    public void processImage(ImageResource imageResource, long organizationKey)
            throws ValidationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateAccountInformation(Organization organization,
            VOUserDetails user, String marketplaceId)
            throws ValidationException, NonUniqueBusinessKeyException,
            OperationNotPermittedException, TechnicalServiceNotAliveException,
            TechnicalServiceOperationException, DistinguishedNameException,
            ConcurrentModificationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Discount updateCustomerDiscount(Organization organization,
            Discount discount, Integer discountVersion)
            throws ObjectNotFoundException, ValidationException,
            OperationNotPermittedException, ConcurrentModificationException {
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

    @Override
    public List<PlatformUser> getOrganizationAdmins(long organizationKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOOrganization getOrganizationDataFallback() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isPaymentTypeEnabled(long serviceKey, long paymentTypeKey)
            throws ObjectNotFoundException {
        return false;
    }

    @Override
    public Set<VOPaymentType> getAvailablePaymentTypesFromOrganization(
            Long serviceKey) throws OrganizationAuthoritiesException,
            ObjectNotFoundException {
        return null;
    }

    @Override
    public long countRegisteredUsers() {
        return 0;
    }

    @Override
    public boolean checkUserNum() throws MailOperationException {
        return false;
    }

    @Override
    public List<VOPaymentInfo> getPaymentInfosForOrgAdmin() {
        return null;
    }

    @Override
    public VOOrganization getMyCustomer(VOOrganization org, String locale)
            throws ObjectNotFoundException {
        return null;
    }

    @Override
    public List<VOServicePaymentConfiguration> getServicePaymentConfiguration(
            PerformanceHint performanceHint) {
        return null;
    }

}
