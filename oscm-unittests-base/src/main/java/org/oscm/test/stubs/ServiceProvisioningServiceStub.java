/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.test.stubs;

import java.util.List;

import org.oscm.domobjects.Organization;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.TriggerProcess;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.internal.intf.ServiceProvisioningService;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.CurrencyException;
import org.oscm.internal.types.exception.DeletionConstraintException;
import org.oscm.internal.types.exception.ImportException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OperationPendingException;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.types.exception.ServiceCompatibilityException;
import org.oscm.internal.types.exception.ServiceNotPublishedException;
import org.oscm.internal.types.exception.ServiceOperationException;
import org.oscm.internal.types.exception.ServiceStateException;
import org.oscm.internal.types.exception.SubscriptionStateException;
import org.oscm.internal.types.exception.TechnicalServiceActiveException;
import org.oscm.internal.types.exception.TechnicalServiceNotAliveException;
import org.oscm.internal.types.exception.UpdateConstraintException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOCompatibleService;
import org.oscm.internal.vo.VOCustomerService;
import org.oscm.internal.vo.VOImageResource;
import org.oscm.internal.vo.VOLocalizedText;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOPriceModel;
import org.oscm.internal.vo.VOPriceModelLocalization;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOServiceActivation;
import org.oscm.internal.vo.VOServiceDetails;
import org.oscm.internal.vo.VOServiceEntry;
import org.oscm.internal.vo.VOServiceLocalization;
import org.oscm.internal.vo.VOSubscriptionDetails;
import org.oscm.internal.vo.VOTechnicalService;
import org.oscm.serviceprovisioningservice.local.ServiceProvisioningServiceLocal;

