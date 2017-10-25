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

import javax.jws.WebService;

import org.oscm.intf.ServiceProvisioningService;
import org.oscm.types.enumtypes.OrganizationRoleType;
import org.oscm.vo.VOCompatibleService;
import org.oscm.vo.VOCustomerService;
import org.oscm.vo.VOImageResource;
import org.oscm.vo.VOLocalizedText;
import org.oscm.vo.VOOrganization;
import org.oscm.vo.VOPriceModel;
import org.oscm.vo.VOPriceModelLocalization;
import org.oscm.vo.VOService;
import org.oscm.vo.VOServiceActivation;
import org.oscm.vo.VOServiceDetails;
import org.oscm.vo.VOServiceEntry;
import org.oscm.vo.VOServiceLocalization;
import org.oscm.vo.VOTechnicalService;

/**
 * This is a stub implementation of the {@link ServiceProvisioningService} as
 * the Metro jax-ws tools do not allow to generate WSDL files from the service
 * interfaces.
 * 
 * <p>
 * <b>WARNING:</b> Do not use this class, all methods simply throw an
 * {@link UnsupportedOperationException}!
 * </p>
 * 
 * @author Aleh Khomich
 */
@WebService(serviceName = "ServiceProvisioningService", targetNamespace = "http://oscm.org/xsd", portName = "ServiceProvisioningServicePort", endpointInterface = "org.oscm.intf.ServiceProvisioningService")
public class ServiceProvisioningServiceImpl implements
        ServiceProvisioningService {

    @Override
    public VOService activateService(VOService product) {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOServiceDetails createService(VOTechnicalService technicalProduct,
            VOService productToCreate, VOImageResource voImageResource) {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOService deactivateService(VOService product) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteService(VOService product) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteTechnicalService(VOTechnicalService technicalProduct) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] exportTechnicalServices(List<VOTechnicalService> products) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOService> getCompatibleServices(VOService referenceProduct) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOService> getServicesForCustomer(VOOrganization customer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOCustomerService> getAllCustomerSpecificServices() {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOServiceDetails getServiceDetails(VOService product) {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOServiceDetails getServiceForCustomer(VOOrganization customer,
            VOService product) {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOServiceDetails getServiceForSubscription(VOOrganization customer,
            String subscriptionId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getSupportedCurrencies() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOTechnicalService> getTechnicalServices(
            OrganizationRoleType organizationRoleType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String importTechnicalServices(byte[] xml) {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOImageResource loadImage(Long productKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOServiceDetails savePriceModel(VOServiceDetails productDetails,
            VOPriceModel priceModel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOServiceDetails savePriceModelForCustomer(
            VOServiceDetails productDetails, VOPriceModel priceModel,
            VOOrganization customer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOServiceDetails savePriceModelForSubscription(
            VOServiceDetails productDetails, VOPriceModel priceModel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOTechnicalService createTechnicalService(
            VOTechnicalService technicalProduct) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void saveTechnicalServiceLocalization(
            VOTechnicalService technicalProduct) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCompatibleServices(VOService sourceProduct,
            List<VOService> compatibleProducts) {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOServiceDetails updateService(VOServiceDetails productDetails,
            VOImageResource voImageResource) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void validateTechnicalServiceCommunication(
            VOTechnicalService technicalProduct) {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOPriceModelLocalization getPriceModelLocalization(
            VOPriceModel pricemodel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOServiceLocalization getServiceLocalization(VOService product) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void savePriceModelLocalization(VOPriceModel pricemodel,
            VOPriceModelLocalization localization) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void saveServiceLocalization(VOService product,
            VOServiceLocalization localization) {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOServiceDetails copyService(VOService service, String serviceId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOImageResource loadImageForSupplier(String serviceId,
            String supplierId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOLocalizedText> getPriceModelLicenseTemplateLocalization(
            VOServiceDetails service) {
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
    public List<VOService> getRelatedServicesForMarketplace(VOService service,
            String marketplaceId, String locale) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOService> setActivationStates(
            List<VOServiceActivation> activations) {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOServiceEntry getServiceForMarketplace(Long serviceKey,
            String marketplaceId, String locale) {
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
    public VOService suspendService(VOService service, String reason) {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOService resumeService(VOService service) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean statusAllowsDeletion(VOService service) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isPartOfUpgradePath(VOService service) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOCompatibleService> getPotentialCompatibleServices(
            VOService service) {
        throw new UnsupportedOperationException();
    }
}