public class ServiceProvisioningServiceStub implements
        ServiceProvisioningService, ServiceProvisioningServiceLocal {

    @Override
    public VOService activateService(VOService product)
            throws ObjectNotFoundException, ServiceStateException,
            OrganizationAuthoritiesException, OperationNotPermittedException,
            ServiceOperationException, TechnicalServiceNotAliveException,
            ServiceNotPublishedException, OperationPendingException {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOServiceDetails createService(VOTechnicalService technicalProduct,
            VOService productToCreate, VOImageResource voImageResource)
            throws OrganizationAuthoritiesException, ObjectNotFoundException,
            OperationNotPermittedException, ValidationException,
            NonUniqueBusinessKeyException {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOService deactivateService(VOService product)
            throws ObjectNotFoundException, ServiceStateException,
            OrganizationAuthoritiesException, OperationNotPermittedException,
            OperationPendingException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteService(VOService product)
            throws OrganizationAuthoritiesException, ObjectNotFoundException,
            ServiceOperationException, ServiceStateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteTechnicalService(VOTechnicalService technicalProduct)
            throws OrganizationAuthoritiesException, ObjectNotFoundException,
            DeletionConstraintException {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] exportTechnicalServices(List<VOTechnicalService> products)
            throws OrganizationAuthoritiesException, ObjectNotFoundException,
            OperationNotPermittedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOService> getCompatibleServices(VOService referenceProduct)
            throws OrganizationAuthoritiesException, ObjectNotFoundException,
            OperationNotPermittedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOService> getServicesForCustomer(VOOrganization customer)
            throws ObjectNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOServiceDetails getServiceDetails(VOService product)
            throws ObjectNotFoundException, OperationNotPermittedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOServiceDetails getServiceForCustomer(VOOrganization customer,
            VOService product) throws OperationNotPermittedException,
            ObjectNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOServiceDetails getServiceForSubscription(VOOrganization customer,
            String subscriptionId) throws OrganizationAuthoritiesException,
            ObjectNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getSupportedCurrencies() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOTechnicalService> getTechnicalServices(
            OrganizationRoleType organizationRoleType)
            throws OrganizationAuthoritiesException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String importTechnicalServices(byte[] xml) throws ImportException,
            OperationNotPermittedException, TechnicalServiceActiveException,
            UpdateConstraintException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void validateTechnicalServiceCommunication(
            VOTechnicalService technicalProduct)
            throws ObjectNotFoundException, OperationNotPermittedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOImageResource loadImage(Long productKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOServiceDetails savePriceModel(VOServiceDetails productDetails,
            VOPriceModel priceModel) throws ObjectNotFoundException,
            OperationNotPermittedException, CurrencyException,
            ValidationException, ServiceStateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOServiceDetails savePriceModelForCustomer(
            VOServiceDetails productDetails, VOPriceModel priceModel,
            VOOrganization customer) throws OrganizationAuthoritiesException,
            ObjectNotFoundException, OperationNotPermittedException,
            CurrencyException, ValidationException, ServiceStateException,
            ServiceOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOServiceDetails savePriceModelForSubscription(
            VOServiceDetails productDetails, VOPriceModel priceModel)
            throws OrganizationAuthoritiesException, ObjectNotFoundException,
            OperationNotPermittedException, CurrencyException,
            ValidationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOTechnicalService createTechnicalService(
            VOTechnicalService technicalProduct)
            throws OrganizationAuthoritiesException, ValidationException,
            NonUniqueBusinessKeyException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void saveTechnicalServiceLocalization(
            VOTechnicalService technicalProduct)
            throws OrganizationAuthoritiesException, ObjectNotFoundException,
            OperationNotPermittedException, UpdateConstraintException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCompatibleServices(VOService sourceProduct,
            List<VOService> compatibleProducts)
            throws OrganizationAuthoritiesException, ObjectNotFoundException,
            OperationNotPermittedException, ServiceCompatibilityException,
            ServiceStateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOServiceDetails updateService(VOServiceDetails productDetails,
            VOImageResource voImageResource) throws ObjectNotFoundException,
            OrganizationAuthoritiesException, OperationNotPermittedException,
            ValidationException, NonUniqueBusinessKeyException,
            ServiceStateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOCustomerService> getAllCustomerSpecificServices()
            throws OrganizationAuthoritiesException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void activateServiceInt(TriggerProcess tp)
            throws ObjectNotFoundException, ServiceOperationException,
            TechnicalServiceNotAliveException, ServiceStateException,
            OrganizationAuthoritiesException, OperationNotPermittedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deactivateServiceInt(TriggerProcess tp)
            throws ObjectNotFoundException, ServiceStateException,
            OrganizationAuthoritiesException, OperationNotPermittedException,
            ServiceOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOPriceModelLocalization getPriceModelLocalization(
            VOPriceModel pricemodel) throws ObjectNotFoundException,
            OperationNotPermittedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOServiceLocalization getServiceLocalization(VOService product)
            throws ObjectNotFoundException, OperationNotPermittedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void savePriceModelLocalization(VOPriceModel pricemodel,
            VOPriceModelLocalization localization)
            throws ObjectNotFoundException, OperationNotPermittedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void saveServiceLocalization(VOService product,
            VOServiceLocalization localization) throws ObjectNotFoundException,
            OperationNotPermittedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOServiceDetails copyService(VOService service, String serviceId)
            throws ObjectNotFoundException, OrganizationAuthoritiesException,
            OperationNotPermittedException, ServiceStateException,
            ConcurrentModificationException, NonUniqueBusinessKeyException,
            ValidationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOService> getSuppliedServices() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOService> getServicesForMarketplace(String marketplaceId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOImageResource loadImageForSupplier(String serviceId,
            String supplierId) throws ObjectNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOLocalizedText> getPriceModelLicenseTemplateLocalization(
            VOServiceDetails service) throws ObjectNotFoundException,
            OperationNotPermittedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOService> setActivationStates(
            List<VOServiceActivation> activations)
            throws ObjectNotFoundException, ServiceStateException,
            OrganizationAuthoritiesException, OperationNotPermittedException,
            ServiceOperationException, TechnicalServiceNotAliveException,
            OperationPendingException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOService> getRelatedServicesForMarketplace(VOService service,
            String marketplaceId, String locale) throws ObjectNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOServiceEntry getServiceForMarketplace(Long serviceKey,
            String marketplaceId, String locale)
            throws ObjectNotFoundException, OperationNotPermittedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOOrganization getServiceSeller(long serviceKey, String locale) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getInstanceIdsForSellers(List<String> organizationIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOService suspendService(VOService service, String reason)
            throws ObjectNotFoundException, OperationNotPermittedException,
            ServiceStateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOService resumeService(VOService service)
            throws ObjectNotFoundException, OperationNotPermittedException,
            ServiceStateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean statusAllowsDeletion(VOService service)
            throws ObjectNotFoundException, OperationNotPermittedException,
            ConcurrentModificationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOCompatibleService> getPotentialCompatibleServices(
            VOService service) throws ObjectNotFoundException,
            OperationNotPermittedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isPartOfUpgradePath(VOService service)
            throws ObjectNotFoundException, OperationNotPermittedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOServiceDetails getServiceDetails(Product product,
            LocalizerFacade facade) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isPartOfUpgradePath(long serviceKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void copyDefaultPaymentEnablement(Product product,
            Organization vendor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSubscriptionLimitReached(Product product) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Product> getCustomerSpecificProducts(Organization cust,
            Organization seller) throws OperationNotPermittedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteProduct(Organization supplier, Product p)
            throws OperationNotPermittedException, ServiceOperationException,
            ServiceStateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOOrganization getServiceSellerFallback(long serviceKey,
            String locale) throws ObjectNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Product> getCustomerSpecificCopyProducts(Product template)
            throws OperationNotPermittedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOSubscriptionDetails validateSubscription(VOService service)
            throws OperationNotPermittedException, SubscriptionStateException,
            ObjectNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOCustomerService> getServiceCustomerTemplates(VOService service)
            throws ObjectNotFoundException, OperationNotPermittedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteService(Long key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteTechnicalService(Long key) {
        throw new UnsupportedOperationException();
    }
}
